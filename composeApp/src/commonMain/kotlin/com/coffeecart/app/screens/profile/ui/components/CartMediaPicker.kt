package com.coffeecart.app.screens.profile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch

/**
 * A highly reusable media selection/capture picker offering FileKit's real system file picker
 * for Storage, and its real camera capture for Camera (Android/iOS only — FileKit's camera
 * launcher is not supported on Wasm; the button is disabled there instead of faking a photo).
 *
 * Note: a picked/captured photo only previews locally here — there is no upload endpoint yet,
 * so it is not persisted with the cart. [imageUrl] / [onImageUrlChange] are unaffected.
 */
@Composable
fun CartMediaPicker(
    imageUrl: String,
    onImageUrlChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Select Cart Image",
) {
    var pickedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val filePickerLauncher = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        file?.let {
            coroutineScope.launch {
                val bytes = it.readBytes()
                pickedImageBytes = bytes
                onImageUrlChange("https://picsum.photos/seed/${kotlin.math.abs(bytes.hashCode())}/400")
            }
        }
    }

    val cameraLauncher = rememberCartCameraLauncher { bytes ->
        pickedImageBytes = bytes
        onImageUrlChange("https://picsum.photos/seed/${kotlin.math.abs(bytes.hashCode())}/400")
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.Small.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = Spacing.Small.dp)
        )

        val previewModel: Any? = pickedImageBytes ?: imageUrl.takeIf { it.isNotEmpty() }
        if (previewModel != null) {
            Card(modifier = Modifier.size(Spacing.XXXLarge.dp * 4)) {
                AsyncImage(
                    model = previewModel,
                    contentDescription = "Cart Preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.Medium.dp)
        ) {
            Button(
                onClick = { filePickerLauncher.launch() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Gallery"
                )
                Spacer(Modifier.width(Spacing.Small.dp))
                Text("Storage")
            }
            Button(
                onClick = { cameraLauncher.launch() },
                enabled = cameraLauncher.isSupported,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "Camera"
                )
                Spacer(Modifier.width(Spacing.Small.dp))
                Text("Camera")
            }
        }

        if (!cameraLauncher.isSupported) {
            Text(
                text = "Camera capture isn't available on this platform.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview
@Composable
private fun CartMediaPickerPreview() {
    CartMediaPicker(
        imageUrl = "",
        onImageUrlChange = {}
    )
}

