package com.coffeecart.app.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Named spacing scale. Use this instead of raw `.dp` literals in UI code, so every spacing value
 * in the app traces back to one of these steps.
 */
enum class Spacing {
    XXXSmall,
    XXSmall,
    XSmall,
    Small,
    Medium,
    Large,
    XLarge,
    XXLarge,
    XXXLarge,
    XXXXLarge,
    XXXXXLarge,
    HeroHeight,
    MapHeight,
    CategoryCardHeight,
}

val Spacing.dp: Dp
    get() = when (this) {
        Spacing.XXXSmall -> 2.dp
        Spacing.XXSmall -> 4.dp
        Spacing.XSmall -> 6.dp
        Spacing.Small -> 8.dp
        Spacing.Medium -> 12.dp
        Spacing.Large -> 16.dp
        Spacing.XLarge -> 20.dp
        Spacing.XXLarge -> 24.dp
        Spacing.XXXLarge -> 32.dp
        Spacing.XXXXLarge -> 48.dp
        Spacing.XXXXXLarge -> 96.dp
        Spacing.HeroHeight -> 240.dp
        Spacing.MapHeight -> 200.dp
        Spacing.CategoryCardHeight -> 160.dp
    }
