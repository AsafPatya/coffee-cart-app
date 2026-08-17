package com.coffeecart.app.screens.profile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp

/**
 * A highly reusable media selection/capture picker overlay offering
 * simulated offline Storage and camera viewfinder simulations.
 */
@Composable
fun CartMediaPicker(
    imageUrl: String,
    onImageUrlChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showGallery by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.Small.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Select Cart Image",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = Spacing.Small.dp)
        )

        if (imageUrl.isNotEmpty()) {
            Card(modifier = Modifier.size(Spacing.XXXLarge.dp * 4)) {
                AsyncImage(
                    model = imageUrl,
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
                onClick = { showGallery = true },
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
                onClick = { showCamera = true },
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
    }

    // LOCAL PHOTO PICKERS
    if (showGallery) {
        val galleryOptions = listOf(
            "Downtown Cart" to "https://picsum.photos/seed/1/200",
            "Riverside Brew" to "https://picsum.photos/seed/2/200",
            "Central Park Coffee" to "https://picsum.photos/seed/3/200",
            "Cozy Corner Brew" to "https://picsum.photos/seed/4/200",
            "Metro Espresso" to "https://picsum.photos/seed/5/200"
        )

        AlertDialog(
            onDismissRequest = { showGallery = false },
            title = { Text("Simulated Media Storage") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Small.dp)
                ) {
                    Text("Pick an image from simulated device storage:")
                    Column(
                        modifier = Modifier.height(Spacing.XXXLarge.dp * 5).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(Spacing.XSmall.dp)
                    ) {
                        galleryOptions.forEach { (label, url) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onImageUrlChange(url)
                                        showGallery = false
                                    }
                                    .padding(Spacing.Small.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = label,
                                    modifier = Modifier.size(Spacing.XXXLarge.dp).clip(MaterialTheme.shapes.small),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(Spacing.Medium.dp))
                                Text(text = label, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showGallery = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showCamera) {
        var cameraStage by remember { mutableStateOf(0) }

        AlertDialog(
            onDismissRequest = {
                showCamera = false
                cameraStage = 0
            },
            title = { Text("Simulated Device Camera") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Medium.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (cameraStage == 0) {
                        Text("Point your camera and snapshot the coffee cart")
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(4f / 3f)
                                .background(Color.Black)
                                .border(Spacing.XXXSmall.dp, MaterialTheme.colorScheme.outline),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "📷 Simulated Viewfinder Active...",
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }

                        IconButton(
                            onClick = { cameraStage = 1 },
                            modifier = Modifier
                                .size(Spacing.XXXLarge.dp * 2)
                                .background(Color.White, CircleShape)
                                .border(Spacing.XXXSmall.dp, Color.Gray, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Camera,
                                contentDescription = "Shutter",
                                modifier = Modifier.size(Spacing.XXXLarge.dp)
                            )
                        }
                    } else {
                        val generatedUrl = "https://picsum.photos/seed/camera_${(100..999).random()}/200"
                        Text("Snapshot capturing succeeded!")
                        Card(modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f)) {
                            AsyncImage(
                                model = generatedUrl,
                                contentDescription = "Captured Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.Medium.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = { cameraStage = 0 },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Retake")
                            }
                            Button(
                                onClick = {
                                    onImageUrlChange(generatedUrl)
                                    showCamera = false
                                    cameraStage = 0
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Use Photo")
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                if (cameraStage == 0) {
                    TextButton(
                        onClick = {
                            showCamera = false
                            cameraStage = 0
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
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

