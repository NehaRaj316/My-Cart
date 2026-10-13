package com.example.data.model

data class CartItem(
    val product: Product,
    val quantity: Int = 1,
    val selectedColorHex: Long = product.primaryColorHex
) {
    val totalPrice: Double
        get() = product.price * quantity
}
