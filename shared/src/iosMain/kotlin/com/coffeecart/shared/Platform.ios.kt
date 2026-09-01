package com.coffeecart.shared

import platform.UIKit.UIDevice
import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSCalendarUnitWeekday
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute

actual fun platformName(): String =
    "${UIDevice.currentDevice.systemName()} ${UIDevice.currentDevice.systemVersion}"

actual fun getCurrentLocalTimeAndDay(): LocalTimeAndDay {
    val calendar = NSCalendar.currentCalendar
    val now = NSDate()
    val components = calendar.components(NSCalendarUnitWeekday or NSCalendarUnitHour or NSCalendarUnitMinute, fromDate = now)
    val iosWeekday = components.weekday.toInt()
    val dayValue = iosWeekday - 1

    val hour = components.hour.toString().padStart(2, '0')
    val minute = components.minute.toString().padStart(2, '0')

    return LocalTimeAndDay(dayValue, "$hour$minute")
}

