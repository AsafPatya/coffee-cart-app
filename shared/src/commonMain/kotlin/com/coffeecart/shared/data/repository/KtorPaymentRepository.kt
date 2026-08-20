package com.coffeecart.shared.data.repository

import com.coffeecart.shared.contract.CheckoutResponse
import com.coffeecart.shared.contract.Endpoints
import com.coffeecart.shared.contract.PaymentAccountResponse
import com.coffeecart.shared.data.remote.ServerEnvironment
import com.coffeecart.shared.domain.PaymentRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post

class KtorPaymentRepository(private val client: HttpClient) : PaymentRepository {

    override suspend fun connectPaymentAccount(cartId: String): String =
        client.post("${ServerEnvironment.baseUrl}${Endpoints.paymentAccount(cartId)}").body<PaymentAccountResponse>().url

    override suspend fun createCheckout(cartId: String, orderId: String): String =
        client.post("${ServerEnvironment.baseUrl}${Endpoints.orderCheckout(cartId, orderId)}").body<CheckoutResponse>().url
}
