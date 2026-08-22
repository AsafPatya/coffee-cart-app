package com.coffeecart.shared.data.repository

import com.coffeecart.shared.contract.CheckoutResponse
import com.coffeecart.shared.contract.Endpoints
import com.coffeecart.shared.contract.PaymentAccountResponse
import com.coffeecart.shared.data.remote.ServerEnvironment
import com.coffeecart.shared.domain.PaymentRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode

class KtorPaymentRepository(private val client: HttpClient) : PaymentRepository {

    override suspend fun connectPaymentAccount(cartId: String): String {
        val response = client.post("${ServerEnvironment.baseUrl}${Endpoints.paymentAccount(cartId)}")
        if (response.status.value !in 200..299) {
            val errorMsg = try { response.bodyAsText() } catch (e: Exception) { "" }
            val message = if (errorMsg.isNotEmpty()) errorMsg else "Failed to connect payment account (HTTP ${response.status.value})."
            throw Exception(message)
        }
        return response.body<PaymentAccountResponse>().url
    }

    override suspend fun createCheckout(cartId: String, orderId: String): String {
        val response = client.post("${ServerEnvironment.baseUrl}${Endpoints.orderCheckout(cartId, orderId)}")
        if (response.status == HttpStatusCode.BadRequest) {
            throw Exception("This coffee cart does not have a connected payment account. Please connect a payment account first.")
        } else if (response.status.value !in 200..299) {
            val errorMsg = try { response.bodyAsText() } catch (e: Exception) { "" }
            val message = if (errorMsg.isNotEmpty()) errorMsg else errorMsg
            throw Exception(if (message.isNotEmpty()) message else "Failed to create checkout (HTTP ${response.status.value}).")
        }
        return response.body<CheckoutResponse>().url
    }
}
