package com.coffeecart.app.ui.location

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Low-cost placeholder: shows coordinates as text instead of an embedded interactive map.
// TODO: replace with a real embedded map (e.g. Leaflet via a DOM overlay) when prioritized.
@Composable
actual fun CoffeeCartMap(latitude: Double, longitude: Double, modifier: Modifier) {
    Box(modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
        Text("Location: $latitude, $longitude")
    }
}
