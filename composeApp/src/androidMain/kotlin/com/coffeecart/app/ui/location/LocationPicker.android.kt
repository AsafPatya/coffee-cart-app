package com.coffeecart.app.ui.location

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.swmansion.kmpmaps.core.CameraPosition
import com.swmansion.kmpmaps.core.Coordinates
import com.swmansion.kmpmaps.core.Map
import com.swmansion.kmpmaps.core.MapProperties
import com.swmansion.kmpmaps.core.MapUISettings
import com.swmansion.kmpmaps.core.Marker

@Composable
actual fun rememberLocationPicker(onPicked: (latitude: Double, longitude: Double) -> Unit): (() -> Unit)? {
    var isVisible by remember { mutableStateOf(false) }
    var picked by remember { mutableStateOf<Coordinates?>(null) }

    if (isVisible) {
        Dialog(onDismissRequest = { isVisible = false }) {
            Box(Modifier.fillMaxSize()) {
                Map(
                    modifier = Modifier.fillMaxSize(),
                    properties = MapProperties(isMyLocationEnabled = true),
                    uiSettings = MapUISettings(myLocationButtonEnabled = true),
                    cameraPosition = CameraPosition(
                        coordinates = picked ?: Coordinates(latitude = 0.0, longitude = 0.0),
                        zoom = 14f,
                    ),
                    markers = picked?.let { listOf(Marker(coordinates = it)) } ?: emptyList(),
                    onMapClick = { coordinates -> picked = coordinates },
                )
                Button(
                    onClick = {
                        picked?.let { onPicked(it.latitude, it.longitude) }
                        isVisible = false
                    },
                    enabled = picked != null,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                ) {
                    Text("Confirm location")
                }
            }
        }
    }

    return { isVisible = true }
}
