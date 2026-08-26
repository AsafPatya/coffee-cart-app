package com.coffeecart.app.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Named border width scale. Use this instead of raw `.dp` literals for borders in UI code.
 */
enum class BorderWidth {
    XXXSmall,
}

val BorderWidth.dp: Dp
    get() = when (this) {
        BorderWidth.XXXSmall -> 1.dp
    }


