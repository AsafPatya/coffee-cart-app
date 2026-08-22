package com.coffeecart.server.rapyd

import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Rapyd's request-signing algorithm, used both for outbound API calls and for verifying
 * incoming webhooks. Per Rapyd's docs:
 * signature = BASE64(HMAC-SHA256(method + url_path + salt + timestamp + access_key + secret_key + body_string))
 */
object RapydSigner {
    fun sign(
        method: String,
        urlPath: String,
        salt: String,
        timestamp: Long,
        body: String,
        accessKey: String,
        secretKey: String,
    ): String {
        val toSign = if (method.isNotEmpty()) {
            // Outbound API signature formula
            "$accessKey${method.lowercase()}$urlPath$salt$timestamp$secretKey$body"
        } else {
            // Webhook signature formula
            "$urlPath$salt$timestamp$accessKey$secretKey$body"
        }
        val mac = Mac.getInstance("HmacSHA256").apply {
            init(SecretKeySpec(secretKey.toByteArray(), "HmacSHA256"))
        }
        val hex = mac.doFinal(toSign.toByteArray()).joinToString("") { "%02x".format(it) }
        return Base64.getEncoder().encodeToString(hex.toByteArray())
    }
}
