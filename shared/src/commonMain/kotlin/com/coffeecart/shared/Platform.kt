package com.coffeecart.shared

/** Identifies the platform the shared module is running on. Used to prove the KMP wiring works. */
expect fun platformName(): String
