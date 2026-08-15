package com.coffeecart.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.coffeecart.app.screens.MainScreen

/** Root composable, shared by every platform. */
@Composable
fun App() {
    MaterialTheme {
        Surface {
            MainScreen()
        }
    }
}
