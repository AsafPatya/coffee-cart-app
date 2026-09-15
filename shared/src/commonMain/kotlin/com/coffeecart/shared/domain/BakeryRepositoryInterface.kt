package com.coffeecart.shared.domain

import com.coffeecart.shared.model.Bakery

interface BakeryRepositoryInterface {
    suspend fun getBakeries(): List<Bakery>
}
