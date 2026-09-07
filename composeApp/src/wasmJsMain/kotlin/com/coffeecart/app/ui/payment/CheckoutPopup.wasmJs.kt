package com.coffeecart.app.ui.payment

// No-op: checkout now redirects the current tab (see CheckoutWebView.wasmJs.kt) instead of using a
// popup, so there's nothing to pre-open at click time.
actual class CheckoutPopupHandle

actual fun openBlankCheckoutPopup(): CheckoutPopupHandle? = null

actual fun CheckoutPopupHandle.navigateCheckoutPopup(url: String) = Unit

actual fun CheckoutPopupHandle.closeCheckoutPopup() = Unit
