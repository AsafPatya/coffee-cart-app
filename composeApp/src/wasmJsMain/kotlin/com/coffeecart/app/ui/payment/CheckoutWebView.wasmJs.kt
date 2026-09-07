package com.coffeecart.app.ui.payment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp

// A popup/iframe both proved unreliable here: payment providers (Rapyd, Grow) block being framed,
// and a canvas-rendered app can't reliably keep window.open() tied to the user's click, so browsers
// often ignore the requested popup size and open a full tab anyway. A same-tab redirect sidesteps
// both problems — same pattern most hosted-checkout integrations use. See CheckoutReturn.kt for how
// the app detects the redirect back to completeUrlPrefix/errorUrlPrefix after a fresh page load.
@Composable
actual fun CheckoutWebView(
    url: String,
    completeUrlPrefix: String,
    errorUrlPrefix: String,
    onComplete: () -> Unit,
    onError: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier,
    popupHandle: CheckoutPopupHandle?,
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
        LaunchedEffect(url) {
            redirectTo(url)
        }
    }
}

@JsFun("(url) => { window.location.href = url; }")
private external fun redirectTo(url: String)
