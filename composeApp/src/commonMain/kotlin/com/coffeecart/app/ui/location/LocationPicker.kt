package com.coffeecart.app.ui.location

import androidx.compose.runtime.Composable

/**
 * Returns a launcher that opens a full-screen map for picking a location, or null on platforms
 * that don't support it (location picking is a mobile-only manager action for now).
 */
@Composable
expect fun rememberLocationPicker(onPicked: (latitude: Double, longitude: Double) -> Unit): (() -> Unit)?
