package com.coffeecart.shared

actual fun platformName(): String = "Web (Wasm)"

@JsFun("() => { const d = new Date(); return d.getDay(); }")
private external fun getJsDay(): Int

@JsFun("() => { const d = new Date(); return d.getHours(); }")
private external fun getJsHours(): Int

@JsFun("() => { const d = new Date(); return d.getMinutes(); }")
private external fun getJsMinutes(): Int

actual fun getCurrentLocalTimeAndDay(): LocalTimeAndDay {
    val day = getJsDay()
    val hourStr = getJsHours().toString().padStart(2, '0')
    val minuteStr = getJsMinutes().toString().padStart(2, '0')
    return LocalTimeAndDay(day, "$hourStr$minuteStr")
}

