package com.coffeecart.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.coffeecart.app.screens.MainScreen
import com.coffeecart.shared.di.coffeeCartModule
import org.koin.compose.KoinApplication

/** Root composable, shared by every platform. */
@Composable
fun App() {
    KoinApplication(application = { modules(coffeeCartModule) }) {
        MaterialTheme {
            Surface {
                MainScreen()
            }
        }
    }
}
