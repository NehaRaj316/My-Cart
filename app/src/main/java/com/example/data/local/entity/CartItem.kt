package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Room @Entity data class representing a shopping cart item stored locally in the database.
 * Contains fields for id, name, price, and quantity to support the local shopping cart database.
 */
@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val price: Double,
    val quantity: Int = 1
) {
    /**
     * Secondary constructor supporting Long id.
     */
    @Ignore
    constructor(id: Long, name: String, price: Double, quantity: Int = 1) : this(
        id = id.toInt(),
        name = name,
        price = price,
        quantity = quantity
    )

    /**
     * Secondary constructor supporting String id.
     */
    @Ignore
    constructor(id: String, name: String, price: Double, quantity: Int = 1) : this(
        id = id.toIntOrNull() ?: 0,
        name = name,
        price = price,
        quantity = quantity
    )

    /**
     * Convenience secondary constructor with default auto-generated id.
     */
    @Ignore
    constructor(name: String, price: Double, quantity: Int = 1) : this(
        id = 0,
        name = name,
        price = price,
        quantity = quantity
    )

    val totalPrice: Double
        get() = price * quantity
}
