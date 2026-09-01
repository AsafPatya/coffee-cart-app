package com.coffeecart.shared

import kotlinx.serialization.Serializable

/** Identifies the platform the shared module is running on. Used to prove the KMP wiring works. */
expect fun platformName(): String

@Serializable
data class LocalTimeAndDay(
    val dayOfWeek: Int, // 0 is Sunday, 1 is Monday ... 6 is Saturday
    val hhmm: String,   // "HHmm" format (e.g. "1430")
)

expect fun getCurrentLocalTimeAndDay(): LocalTimeAndDay


