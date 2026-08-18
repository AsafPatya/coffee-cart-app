package com.coffeecart.app.ui.location

import androidx.compose.runtime.Composable

/** Requests the device's current location (prompting for permission if needed) and returns it once available, or null. */
@Composable
expect fun rememberCurrentLocation(): UserLocation?
