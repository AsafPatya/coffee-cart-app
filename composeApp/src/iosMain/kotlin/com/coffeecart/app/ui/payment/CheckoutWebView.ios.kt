package com.coffeecart.app.ui.payment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
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
        UIKitView(
            factory = {
                val webView = WKWebView()
                webView.navigationDelegate = object : NSObject(), WKNavigationDelegateProtocol {
                    override fun webView(
                        webView: WKWebView,
                        decidePolicyForNavigationAction: platform.WebKit.WKNavigationAction,
                        decisionHandler: (WKNavigationActionPolicy) -> Unit,
                    ) {
                        val navigatedUrl = decidePolicyForNavigationAction.request.URL?.absoluteString
                        when {
                            navigatedUrl != null && navigatedUrl.startsWith(completeUrlPrefix) -> {
                                onComplete()
                                decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
                            }
                            navigatedUrl != null && navigatedUrl.startsWith(errorUrlPrefix) -> {
                                onError("Payment failed or was cancelled.")
                                decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
                            }
                            else -> decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
                        }
                    }
                }
                webView.loadRequest(NSURLRequest(uRL = NSURL(string = url)))
                webView
            },
            modifier = Modifier.fillMaxSize(),
        )
        IconButton(onClick = onCancel, modifier = Modifier.align(Alignment.TopStart).padding(8.dp)) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
        }
    }
}
