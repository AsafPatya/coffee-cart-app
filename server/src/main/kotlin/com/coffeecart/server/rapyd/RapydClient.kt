package com.coffeecart.server.rapyd

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object RapydConfig {
    val baseUrl: String = System.getenv("RAPYD_BASE_URL") ?: "https://sandboxapi.rapyd.net"
    val accessKey: String = System.getenv("RAPYD_ACCESS_KEY") ?: error("RAPYD_ACCESS_KEY not set")
    val secretKey: String = System.getenv("RAPYD_SECRET_KEY") ?: error("RAPYD_SECRET_KEY not set")
}

@Serializable
private data class RapydEnvelope(val status: RapydStatus, val data: JsonElement? = null)

@Serializable
private data class RapydStatus(val status: String, val message: String? = null)

class RapydClient(private val client: HttpClient) {

    /** Creates a Rapyd wallet for a coffee cart owner. Returns the wallet id (ewallet_...) and its primary contact id (cont_...). */
    suspend fun createWallet(
        referenceId: String,
        firstName: String,
        lastName: String,
        email: String,
        phoneNumber: String,
        addressLine: String,
    ): WalletCreated {
        val body = """
            {"first_name":"$firstName","last_name":"$lastName","ewallet_reference_id":"$referenceId","type":"person",
            "contact":{"first_name":"$firstName","last_name":"$lastName","email":"$email","phone_number":"$phoneNumber",
            "contact_type":"personal","country":"IL","address":{"name":"$firstName $lastName","line_1":"$addressLine"}}}
        """.trimIndent().replace("\n", "").replace(" ", "")

        val data = post("/v1/ewallets", body).jsonObject
        val walletId = data["id"]?.jsonPrimitive?.content ?: error("Rapyd wallet creation did not return an id")
        // TODO(confirm against real sandbox response): exact path to the created contact's id —
        // likely data.contacts[0].id per Rapyd's typical wallet shape.
        val contactId = data["contacts"]?.jsonObject?.get("id")?.jsonPrimitive?.content ?: ""
        return WalletCreated(walletId = walletId, contactId = contactId)
    }

    /** Creates a Hosted IDV Page for a wallet's contact to complete their own KYC. Returns the URL to send them to. */
    suspend fun createIdvPage(walletId: String, contactId: String, referenceId: String): String {
        val body = """{"contact":"$contactId","ewallet":"$walletId","reference_id":"$referenceId"}"""
        val data = post("/v1/hosted/idv", body).jsonObject
        return data["redirect_url"]?.jsonPrimitive?.content ?: error("Rapyd IDV page creation did not return a redirect_url")
    }

    /** Creates a Checkout Page that routes payment directly to [walletId]. Returns the checkout URL. */
    suspend fun createCheckout(
        walletId: String,
        amountInMainUnits: Double,
        currency: String,
        orderId: String,
        completeUrl: String,
        errorUrl: String,
    ): String {
        val body = """
            {"amount":$amountInMainUnits,"currency":"$currency","country":"IL","ewallet":"$walletId",
            "complete_payment_url":"$completeUrl","error_payment_url":"$errorUrl","merchant_reference_id":"$orderId"}
        """.trimIndent().replace("\n", "").replace(" ", "")

        val data = post("/v1/checkout", body).jsonObject
        return data["redirect_url"]?.jsonPrimitive?.content ?: error("Rapyd checkout creation did not return a redirect_url")
    }

    private suspend fun post(urlPath: String, body: String): JsonElement {
        val salt = generateSalt()
        val timestamp = System.currentTimeMillis() / 1000
        val signature = RapydSigner.sign(
            method = "post",
            urlPath = urlPath,
            salt = salt,
            timestamp = timestamp,
            body = body,
            accessKey = RapydConfig.accessKey,
            secretKey = RapydConfig.secretKey,
        )

        val response = client.post("${RapydConfig.baseUrl}$urlPath") {
            contentType(ContentType.Application.Json)
            header("access_key", RapydConfig.accessKey)
            header("salt", salt)
            header("timestamp", timestamp)
            header("signature", signature)
            setBody(body)
        }

        val envelope = response.body<RapydEnvelope>()
        if (envelope.status.status != "SUCCESS") {
            error("Rapyd request to $urlPath failed: ${envelope.status.message}")
        }
        return envelope.data ?: error("Rapyd request to $urlPath returned no data")
    }

    private fun generateSalt(): String = (1..12)
        .map { "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".random() }
        .joinToString("")
}

data class WalletCreated(val walletId: String, val contactId: String)
