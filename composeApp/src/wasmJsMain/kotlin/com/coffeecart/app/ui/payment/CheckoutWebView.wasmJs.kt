package com.coffeecart.app.ui.payment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp

// The checkout page itself is cross-origin (the provider's domain), so its location can't be read
// while there — only once it navigates back to our own domain (complete/error URLs) does polling
// succeed. Opened as a popup window rather than an iframe because payment providers (Rapyd, Grow)
// send X-Frame-Options/CSP headers that block being framed at all.
@Composable
actual fun CheckoutWebView(
    url: String,
    completeUrlPrefix: String,
    errorUrlPrefix: String,
    onComplete: () -> Unit,
    onError: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier,
) {
    Column(modifier) {
        IconButton(
            onClick = onCancel,
            modifier = Modifier
                .align(Alignment.End)
                .padding(Spacing.Small.dp)
        ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
        }
        DisposableEffect(url) {
            val handle = openCheckoutWindow(
                url,
                completeUrlPrefix,
                errorUrlPrefix,
                onComplete = { onComplete() },
                onError = { onError("Payment failed or was cancelled.") },
                onClosed = { onCancel() },
            )
            onDispose { closeCheckoutWindow(handle) }
        }
    }
}

@JsFun(
    "(url, completePrefix, errorPrefix, onComplete, onError, onClosed) => {" +
        "const popup = window.open(url, 'checkout', 'width=480,height=760');" +
        "const interval = setInterval(() => {" +
        "  if (!popup || popup.closed) { clearInterval(interval); onClosed(); return; }" +
        "  try {" +
        "    const href = popup.location.href;" +
        "    if (href.indexOf(completePrefix) === 0) { clearInterval(interval); popup.close(); onComplete(); }" +
        "    else if (href.indexOf(errorPrefix) === 0) { clearInterval(interval); popup.close(); onError(); }" +
        "  } catch (e) { /* cross-origin while on the checkout page itself; ignore */ }" +
        "}, 500);" +
        "return { popup, interval };" +
        "}"
)
private external fun openCheckoutWindow(
    url: String,
    completePrefix: String,
    errorPrefix: String,
    onComplete: () -> Unit,
    onError: () -> Unit,
    onClosed: () -> Unit,
): JsAny

@JsFun("(handle) => { clearInterval(handle.interval); if (handle.popup && !handle.popup.closed) handle.popup.close(); }")
private external fun closeCheckoutWindow(handle: JsAny)
