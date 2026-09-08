package com.coffeecart.app.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.LayoutDirection

// The app is Hebrew-only for now (see values/strings.xml), regardless of browser locale.
@Composable
actual fun rememberAppLayoutDirection(): LayoutDirection = LayoutDirection.Rtl
