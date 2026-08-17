package com.coffeecart.app.screens.profile.ui.components

import androidx.compose.runtime.Composable

/** A launcher for capturing a photo with the device camera, where supported. */
interface CartCameraLauncher {
    /** False on platforms with no native camera capture (currently: Wasm). */
    val isSupported: Boolean
    fun launch()
}

/**
 * FileKit's camera capture API only exists on Android and iOS (it lives in FileKit's "mobile"
 * source set, absent from commonMain), so this expect/actual wraps it — Wasm gets a stub that
 * reports itself unsupported instead of failing to compile.
 */
@Composable
expect fun rememberCartCameraLauncher(onPhotoCaptured: (ByteArray) -> Unit): CartCameraLauncher
