package com.coffeecart.shared.contract

/**
 * Shared API endpoint paths used by both the Frontend (Shared Repository) and Backend (Server).
 */
object Endpoints {
    const val CARTS = "/carts"
    const val CARTS_ID = "/carts/{id}"

    /**
     * Returns the dynamic path for a specific coffee cart resource.
     */
    const val IMAGES_UPLOAD = "/images/upload"

    fun cartById(id: String): String = "$CARTS/$id"

    fun cartOrders(cartId: String): String = "$CARTS/$cartId/orders"

    fun advanceOrder(cartId: String, orderId: String): String = "$CARTS/$cartId/orders/$orderId/advance"
}

