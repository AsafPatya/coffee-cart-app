package com.coffeecart.printagent

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.coffeecart.shared.data.repository.KtorCoffeeCartRepository
import com.coffeecart.shared.data.repository.KtorOrderRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun main() = application {
    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    val coffeeCartRepository = KtorCoffeeCartRepository(httpClient)
    val orderRepository = KtorOrderRepository(httpClient)

    Window(onCloseRequest = ::exitApplication, title = "Coffee Cart Print Agent") {
        MaterialTheme {
            Surface {
                App(coffeeCartRepository, orderRepository)
            }
        }
    }
}

@Composable
private fun App(
    coffeeCartRepository: com.coffeecart.shared.domain.CoffeeCartRepository,
    orderRepository: com.coffeecart.shared.domain.OrderRepository,
) {
    var config by remember { mutableStateOf(loadAgentConfig()) }

    val currentConfig = config
    if (currentConfig == null) {
        SetupScreen(coffeeCartRepository, onConfigured = { config = it })
    } else {
        MainScreen(currentConfig, orderRepository, onReset = { config = null })
    }
}
