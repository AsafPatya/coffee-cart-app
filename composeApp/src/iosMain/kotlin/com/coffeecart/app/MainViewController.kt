package com.coffeecart.app

import androidx.compose.ui.window.ComposeUIViewController

/** Entry point consumed by the SwiftUI wrapper in `iosApp/`. */
fun MainViewController() = ComposeUIViewController { App() }
