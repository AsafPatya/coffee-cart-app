package com.coffeecart.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import androidx.compose.ui.tooling.preview.Preview

/** Placeholder — account details and sign-out land here once auth exists. */
@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(Spacing.XXLarge.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Sign-in and account details coming soon.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = Spacing.Small.dp),
        )
    }
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    ProfileScreen()
}
