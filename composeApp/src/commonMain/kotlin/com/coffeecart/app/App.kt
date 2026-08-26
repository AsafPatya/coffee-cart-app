package com.coffeecart.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import com.coffeecart.app.appcontainer.AppContainer
import com.coffeecart.app.theme.appTypography
import com.coffeecart.app.theme.rememberAppLayoutDirection
import com.coffeecart.shared.di.coffeeCartModule
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

/** Root composable, shared by every platform. */
@Composable
fun App() {
    KoinApplication(
        configuration = koinConfiguration(declaration = { modules(coffeeCartModule) }),
        content = {
            CompositionLocalProvider(LocalLayoutDirection provides rememberAppLayoutDirection()) {
                MaterialTheme(typography = appTypography()) {
                    Surface {
                        AppContainer()
                    }
                }
            }
        })
}
