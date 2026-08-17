package com.coffeecart.app.screens.profile.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import io.github.vinceglb.filekit.dialogs.FileKitCameraType
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch

@Composable
actual fun rememberCartCameraLauncher(onPhotoCaptured: (ByteArray) -> Unit): CartCameraLauncher {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val cameraPickerLauncher = rememberCameraPickerLauncher(type = FileKitCameraType.Photo) { file ->
        file?.let {
            coroutineScope.launch {
                onPhotoCaptured(it.readBytes())
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) cameraPickerLauncher.launch()
    }

    return remember(context) {
        object : CartCameraLauncher {
            override val isSupported: Boolean = true
            override fun launch() {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPermission) {
                    cameraPickerLauncher.launch()
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }
}
