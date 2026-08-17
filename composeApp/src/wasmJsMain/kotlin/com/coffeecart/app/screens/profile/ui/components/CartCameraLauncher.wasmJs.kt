package com.coffeecart.app.screens.profile.ui.components

import androidx.compose.runtime.Composable

@Composable
actual fun rememberCartCameraLauncher(onPhotoCaptured: (ByteArray) -> Unit): CartCameraLauncher =
    object : CartCameraLauncher {
        override val isSupported: Boolean = false
        override fun launch() {
            // No native camera capture API available in the browser via FileKit.
        }
    }
