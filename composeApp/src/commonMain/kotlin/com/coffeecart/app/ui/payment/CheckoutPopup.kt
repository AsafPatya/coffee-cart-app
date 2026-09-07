package com.coffeecart.app.ui.payment

/**
 * Opaque handle for a checkout popup window pre-opened synchronously inside a real click handler.
 * The checkout URL itself is only known after an async network round trip, but browsers only honor
 * `window.open` as a small sized popup (rather than blocking it, or opening a full new tab) when
 * it's called synchronously within a user gesture — so the blank popup must be opened at click time
 * and navigated to the real URL once it arrives. No-op on platforms that embed checkout in a native
 * WebView instead of a browser popup (Android/iOS).
 */
expect class CheckoutPopupHandle

expect fun openBlankCheckoutPopup(): CheckoutPopupHandle?

expect fun CheckoutPopupHandle.navigateCheckoutPopup(url: String)

expect fun CheckoutPopupHandle.closeCheckoutPopup()
