package com.coffeecart.shared.data.repository

import com.coffeecart.shared.domain.BakeryRepositoryInterface
import com.coffeecart.shared.model.Bakery

/** Stub implementation until a real bakery API exists; returns mock data. */
class BakeryRepository : BakeryRepositoryInterface {
    override suspend fun getBakeries(): List<Bakery> = listOf(
        Bakery(
            id = "bakery-1",
            name = "Sunrise Bakery",
            address = "12 Baker St",
            imageUrl = "https://images.unsplash.com/photo-1509440159596-0249088772ff",
        ),
        Bakery(
            id = "bakery-2",
            name = "Golden Crust Bakery",
            address = "48 Flour Ave",
            imageUrl = "https://images.unsplash.com/photo-1568254183919-78a4f43a2877",
        ),
        Bakery(
            id = "bakery-3",
            name = "Artisan Loaf House",
            address = "7 Wheat Rd",
            imageUrl = "https://images.unsplash.com/photo-1533089860892-a7c6f0a88666",
        ),
    )
}
