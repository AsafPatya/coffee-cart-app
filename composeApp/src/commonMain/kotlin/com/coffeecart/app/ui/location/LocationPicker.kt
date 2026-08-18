package com.coffeecart.app.ui.location

import androidx.compose.runtime.Composable

/**
 * Returns a launcher that opens a full-screen map for picking a location, or null on platforms
 * that don't support it (location picking is a mobile-only manager action for now).
 *
 * [initialLocation], when known (e.g. the cart's existing location, or the device's current
 * location), centers the map before anything is picked — without it the map would default to
 * (0, 0), which is open ocean and renders as a solid blue tile.
 */
@Composable
expect fun rememberLocationPicker(
    initialLocation: UserLocation? = null,
    onPicked: (latitude: Double, longitude: Double) -> Unit,
): (() -> Unit)?
