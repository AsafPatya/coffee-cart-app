package com.coffeecart.app.ui.payment

/**
 * The current page URL, used to detect a same-tab redirect back from a hosted checkout page after
 * a fresh page load (see CheckoutWebView.wasmJs.kt). Empty on platforms that embed checkout in a
 * native WebView instead, where this detection isn't needed.
 */
expect fun currentPageUrl(): String

/** Clears the complete/error URL from the address bar after handling it, so a page refresh doesn't
 *  re-trigger the same completion. No-op on platforms that don't use currentPageUrl(). */
expect fun clearCheckoutReturnUrl()
