package com.coffeecart.server

import com.coffeecart.server.db.DatabaseFactory
import com.coffeecart.server.db.PostgresCartStore
import com.coffeecart.server.db.PostgresOrderStore
import com.coffeecart.server.google.toDto
import com.coffeecart.server.google.GooglePlacesConfig
import com.coffeecart.server.google.GooglePlacesService
import com.coffeecart.server.rapyd.RapydClient
import com.coffeecart.server.rapyd.RapydConfig
import com.coffeecart.server.rapyd.RapydSigner
import com.coffeecart.shared.contract.CheckoutResponse
import com.coffeecart.shared.contract.CoffeeCartDto
import com.coffeecart.shared.contract.CreateCoffeeCartRequest
import com.coffeecart.shared.contract.Endpoints
import com.coffeecart.shared.contract.PaymentAccountResponse
import com.coffeecart.shared.contract.UploadImageResponse
import com.coffeecart.shared.contract.toDto
import com.coffeecart.shared.contract.toModel
import com.coffeecart.shared.model.OrderItem
import com.coffeecart.shared.model.PaymentStatus
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.get as clientGet
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.http.HttpMethod
import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import io.ktor.utils.io.toByteArray
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.util.UUID

private const val DEFAULT_PORT = 8080

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: DEFAULT_PORT
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
    install(CallLogging)
    install(CORS) {
        anyHost() // dev/QA convenience — the web (wasmJs) client runs on localhost or any preview URL
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(io.ktor.http.HttpHeaders.ContentType)
    }

    DatabaseFactory.init()
    val cartStore = PostgresCartStore()
    val orderStore = PostgresOrderStore()
    val httpClient = HttpClient(OkHttp) {
        install(ClientContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    val rapydClient = RapydClient(httpClient)

    // Mounted to a persistent Railway volume in production so uploaded images survive restarts/redeploys.
    val imagesDir = File(System.getenv("IMAGES_DIR") ?: "images").apply { mkdirs() }

    // RAILWAY_PUBLIC_DOMAIN is auto-injected by Railway for any service with a public domain —
    // needed because clients load images via absolute URLs, not relative to this server.
    val publicBaseUrl = System.getenv("RAILWAY_PUBLIC_DOMAIN")?.let { "https://$it" }
        ?: "http://localhost:${System.getenv("PORT")?.toIntOrNull() ?: DEFAULT_PORT}"

    val googlePlacesService = GooglePlacesService(httpClient, publicBaseUrl)

    routing {
        staticFiles("/images", imagesDir)

        post(Endpoints.IMAGES_UPLOAD) {
            var savedFileName: String? = null
            call.receiveMultipart().forEachPart { part ->
                if (part is PartData.FileItem) {
                    val extension = part.originalFileName?.substringAfterLast('.', "jpg") ?: "jpg"
                    val fileName = "${UUID.randomUUID()}.$extension"
                    val file = File(imagesDir, fileName)
                    file.writeBytes(part.provider().toByteArray())
                    savedFileName = fileName
                }
                part.dispose()
            }
            val fileName = savedFileName
            if (fileName != null) {
                call.respond(HttpStatusCode.Created, UploadImageResponse(url = "$publicBaseUrl/images/$fileName"))
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        get(Endpoints.CARTS) {
            call.respond(cartStore.getAll().map { it.toDto() })
        }

        get(Endpoints.PLACES + "/details") {
            val placeId = call.request.queryParameters["placeId"]
            println("[Places API] Fetching place details for placeId=$placeId")
            if (placeId.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val details = googlePlacesService.fetchPlaceDetails(placeId)
            if (details != null) {
                call.respond(details.toDto())
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Proxies Google's Place Photo API: Google sends no CORS headers on that endpoint, which silently
        // breaks image loading on the web target, and this also keeps our API key off the client entirely.
        get(Endpoints.PLACES_PHOTO) {
            val photoReference = call.request.queryParameters["photo_reference"]
            val apiKey = GooglePlacesConfig.apiKey
            if (photoReference.isNullOrBlank() || apiKey.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val googleUrl = "${GooglePlacesConfig.PHOTO_BASE_URL}?maxwidth=${GooglePlacesConfig.DEFAULT_PHOTO_MAX_WIDTH}" +
                "&photo_reference=$photoReference&key=$apiKey"
            val response = httpClient.clientGet(googleUrl)
            call.respondBytes(bytes = response.body(), contentType = response.contentType())
        }

        post(Endpoints.CARTS) {
            val request = call.receive<CreateCoffeeCartRequest>()
            var cart = cartStore.add(name = request.name, address = request.address, imageUrl = request.imageUrl, placeId = request.placeId, phone = request.phone, cartImages = request.cartImages)
            val placeId = request.placeId
            println("[Cart API] Creating cart with placeId=$placeId")
            if (!placeId.isNullOrBlank()) {
                val details = googlePlacesService.fetchPlaceDetails(placeId)
                println("[Cart API] Fetched details for placeId=$placeId: $details")
                if (details != null) {
                    val updatedCart = cart.copy(
                        openingHours = if (details.openingHours.isNotEmpty()) details.openingHours else cart.openingHours,
                        latitude = details.latitude ?: cart.latitude,
                        longitude = details.longitude ?: cart.longitude,
                        phone = details.phoneNumber ?: cart.phone,
                        cartImages = if (details.photoUrls.isNotEmpty()) details.photoUrls else cart.cartImages,
                        rating = details.rating ?: cart.rating,
                        userRatingsTotal = details.userRatingsTotal ?: cart.userRatingsTotal,
                        website = details.website ?: cart.website,
                        periods = if (details.periods.isNotEmpty()) details.periods else cart.periods,
                    )
                    cartStore.updateFull(updatedCart)
                    cart = updatedCart
                }
            }
            call.respond(HttpStatusCode.Created, cart.toDto())
        }

        delete(Endpoints.cartById("{id}")) {
            val id = call.parameters["id"]
            val removed = id != null && cartStore.remove(id)
            call.respond(if (removed) HttpStatusCode.NoContent else HttpStatusCode.NotFound)
        }

        delete(Endpoints.deleteCategory("{id}", "{categoryName}")) {
            val id = call.parameters["id"]
            val categoryName = call.parameters["categoryName"]
            val removed = id != null && categoryName != null && cartStore.deleteCategory(id, categoryName)
            call.respond(if (removed) HttpStatusCode.NoContent else HttpStatusCode.NotFound)
        }

        put(Endpoints.cartById("{id}")) {
            val id = call.parameters["id"]
            val request = call.receive<CoffeeCartDto>()
            val model = request.toModel()
            val placeId = model.placeId
            var hours = model.openingHours
            var lat = model.latitude
            var lng = model.longitude
            var phone = model.phone
            var images = model.cartImages
            var rating = model.rating
            var userRatingsTotal = model.userRatingsTotal
            var website = model.website
            var periods = model.periods
            println("[Cart API] Updating cart id=$id with placeId=$placeId")
            if (!placeId.isNullOrBlank()) {
                val details = googlePlacesService.fetchPlaceDetails(placeId)
                println("[Cart API] Fetched details for placeId=$placeId: $details")
                if (details != null) {
                    if (details.openingHours.isNotEmpty()) hours = details.openingHours
                    if (details.latitude != null) lat = details.latitude
                    if (details.longitude != null) lng = details.longitude
                    if (details.phoneNumber != null) phone = details.phoneNumber
                    if (details.photoUrls.isNotEmpty()) images = details.photoUrls
                    if (details.rating != null) rating = details.rating
                    if (details.userRatingsTotal != null) userRatingsTotal = details.userRatingsTotal
                    if (details.website != null) website = details.website
                    if (details.periods.isNotEmpty()) periods = details.periods
                }
            }
            val cartToUpdate = model.copy(
                openingHours = hours,
                latitude = lat,
                longitude = lng,
                phone = phone,
                cartImages = images,
                rating = rating,
                userRatingsTotal = userRatingsTotal,
                website = website,
                periods = periods,
            )
            val updated = id != null && cartStore.updateFull(cartToUpdate)
            call.respond(if (updated) HttpStatusCode.OK else HttpStatusCode.NotFound)
        }

        post(Endpoints.cartOrders("{id}")) {
            val cartId = call.parameters["id"]
            if (cartId == null) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                val items = call.receive<List<OrderItem>>()
                call.respond(HttpStatusCode.Created, orderStore.create(cartId, items))
            }
        }

        get(Endpoints.cartOrders("{id}")) {
            val cartId = call.parameters["id"]
            if (cartId == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            // Only paid orders are "live" — an order awaiting payment doesn't exist yet from the dashboard's view.
            val paidOrders = if (call.request.queryParameters["unprinted"] == "true") {
                orderStore.getUnprintedByCart(cartId).filter { it.paymentStatus == PaymentStatus.PAID }
            } else {
                orderStore.getByCart(cartId).filter { it.paymentStatus == PaymentStatus.PAID }
            }
            call.respond(paidOrders)
        }

        post(Endpoints.markOrderPrinted("{id}", "{orderId}")) {
            val orderId = call.parameters["orderId"]
            val marked = orderId != null && orderStore.markPrinted(orderId)
            call.respond(if (marked) HttpStatusCode.OK else HttpStatusCode.NotFound)
        }

        post(Endpoints.advanceOrder("{id}", "{orderId}")) {
            val orderId = call.parameters["orderId"]
            val updated = orderId?.let { orderStore.advance(it) }
            if (updated != null) {
                call.respond(updated)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        post(Endpoints.paymentAccount("{id}")) {
            val cartId = call.parameters["id"]
            val cart = cartId?.let { cartStore.getById(it) }
            if (cart == null) {
                call.respond(HttpStatusCode.NotFound)
                return@post
            }
            try {
                val existingWalletId = cart.paymentAccountId
                val walletId: String
                val contactId: String
                if (existingWalletId != null) {
                    walletId = existingWalletId
                    contactId = rapydClient.getContactId(existingWalletId)
                } else {
                    val created = rapydClient.createWallet(
                        referenceId = cart.id,
                        firstName = cart.name,
                        lastName = "Owner",
                        email = "owner+${cart.id}@example.com",
                        phoneNumber = "+972000000000",
                        addressLine = cart.address,
                    )
                    walletId = created.walletId
                    contactId = created.contactId
                    cartStore.setPaymentAccount(cart.id, walletId)
                }
                val url = rapydClient.createIdvPage(
                    walletId = walletId,
                    contactId = contactId,
                    referenceId = "${cart.id}-${System.currentTimeMillis()}",
                )
                call.respond(HttpStatusCode.Created, PaymentAccountResponse(url = url))
            } catch (e: Exception) {
                System.err.println("Database/Rapyd ewallet creation failed:")
                e.printStackTrace()
                call.respondText(e.message ?: "Failed to connect payment account", status = HttpStatusCode.InternalServerError)
            }
        }

        post(Endpoints.orderCheckout("{id}", "{orderId}")) {
            val cartId = call.parameters["id"]
            val orderId = call.parameters["orderId"]
            val cart = cartId?.let { cartStore.getById(it) }
            val order = orderId?.let { orderStore.getById(it) }
            val walletId = "ewallet_f64e93a83b4830f61c8de72b3644387c"
            println("[Checkout API] Creating checkout: cartId=$cartId, orderId=$orderId, walletId=$walletId")
            if (order == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            try {
                val amount = order.items.sumOf { it.product.price * it.quantity }
                println("[Checkout API] Amount to charge: $amount ILS")
                val url = rapydClient.createCheckout(
                    walletId = walletId,
                    amountInMainUnits = amount,
                    currency = "ILS",
                    orderId = order.id,
                    completeUrl = "$publicBaseUrl/payments/complete",
                    errorUrl = "$publicBaseUrl/payments/error",
                )
                println("[Checkout API] Success! Redirect URL generated: $url")
                orderStore.setCheckoutUrl(order.id, url)
                call.respond(HttpStatusCode.Created, CheckoutResponse(url = url))
            } catch (e: Exception) {
                System.err.println("Database/Rapyd checkout creation failed:")
                e.printStackTrace()
                call.respondText(e.message ?: "Failed to create checkout", status = HttpStatusCode.InternalServerError)
            }
        }

        post(Endpoints.RAPYD_WEBHOOK) {
            val bodyText = call.receiveText()
            val signature = call.request.header("signature")
            val salt = call.request.header("salt")
            val timestamp = call.request.header("timestamp")
            if (signature == null || salt == null || timestamp == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val expected = RapydSigner.sign(
                method = "",
                urlPath = Endpoints.RAPYD_WEBHOOK,
                salt = salt,
                timestamp = timestamp.toLong(),
                body = bodyText,
                accessKey = RapydConfig.accessKey,
                secretKey = RapydConfig.secretKey,
            )
            if (expected != signature) {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }

            val json = Json { ignoreUnknownKeys = true }.parseToJsonElement(bodyText).jsonObject
            val type = json["type"]?.jsonPrimitive?.content
            val data = json["data"]?.jsonObject
            when (type) {
                // TODO(confirm exact event name against real sandbox webhook)
                "PAYMENT_COMPLETED" -> {
                    val orderId = data?.get("merchant_reference_id")?.jsonPrimitive?.content
                    orderId?.let { orderStore.markPaid(it) }
                }
                // TODO(confirm exact event name against real sandbox webhook)
                "EWALLET_ENABLED" -> {
                    val walletId = data?.get("id")?.jsonPrimitive?.content
                    walletId?.let { cartStore.setPaymentAccountVerified(it, true) }
                }
            }
            call.respond(HttpStatusCode.OK)
        }
    }
}

@Serializable
data class HealthResponse(val status: String)
