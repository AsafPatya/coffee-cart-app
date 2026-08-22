package com.coffeecart.app.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.browser.window

private val rtlLanguagePrefixes = setOf("he", "ar", "fa", "ur", "yi", "dv")

@Composable
actual fun rememberAppLayoutDirection(): LayoutDirection {
    val languageCode = window.navigator.language.substringBefore("-").lowercase()
    return if (languageCode in rtlLanguagePrefixes) LayoutDirection.Rtl else LayoutDirection.Ltr
}
