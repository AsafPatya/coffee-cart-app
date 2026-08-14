package com.coffeecart.shared

import kotlin.test.Test
import kotlin.test.assertTrue

class PlatformTest {

    @Test
    fun `every platform reports a non-blank name`() {
        assertTrue(platformName().isNotBlank())
    }
}
