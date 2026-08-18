package com.coffeecart.app.ui.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
actual fun rememberCurrentLocation(): UserLocation? {
    var location by remember { mutableStateOf<UserLocation?>(null) }

    DisposableEffect(Unit) {
        requestGeolocation { latitude, longitude -> location = UserLocation(latitude, longitude) }
        onDispose { }
    }

    return location
}

@JsFun(
    "(onSuccess) => { navigator.geolocation.getCurrentPosition(" +
        "function(pos) { onSuccess(pos.coords.latitude, pos.coords.longitude); }" +
        "); }"
)
private external fun requestGeolocation(onSuccess: (Double, Double) -> Unit)
