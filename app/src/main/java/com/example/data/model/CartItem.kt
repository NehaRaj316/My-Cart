package com.example.data.model

data class CartItem(
    val product: Product,
    val quantity: Int = 1,
    val selectedColorHex: Long = product.primaryColorHex
) {
    val id: String
        get() = product.id

    val name: String
        get() = product.title

    val price: Double
        get() = product.price

    val totalPrice: Double
        get() = product.price * quantity

    fun toEntity(): com.example.data.local.entity.CartItem =
        com.example.data.local.entity.CartItem(
            name = product.title,
            price = product.price,
            quantity = quantity
        )
}
