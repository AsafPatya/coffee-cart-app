package com.coffeecart.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import com.coffeecart.app.appcontainer.AppContainer
import com.coffeecart.app.theme.Colors.BackgroundCream
import com.coffeecart.app.theme.appTypography
import com.coffeecart.app.theme.rememberAppLayoutDirection
import com.coffeecart.shared.di.coffeeCartModule
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

// Overrides the default Material3 light scheme's off-white/lavender background and surface
// (~#FFFBFE) with plain white, per design request — everything else stays default.

private val appColorScheme = lightColorScheme(
    background = BackgroundCream,
    surface = BackgroundCream,
    primary = Color(0xFF4A1821)
)

/** Root composable, shared by every platform. */
@Composable
fun App() {
    KoinApplication(
        configuration = koinConfiguration(declaration = { modules(coffeeCartModule) }),
        content = {
            CompositionLocalProvider(LocalLayoutDirection provides rememberAppLayoutDirection()) {
                MaterialTheme(colorScheme = appColorScheme, typography = appTypography()) {
                    Surface {
                        AppContainer()
                    }
                }
            }
        })
}
