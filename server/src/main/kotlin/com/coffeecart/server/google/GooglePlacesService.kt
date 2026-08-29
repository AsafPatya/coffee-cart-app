package com.coffeecart.server.google

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class GooglePlacesService(
    private val client: HttpClient,
    private val apiKey: String? = System.getenv("GOOGLE_PLACES_API_KEY"),
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchOpeningHours(cartName: String, address: String, existingPlaceId: String? = null): String? {
        if (apiKey.isNullOrBlank()) {
            return null
        }
        try {
            val placeId = existingPlaceId ?: findPlaceId(cartName, address)
            if (placeId.isNullOrBlank()) return null

            val url = "https://maps.googleapis.com/maps/api/place/details/json?place_id=$placeId&fields=opening_hours&key=$apiKey"
            val responseText = client.get(url).bodyAsText()
            val jsonObj = json.parseToJsonElement(responseText).jsonObject
            val resultObj = jsonObj["result"]?.jsonObject
            val openingHoursObj = resultObj?.get("opening_hours")?.jsonObject
            val weekdayTextArray = openingHoursObj?.get("weekday_text")?.jsonArray

            if (weekdayTextArray != null && weekdayTextArray.isNotEmpty()) {
                return weekdayTextArray.joinToString("\n") { it.jsonPrimitive.content }
            }
        } catch (e: Exception) {
            System.err.println("Failed to fetch Google Places opening hours: ${e.message}")
        }
        return null
    }

    private suspend fun findPlaceId(name: String, address: String): String? {
        if (apiKey.isNullOrBlank()) return null
        try {
            val query = "$name $address".replace(" ", "+")
            val url = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=$query&inputtype=textquery&fields=place_id&key=$apiKey"
            val responseText = client.get(url).bodyAsText()
            val jsonObj = json.parseToJsonElement(responseText).jsonObject
            val candidates = jsonObj["candidates"]?.jsonArray
            if (candidates != null && candidates.isNotEmpty()) {
                return candidates[0].jsonObject["place_id"]?.jsonPrimitive?.content
            }
        } catch (e: Exception) {
            System.err.println("Failed to find Google Place ID: ${e.message}")
        }
        return null
    }
}

