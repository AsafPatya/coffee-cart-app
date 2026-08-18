package com.coffeecart.app.ui.location

import androidx.compose.runtime.Composable

@Composable
actual fun rememberLocationPicker(
    initialLocation: UserLocation?,
    onPicked: (latitude: Double, longitude: Double) -> Unit,
): (() -> Unit)? = null
