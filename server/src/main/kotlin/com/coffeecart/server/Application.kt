package com.coffeecart.server

import com.coffeecart.server.db.DatabaseFactory
import com.coffeecart.server.db.PostgresCartStore
import com.coffeecart.shared.contract.CoffeeCartDto
import com.coffeecart.shared.contract.CreateCoffeeCartRequest
import com.coffeecart.shared.contract.Endpoints
import com.coffeecart.shared.contract.UploadImageResponse
import com.coffeecart.shared.contract.toDto
import com.coffeecart.shared.contract.toModel
import io.ktor.http.HttpStatusCode
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
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import io.ktor.utils.io.toByteArray
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
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

    // Mounted to a persistent Railway volume in production so uploaded images survive restarts/redeploys.
    val imagesDir = File(System.getenv("IMAGES_DIR") ?: "images").apply { mkdirs() }

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
                call.respond(HttpStatusCode.Created, UploadImageResponse(url = "/images/$fileName"))
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        get(Endpoints.CARTS) {
            call.respond(cartStore.getAll().map { it.toDto() })
        }

        post(Endpoints.CARTS) {
            val request = call.receive<CreateCoffeeCartRequest>()
            val cart = cartStore.add(name = request.name, address = request.address, imageUrl = request.imageUrl)
            call.respond(HttpStatusCode.Created, cart.toDto())
        }

        delete(Endpoints.CARTS_ID) {
            val id = call.parameters["id"]
            val removed = id != null && cartStore.remove(id)
            call.respond(if (removed) HttpStatusCode.NoContent else HttpStatusCode.NotFound)
        }

        put(Endpoints.CARTS_ID) {
            val id = call.parameters["id"]
            val request = call.receive<CoffeeCartDto>()
            val updated = id != null && cartStore.updateFull(request.toModel())
            call.respond(if (updated) HttpStatusCode.OK else HttpStatusCode.NotFound)
        }
    }
}

@Serializable
data class HealthResponse(val status: String)
