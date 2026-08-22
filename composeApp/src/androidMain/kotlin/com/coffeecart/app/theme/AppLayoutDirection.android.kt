package com.coffeecart.app.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

@Composable
actual fun rememberAppLayoutDirection(): LayoutDirection = LocalLayoutDirection.current
