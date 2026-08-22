package com.coffeecart.server.rapyd

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
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
        val contactId = data["contacts"]?.jsonObject?.get("data")?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("id")?.jsonPrimitive?.content
            ?: error("Rapyd wallet creation did not return a contact id")
        return WalletCreated(walletId = walletId, contactId = contactId)
    }

    /** Looks up an existing wallet's primary contact id, for when we already stored the wallet id but not the contact id. */
    suspend fun getContactId(walletId: String): String {
        val data = get("/v1/ewallets/$walletId").jsonObject
        return data["contacts"]?.jsonObject?.get("data")?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("id")?.jsonPrimitive?.content
            ?: error("Rapyd wallet $walletId has no contact on file")
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
        val formattedAmount = if (amountInMainUnits % 1.0 == 0.0) {
            amountInMainUnits.toInt().toString()
        } else {
            amountInMainUnits.toString()
        }

        val body = """
            {"amount":$formattedAmount,"currency":"$currency","country":"IL","ewallet":"$walletId",
            "complete_payment_url":"$completeUrl","error_payment_url":"$errorUrl","merchant_reference_id":"$orderId"}
        """.trimIndent().replace("\n", "").replace(" ", "")

        val data = post("/v1/checkout", body).jsonObject
        return data["redirect_url"]?.jsonPrimitive?.content ?: error("Rapyd checkout creation did not return a redirect_url")
    }

    private suspend fun post(urlPath: String, body: String): JsonElement = request("post", urlPath, body) {
        client.post("${RapydConfig.baseUrl}$urlPath") {
            contentType(ContentType.Application.Json)
            header("access_key", RapydConfig.accessKey)
            header("salt", it.salt)
            header("timestamp", it.timestamp)
            header("signature", it.signature)
            setBody(body)
        }
    }

    private suspend fun get(urlPath: String): JsonElement = request("get", urlPath, "") {
        client.get("${RapydConfig.baseUrl}$urlPath") {
            contentType(ContentType.Application.Json)
            header("access_key", RapydConfig.accessKey)
            header("salt", it.salt)
            header("timestamp", it.timestamp)
            header("signature", it.signature)
        }
    }

    private class SignedHeaders(val salt: String, val timestamp: Long, val signature: String)

    private suspend fun request(
        method: String,
        urlPath: String,
        body: String,
        call: suspend (SignedHeaders) -> io.ktor.client.statement.HttpResponse,
    ): JsonElement {
        val salt = generateSalt()
        val timestamp = System.currentTimeMillis() / 1000
                val signature = RapydSigner.sign(
            method = method,
            urlPath = urlPath,
            salt = salt,
            timestamp = timestamp,
            body = body,
            accessKey = RapydConfig.accessKey,
            secretKey = RapydConfig.secretKey,
        )

        println("[Rapyd Client] About to send outbound request:")
        println("  Method    : ${method.uppercase()}")
        println("  URL       : ${RapydConfig.baseUrl}$urlPath")
        if (body.isNotEmpty()) {
            println("  Body      : $body")
        }
        println("  Access Key: ${RapydConfig.accessKey}")
        println("  Salt      : $salt")
        println("  Timestamp : $timestamp")
        println("  Signature : $signature")

        val response = call(SignedHeaders(salt, timestamp, signature))
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
