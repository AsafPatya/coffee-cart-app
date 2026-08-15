package com.coffeecart.shared

actual fun platformName(): String = "JVM ${System.getProperty("java.version")}"
