package com.example.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.example.data.model.CartItem

data class CartItemWithProduct(
    @Embedded
    val cartItem: CartItemEntity,
    @Relation(
        parentColumn = "productId",
        entityColumn = "id"
    )
    val product: ProductEntity
) {
    fun toDomain(): CartItem = CartItem(
        product = product.toDomain(),
        quantity = cartItem.quantity,
        selectedColorHex = cartItem.selectedColorHex
    )
}
