package com.coffeecart.server.grow

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
private data class CreatePaymentLinkRequest(
    val orderId: String,
    val amount: Double,
    val completeUrl: String,
)

/** Calls a cart's Make.com webhook, which runs a scenario that creates a Grow payment link and returns its URL. */
class GrowClient(private val client: HttpClient) {

    suspend fun createPaymentLink(webhookUrl: String, orderId: String, amount: Double, completeUrl: String): String {
        val response = client.post(webhookUrl) {
            contentType(ContentType.Application.Json)
            setBody(CreatePaymentLinkRequest(orderId = orderId, amount = amount, completeUrl = completeUrl))
        }
        if (!response.status.isSuccess()) {
            error("Grow payment link creation failed: ${response.status}")
        }
        val json = Json { ignoreUnknownKeys = true }.parseToJsonElement(response.body<String>()).jsonObject
        return json["url"]?.jsonPrimitive?.content ?: error("Grow payment link creation did not return a url")
    }
}
