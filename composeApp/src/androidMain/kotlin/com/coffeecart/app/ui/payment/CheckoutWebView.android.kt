package com.coffeecart.app.ui.payment

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

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
    Box(modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
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
        IconButton(onClick = onCancel, modifier = Modifier.align(Alignment.TopStart).padding(8.dp)) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
        }
    }
}
