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

// The checkout page itself is cross-origin (Rapyd's domain), so its location can't be read while
// there — only once it navigates back to our own domain (complete/error URLs) does polling succeed.
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
                .align(Alignment.Start)
                .padding(Spacing.Small.dp)
        ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
        }
        DisposableEffect(url) {
            val handle = embedCheckoutIframe(
                url,
                completeUrlPrefix,
                errorUrlPrefix,
                onComplete = { onComplete() },
                onError = { onError("Payment failed or was cancelled.") },
            )
            onDispose { removeCheckoutIframe(handle) }
        }
    }
}

@JsFun(
    "(url, completePrefix, errorPrefix, onComplete, onError) => {" +
        "const iframe = document.createElement('iframe');" +
        "iframe.src = url;" +
        "iframe.style.position = 'fixed';" +
        "iframe.style.top = '0'; iframe.style.left = '0';" +
        "iframe.style.width = '100%'; iframe.style.height = '100%';" +
        "iframe.style.border = 'none'; iframe.style.zIndex = '1000';" +
        "document.body.appendChild(iframe);" +
        "const interval = setInterval(() => {" +
        "  try {" +
        "    const href = iframe.contentWindow.location.href;" +
        "    if (href.indexOf(completePrefix) === 0) { clearInterval(interval); onComplete(); }" +
        "    else if (href.indexOf(errorPrefix) === 0) { clearInterval(interval); onError(); }" +
        "  } catch (e) { /* cross-origin while on the checkout page itself; ignore */ }" +
        "}, 500);" +
        "iframe.dataset.pollHandle = interval;" +
        "return iframe;" +
        "}"
)
private external fun embedCheckoutIframe(
    url: String,
    completePrefix: String,
    errorPrefix: String,
    onComplete: () -> Unit,
    onError: () -> Unit,
): JsAny

@JsFun("(iframe) => { if (iframe.dataset.pollHandle) clearInterval(parseInt(iframe.dataset.pollHandle)); iframe.remove(); }")
private external fun removeCheckoutIframe(iframe: JsAny)
