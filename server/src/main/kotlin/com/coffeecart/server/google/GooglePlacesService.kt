package com.coffeecart.server.google

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class PlaceDetails(
    val openingHours: String? = null,
    val name: String? = null,
    val formattedAddress: String? = null,
    val phoneNumber: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val rating: Double? = null,
    val userRatingsTotal: Int? = null,
    val website: String? = null,
    val photoUrls: List<String> = emptyList(),
)

class GooglePlacesService(
    private val client: HttpClient,
    private val apiKey: String? = GooglePlacesConfig.apiKey,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchPlaceDetails(
        placeId: String,
        language: String? = GooglePlacesConfig.DEFAULT_LANGUAGE,
    ): PlaceDetails? {
        println("[GooglePlacesService] apiKey=$apiKey, language=$language")
        if (apiKey.isNullOrBlank() || placeId.isBlank()) {
            return null
        }
        try {
            val langParam = if (!language.isNullOrBlank()) "&language=$language" else ""
            val url = "${GooglePlacesConfig.DETAILS_BASE_URL}?place_id=$placeId&fields=${GooglePlacesConfig.FIELDS}$langParam&key=$apiKey"
            val responseText = client.get(url).bodyAsText()
            println("[GooglePlacesService] Response from Google Places API:\n$responseText")
            val jsonObj = json.parseToJsonElement(responseText).jsonObject
            val resultObj = jsonObj["result"]?.jsonObject ?: return null

            val name = resultObj[GooglePlacesConfig.FIELD_NAME]?.jsonPrimitive?.content
            val formattedAddress = resultObj[GooglePlacesConfig.FIELD_FORMATTED_ADDRESS]?.jsonPrimitive?.content
            val phoneNumber = resultObj[GooglePlacesConfig.FIELD_FORMATTED_PHONE_NUMBER]?.jsonPrimitive?.content
            val website = resultObj[GooglePlacesConfig.FIELD_WEBSITE]?.jsonPrimitive?.content
            val rating = resultObj[GooglePlacesConfig.FIELD_RATING]?.jsonPrimitive?.doubleOrNull
            val userRatingsTotal = resultObj[GooglePlacesConfig.FIELD_USER_RATINGS_TOTAL]?.jsonPrimitive?.intOrNull

            val openingHoursObj = resultObj[GooglePlacesConfig.FIELD_OPENING_HOURS]?.jsonObject
            val weekdayTextArray = openingHoursObj?.get("weekday_text")?.jsonArray
            val openingHours = if (weekdayTextArray != null && weekdayTextArray.isNotEmpty()) {
                weekdayTextArray.joinToString("\n") { it.jsonPrimitive.content }
            } else null
            println("[GooglePlacesService] Opening hours for placeId=$placeId:\n$openingHours")

            val locationObj = resultObj[GooglePlacesConfig.FIELD_GEOMETRY]?.jsonObject?.get("location")?.jsonObject
            val latitude = locationObj?.get("lat")?.jsonPrimitive?.doubleOrNull
            val longitude = locationObj?.get("lng")?.jsonPrimitive?.doubleOrNull

            val photosArray = resultObj[GooglePlacesConfig.FIELD_PHOTOS]?.jsonArray
            val photoUrls = photosArray?.mapNotNull { photoElement ->
                val photoRef = photoElement.jsonObject["photo_reference"]?.jsonPrimitive?.content
                if (!photoRef.isNullOrBlank()) {
                    "${GooglePlacesConfig.PHOTO_BASE_URL}?maxwidth=${GooglePlacesConfig.DEFAULT_PHOTO_MAX_WIDTH}&photo_reference=$photoRef&key=$apiKey"
                } else null
            } ?: emptyList()

            val details = PlaceDetails(
                openingHours = openingHours,
                name = name,
                formattedAddress = formattedAddress,
                phoneNumber = phoneNumber,
                latitude = latitude,
                longitude = longitude,
                rating = rating,
                userRatingsTotal = userRatingsTotal,
                website = website,
                photoUrls = photoUrls,
            )
            println("[GooglePlacesService] Fetched PlaceDetails for placeId=$placeId: $details")
            return details
        } catch (e: Exception) {
            System.err.println("Failed to fetch Google Places details for placeId=$placeId: ${e.message}")
        }
        return null
    }

    suspend fun fetchOpeningHours(
        placeId: String,
        language: String? = GooglePlacesConfig.DEFAULT_LANGUAGE,
    ): String? {
        return fetchPlaceDetails(placeId, language)?.openingHours
    }
}

