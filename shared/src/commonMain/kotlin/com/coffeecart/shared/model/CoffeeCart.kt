package com.coffeecart.shared.model

import kotlinx.serialization.Serializable
import com.coffeecart.shared.LocalTimeAndDay
import com.coffeecart.shared.getCurrentLocalTimeAndDay

@Serializable
data class CoffeeCart(
    val id: String,
    val name: String,
    val address: String,
    val imageUrl: String,
    val categories: List<MenuCategory> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val paymentAccountId: String? = null,
    val paymentAccountVerified: Boolean = false,
    val openingHours: List<String> = emptyList(),
    val placeId: String? = null,
    val phone: String? = null,
    val cartImages: List<String> = emptyList(),
    val rating: Double? = null,
    val userRatingsTotal: Int? = null,
    val website: String? = null,
    val periods: List<OpeningPeriod> = emptyList(),
) {
    fun isOpenNow(currentTime: LocalTimeAndDay = getCurrentLocalTimeAndDay()): Boolean {
        if (periods.isEmpty()) return false
        val hhmm = currentTime.hhmm
        if (hhmm.length < 4) return false
        val currentHour = hhmm.substring(0, 2).toIntOrNull() ?: 0
        val currentMin = hhmm.substring(2, 4).toIntOrNull() ?: 0
        val currentMinutesTotal = currentHour * 60 + currentMin
        val dayOfWeek = currentTime.dayOfWeek

        for (period in periods) {
            val openDay = period.open.day
            if (period.open.time.length < 4) continue
            val openHour = period.open.time.substring(0, 2).toIntOrNull() ?: 0
            val openMin = period.open.time.substring(2, 4).toIntOrNull() ?: 0
            val openMinutesTotal = openHour * 60 + openMin

            if (period.close == null) {
                if (openDay == dayOfWeek) return true
                continue
            }

            val closeDay = period.close.day
            if (period.close.time.length < 4) continue
            val closeHour = period.close.time.substring(0, 2).toIntOrNull() ?: 0
            val closeMin = period.close.time.substring(2, 4).toIntOrNull() ?: 0
            val closeMinutesTotal = closeHour * 60 + closeMin

            if (openDay == closeDay) {
                if (dayOfWeek == openDay && currentMinutesTotal in openMinutesTotal..closeMinutesTotal) {
                    return true
                }
            } else {
                if (dayOfWeek == openDay && currentMinutesTotal >= openMinutesTotal) {
                    return true
                }
                if (dayOfWeek == closeDay && currentMinutesTotal <= closeMinutesTotal) {
                    return true
                }

                var checkDay = (openDay + 1) % 7
                while (checkDay != closeDay) {
                    if (dayOfWeek == checkDay) return true
                    checkDay = (checkDay + 1) % 7
                }
            }
        }
        return false
    }
}
