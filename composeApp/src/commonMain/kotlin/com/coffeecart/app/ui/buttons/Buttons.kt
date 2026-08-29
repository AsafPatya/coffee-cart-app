package com.coffeecart.app.ui.buttons

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import coffeecart.composeapp.generated.resources.Res
import coffeecart.composeapp.generated.resources.strBack
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import org.jetbrains.compose.resources.stringResource

/**
 * A circular back button styled for overlaying on hero images or top surfaces.
 */
@Composable
fun OverlayBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.padding(Spacing.Medium.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.4f),
            contentColor = Color.White,
        ) {
            Box(
                modifier = Modifier.padding(Spacing.Small.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.strBack),
                    tint = Color.White
                )
            }
        }
    }
}

@Preview
@Composable
private fun OverlayBackButtonPreview() {
    OverlayBackButton(onClick = {})
}

