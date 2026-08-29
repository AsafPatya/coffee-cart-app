package com.coffeecart.app.ui.location

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil3.compose.AsyncImage
import coffeecart.composeapp.generated.resources.Res
import coffeecart.composeapp.generated.resources.strNavigateNow
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import org.jetbrains.compose.resources.stringResource
import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
@Composable
actual fun CoffeeCartMap(latitude: Double, longitude: Double, modifier: Modifier) {
    var zoomLevel by remember { mutableStateOf(15) }
    val mapLang = remember { getMapLanguage() }
    val googleMapsUrl = "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
    val staticMapUrl = "https://static-maps.yandex.ru/1.x/?ll=$longitude,$latitude&z=$zoomLevel&l=sat,skl&lang=$mapLang&pt=$longitude,$latitude,pm2rdm"

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(Spacing.Large.dp))
            .clickable { openExternalUrl(googleMapsUrl) }
    ) {
        AsyncImage(
            model = staticMapUrl,
            contentDescription = "Map location: $latitude, $longitude",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // Zoom In/Out controls
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(Spacing.Small.dp),
            shape = RoundedCornerShape(Spacing.Medium.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shadowElevation = Spacing.Small.dp,
        ) {
            Column {
                IconButton(
                    onClick = { if (zoomLevel < 19) zoomLevel++ },
                    enabled = zoomLevel < 19,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                    )
                }
                IconButton(
                    onClick = { if (zoomLevel > 2) zoomLevel-- },
                    enabled = zoomLevel > 2,
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Spacing.Medium.dp),
            shape = RoundedCornerShape(Spacing.Large.dp),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = Spacing.Small.dp,
        ) {
            Row(
                modifier = Modifier
                    .clickable { openExternalUrl(googleMapsUrl) }
                    .padding(horizontal = Spacing.Large.dp, vertical = Spacing.Small.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    tint = Color.White,
                )
                Spacer(modifier = Modifier.width(Spacing.Small.dp))
                Text(
                    text = stringResource(Res.string.strNavigateNow),
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(url) => { window.open(url, '_blank'); }")
private external fun openExternalUrl(url: String)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => { return (navigator.language || 'en').replace('-', '_'); }")
private external fun getBrowserLanguage(): String

private fun getMapLanguage(): String {
    val navLang = getBrowserLanguage().lowercase()
    return when {
        navLang.startsWith("he") -> "he_IL"
        navLang.startsWith("ru") -> "ru_RU"
        navLang.startsWith("uk") -> "uk_UA"
        navLang.startsWith("tr") -> "tr_TR"
        else -> "en_US"
    }
}
