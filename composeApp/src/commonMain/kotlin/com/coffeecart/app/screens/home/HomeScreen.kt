package com.coffeecart.app.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import coffeecart.composeapp.generated.resources.Res
import coffeecart.composeapp.generated.resources.coffee_cart_quick_order
import coffeecart.composeapp.generated.resources.strClickHere
import coffeecart.composeapp.generated.resources.strToStartANewOrder
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Landing tab: full-bleed hero background, title, and a CTA into the coffee cart directory. */
@Composable
fun HomeScreen(onCtaButtonClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.coffee_cart_quick_order),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f)),
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(Spacing.XXLarge.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                stringResource(Res.string.strToStartANewOrder),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
            )

            Button(
                onClick = onCtaButtonClick,
                modifier = Modifier.padding(top = Spacing.XXLarge.dp),
            ) {
                Text(stringResource(Res.string.strClickHere))
            }
        }
    }
}

@Preview(locale = "iw")
@Composable
private fun HomeScreenPreview() {
    HomeScreen(onCtaButtonClick = {})
}
