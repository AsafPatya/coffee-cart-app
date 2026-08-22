package com.coffeecart.app.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.LayoutDirection

/**
 * Android and iOS derive [LayoutDirection] from the OS locale automatically. Compose
 * Multiplatform's Wasm/web target does not — it needs the browser's language read explicitly,
 * which is what the wasmJs actual does; Android/iOS actuals just pass the ambient value through.
 */
@Composable
expect fun rememberAppLayoutDirection(): LayoutDirection
