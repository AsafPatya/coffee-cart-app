package com.coffeecart.server

import com.coffeecart.server.db.DatabaseFactory
import com.coffeecart.server.db.PostgresCartStore
import com.coffeecart.shared.contract.CreateCoffeeCartRequest
import com.coffeecart.shared.contract.Endpoints
import com.coffeecart.shared.contract.toDto
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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

    DatabaseFactory.init()
    val cartStore = PostgresCartStore()

    routing {

        get(Endpoints.CARTS) {
            println("SERVER LOG: Received GET carts request")
            val carts = cartStore.getAll()
            println("SERVER LOG: Returning ${carts.size} carts from database")
            call.respond(carts.map { it.toDto() })
        }

        post(Endpoints.CARTS) {
            val request = call.receive<CreateCoffeeCartRequest>()
            println("SERVER LOG: Received POST cart request with data: name='${request.name}', address='${request.address}'")
            val cart = cartStore.add(name = request.name, address = request.address, imageUrl = request.imageUrl)
            println("SERVER LOG: Successfully inserted cart: ID='${cart.id}', name='${cart.name}'")
            call.respond(HttpStatusCode.Created, cart.toDto())
        }

        delete(Endpoints.CARTS_ID) {
            val id = call.parameters["id"]
            println("SERVER LOG: Received DELETE request for ID '$id'")
            val removed = id != null && cartStore.remove(id)
            println("SERVER LOG: Delete result for ID '$id': removed=$removed")
            call.respond(if (removed) HttpStatusCode.NoContent else HttpStatusCode.NotFound)
        }

        put(Endpoints.CARTS_ID) {
            val id = call.parameters["id"]
            val request = call.receive<CreateCoffeeCartRequest>()
            println("SERVER LOG: Received PUT update request for ID '$id' with data: name='${request.name}', address='${request.address}'")
            val updated = id != null && cartStore.update(cartId = id, name = request.name, address = request.address, imageUrl = request.imageUrl)
            println("SERVER LOG: Update result for ID '$id': updated=$updated")
            call.respond(if (updated) HttpStatusCode.OK else HttpStatusCode.NotFound)
        }
    }
}

@Serializable
data class HealthResponse(val status: String)
