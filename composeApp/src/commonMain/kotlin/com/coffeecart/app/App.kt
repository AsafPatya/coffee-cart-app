package com.coffeecart.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coffeecart.shared.platformName

/**
 * Root composable, shared by every platform.
 *
 * Scaffolding placeholder: it renders the platform name to prove that shared code, Compose, and
 * each platform entry point are wired together. Replaced by the customer shell in step 5.
 */
@Composable
fun App() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Coffee Cart", style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = "running on ${platformName()}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text("Asaf")
            }
        }
    }
}
