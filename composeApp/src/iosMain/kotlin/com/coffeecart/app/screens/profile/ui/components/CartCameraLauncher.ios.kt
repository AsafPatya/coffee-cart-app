package com.coffeecart.app.screens.profile.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.vinceglb.filekit.dialogs.FileKitCameraType
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch

@Composable
actual fun rememberCartCameraLauncher(onPhotoCaptured: (ByteArray) -> Unit): CartCameraLauncher {
    val coroutineScope = rememberCoroutineScope()
    val launcher = rememberCameraPickerLauncher(type = FileKitCameraType.Photo) { file ->
        file?.let {
            coroutineScope.launch {
                onPhotoCaptured(it.readBytes())
            }
        }
    }
    return object : CartCameraLauncher {
        override val isSupported: Boolean = true
        override fun launch() = launcher.launch()
    }
}
