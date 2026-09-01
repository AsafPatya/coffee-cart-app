package com.coffeecart.shared.contract

import kotlinx.serialization.Serializable
import com.coffeecart.shared.model.OpeningPeriod

@Serializable
data class PlaceDetailsDto(
    val openingHours: List<String> = emptyList(),
    val name: String? = null,
    val formattedAddress: String? = null,
    val phoneNumber: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val rating: Double? = null,
    val userRatingsTotal: Int? = null,
    val website: String? = null,
    val photoUrls: List<String> = emptyList(),
    val periods: List<OpeningPeriod> = emptyList(),
)

