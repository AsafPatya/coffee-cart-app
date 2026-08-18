package com.coffeecart.app.ui.location

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.swmansion.kmpmaps.core.CameraPosition
import com.swmansion.kmpmaps.core.Coordinates
import com.swmansion.kmpmaps.core.Map
import com.swmansion.kmpmaps.core.Marker

@Composable
actual fun CoffeeCartMap(latitude: Double, longitude: Double, modifier: Modifier) {
    val coordinates = Coordinates(latitude = latitude, longitude = longitude)
    Map(
        modifier = modifier,
        cameraPosition = CameraPosition(coordinates = coordinates, zoom = 14f),
        markers = listOf(Marker(coordinates = coordinates)),
    )
}
