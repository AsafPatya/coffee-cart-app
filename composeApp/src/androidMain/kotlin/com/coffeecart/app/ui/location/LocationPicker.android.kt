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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.swmansion.kmpmaps.core.CameraPosition
import com.swmansion.kmpmaps.core.Coordinates
import com.swmansion.kmpmaps.core.Map
import com.swmansion.kmpmaps.core.MapProperties
import com.swmansion.kmpmaps.core.MapUISettings
import com.swmansion.kmpmaps.core.Marker

@Composable
actual fun rememberLocationPicker(
    initialLocation: UserLocation?,
    onPicked: (latitude: Double, longitude: Double) -> Unit,
): (() -> Unit)? {
    var isVisible by remember { mutableStateOf(false) }
    var picked by remember { mutableStateOf<Coordinates?>(null) }
    val initialCameraCoordinates = initialLocation?.let { Coordinates(latitude = it.latitude, longitude = it.longitude) }
        ?: Coordinates(latitude = 0.0, longitude = 0.0)

    if (isVisible) {
        Dialog(
            onDismissRequest = { isVisible = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(Modifier.fillMaxSize()) {
                Map(
                    modifier = Modifier.fillMaxSize(),
                    properties = MapProperties(isMyLocationEnabled = true),
                    uiSettings = MapUISettings(myLocationButtonEnabled = true),
                    cameraPosition = CameraPosition(
                        coordinates = picked ?: initialCameraCoordinates,
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
                    modifier = Modifier.align(Alignment.BottomCenter).padding(Spacing.Large.dp),
                ) {
                    Text("Confirm location")
                }
            }
        }
    }

    return { isVisible = true }
}
