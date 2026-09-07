package com.coffeecart.app.ui.payment

actual fun currentPageUrl(): String = jsCurrentUrl()

actual fun clearCheckoutReturnUrl() = jsReplaceUrlWithRoot()

@JsFun("() => window.location.href")
private external fun jsCurrentUrl(): String

@JsFun("() => { window.history.replaceState(null, '', '/'); }")
private external fun jsReplaceUrlWithRoot()
