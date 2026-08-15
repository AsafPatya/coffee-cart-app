package com.coffeecart.server

import com.coffeecart.shared.contract.CreateCoffeeCartRequest
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
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable

private const val DEFAULT_PORT = 8080

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: DEFAULT_PORT
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) { json() }
    install(CallLogging)

    val cartStore = CartStore()

    routing {
        get("/health") { call.respond(HealthResponse(status = "ok")) }

        get("/carts") {
            call.respond(cartStore.getAll().map { it.toDto() })
        }

        post("/carts") {
            val request = call.receive<CreateCoffeeCartRequest>()
            val cart = cartStore.add(name = request.name, address = request.address, imageUrl = request.imageUrl)
            call.respond(HttpStatusCode.Created, cart.toDto())
        }

        delete("/carts/{id}") {
            val id = call.parameters["id"]
            val removed = id != null && cartStore.remove(id)
            call.respond(if (removed) HttpStatusCode.NoContent else HttpStatusCode.NotFound)
        }
    }
}

@Serializable
data class HealthResponse(val status: String)
