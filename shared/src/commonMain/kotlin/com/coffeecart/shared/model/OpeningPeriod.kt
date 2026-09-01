package com.coffeecart.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class DayTime(
    val day: Int,          // 0 to 6 (Sunday to Saturday)
    val time: String,      // HHmm (e.g., "0800", "1800")
)

@Serializable
data class OpeningPeriod(
    val open: DayTime,
    val close: DayTime? = null,
)

