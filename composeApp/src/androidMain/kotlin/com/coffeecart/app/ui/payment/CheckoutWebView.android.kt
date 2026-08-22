package com.coffeecart.app.ui.payment

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp

@SuppressLint("SetJavaScriptEnabled")
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
        AndroidView(
            modifier = Modifier.fillMaxWidth().weight(1f),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, requestUrl: String?): Boolean {
                            val navigatedUrl = requestUrl ?: return false
                            return when {
                                navigatedUrl.startsWith(completeUrlPrefix) -> {
                                    onComplete()
                                    true
                                }
                                navigatedUrl.startsWith(errorUrlPrefix) -> {
                                    onError("Payment failed or was cancelled.")
                                    true
                                }
                                else -> false
                            }
                        }
                    }
                    loadUrl(url)
                }
            },
        )
    }
}
