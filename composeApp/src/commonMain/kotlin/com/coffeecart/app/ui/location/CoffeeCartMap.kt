package com.coffeecart.app.ui.location

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Read-only map showing a single pin at the given coordinates. Pan/zoom allowed, no editing. */
@Composable
expect fun CoffeeCartMap(latitude: Double, longitude: Double, modifier: Modifier)
