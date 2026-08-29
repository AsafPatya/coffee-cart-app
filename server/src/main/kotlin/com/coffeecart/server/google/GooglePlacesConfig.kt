package com.coffeecart.server.google

object GooglePlacesConfig {
    const val ENV_API_KEY = "GOOGLE_PLACES_API_KEY"
    const val DETAILS_BASE_URL = "https://maps.googleapis.com/maps/api/place/details/json"
    const val PHOTO_BASE_URL = "https://maps.googleapis.com/maps/api/place/photo"
    const val DEFAULT_PHOTO_MAX_WIDTH = 800

    const val FIELD_NAME = "name"
    const val FIELD_FORMATTED_ADDRESS = "formatted_address"
    const val FIELD_OPENING_HOURS = "opening_hours"
    const val FIELD_FORMATTED_PHONE_NUMBER = "formatted_phone_number"
    const val FIELD_GEOMETRY = "geometry"
    const val FIELD_RATING = "rating"
    const val FIELD_USER_RATINGS_TOTAL = "user_ratings_total"
    const val FIELD_WEBSITE = "website"
    const val FIELD_PHOTOS = "photos"

    val FIELDS = "$FIELD_NAME,$FIELD_FORMATTED_ADDRESS,$FIELD_OPENING_HOURS,$FIELD_FORMATTED_PHONE_NUMBER,$FIELD_GEOMETRY,$FIELD_RATING,$FIELD_USER_RATINGS_TOTAL,$FIELD_WEBSITE,$FIELD_PHOTOS"

    const val DEFAULT_LANGUAGE = "he"

    val apiKey: String? = System.getenv(ENV_API_KEY)
}

