package com.coffeecart.app.ui.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberCurrentLocation(): UserLocation? {
    var location by remember { mutableStateOf<UserLocation?>(null) }
    val manager = remember { CLLocationManager() }

    DisposableEffect(Unit) {
        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val latest = didUpdateLocations.lastOrNull() as? CLLocation ?: return
                latest.coordinate.useContents {
                    location = UserLocation(latitude = latitude, longitude = longitude)
                }
                manager.stopUpdatingLocation()
            }
        }
        manager.delegate = delegate
        manager.requestWhenInUseAuthorization()
        manager.startUpdatingLocation()
        onDispose { manager.stopUpdatingLocation() }
    }

    return location
}
