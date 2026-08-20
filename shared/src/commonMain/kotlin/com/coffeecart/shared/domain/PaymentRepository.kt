package com.coffeecart.shared.domain

interface PaymentRepository {
    /** Starts (or resumes) a cart's payment-account onboarding. Returns the Hosted IDV Page URL to show the owner. */
    suspend fun connectPaymentAccount(cartId: String): String

    /** Creates a checkout for an order, routed directly to the cart's payment account. Returns the checkout URL. */
    suspend fun createCheckout(cartId: String, orderId: String): String
}
