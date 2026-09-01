package com.coffeecart.shared

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

actual fun platformName(): String = "Android ${android.os.Build.VERSION.SDK_INT}"

actual fun getCurrentLocalTimeAndDay(): LocalTimeAndDay {
    val now = LocalDateTime.now()
    val javaDayValue = now.dayOfWeek.value // 1 (Mon) to 7 (Sun)
    val dayValue = if (javaDayValue == 7) 0 else javaDayValue

    val formatter = DateTimeFormatter.ofPattern("HHmm")
    val hhmm = now.format(formatter)
    return LocalTimeAndDay(dayValue, hhmm)
}

