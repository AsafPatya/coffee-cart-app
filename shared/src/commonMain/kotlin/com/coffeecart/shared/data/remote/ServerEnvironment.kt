package com.coffeecart.shared.data.remote

object ServerEnvironment {
    private const val PROD_URL = "https://coffee-cart-app-production.up.railway.app"
    private const val QA_URL = "https://coffee-cart-app-qa.up.railway.app"
    private const val LOCAL_URL = "http://localhost:8080"
    private const val EMULATOR_URL = "http://10.0.2.2:8080" // For Android Emulator connecting to local server

    val baseUrl: String = QA_URL // flip this constant to switch environments (e.g. use EMULATOR_URL or LOCAL_URL for local testing)
}
