package com.coffeecart.app.ui.payment

actual class CheckoutPopupHandle

actual fun openBlankCheckoutPopup(): CheckoutPopupHandle? = null

actual fun CheckoutPopupHandle.navigateCheckoutPopup(url: String) = Unit

actual fun CheckoutPopupHandle.closeCheckoutPopup() = Unit
