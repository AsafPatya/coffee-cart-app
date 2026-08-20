package com.coffeecart.app.ui.payment

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Hosts a Rapyd-hosted checkout/onboarding page. Rendered as plain full-screen content (not a
 * Dialog/ModalBottomSheet) — those overlay containers were found to swallow touch/payment-sheet
 * gestures on iOS (see the location-picker work); a native Apple Pay/Google Pay sheet can still
 * be triggered from inside this page even though the page itself is web content.
 */
@Composable
expect fun CheckoutWebView(
    url: String,
    completeUrlPrefix: String,
    errorUrlPrefix: String,
    onComplete: () -> Unit,
    onError: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier,
)
