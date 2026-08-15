package com.coffeecart.shared.data.remote

object ServerEnvironment {
    private const val PROD_URL = "https://coffee-cart-app-production.up.railway.app"
    private const val QA_URL = "https://coffee-cart-app-qa.up.railway.app"

    val baseUrl: String = QA_URL // flip this constant to switch environments
}
