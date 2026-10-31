package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Room @Entity data class representing a 'Product' for offline shopping list management.
 * Stores product details like name, price, description, and category.
 */
@Entity(tableName = "shopping_list_products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "price")
    val price: Double,
    @ColumnInfo(name = "description")
    val description: String = "",
    @ColumnInfo(name = "category")
    val category: String = "General",
    @ColumnInfo(name = "is_purchased")
    val isPurchased: Boolean = false,
    @ColumnInfo(name = "quantity")
    val quantity: Int = 1,
    @ColumnInfo(name = "added_at")
    val addedAt: Long = System.currentTimeMillis()
) {
    /**
     * Secondary constructor matching: name, price, description, and category.
     */
    @Ignore
    constructor(
        name: String,
        price: Double,
        description: String,
        category: String
    ) : this(
        id = 0,
        name = name,
        price = price,
        description = description,
        category = category,
        isPurchased = false,
        quantity = 1,
        addedAt = System.currentTimeMillis()
    )

    /**
     * Secondary constructor matching: id, name, price, description, and category.
     */
    @Ignore
    constructor(
        id: Int,
        name: String,
        price: Double,
        description: String,
        category: String
    ) : this(
        id = id,
        name = name,
        price = price,
        description = description,
        category = category,
        isPurchased = false,
        quantity = 1,
        addedAt = System.currentTimeMillis()
    )

    /**
     * Secondary constructor supporting Long ID.
     */
    @Ignore
    constructor(
        id: Long,
        name: String,
        price: Double,
        description: String,
        category: String
    ) : this(
        id = id.toInt(),
        name = name,
        price = price,
        description = description,
        category = category,
        isPurchased = false,
        quantity = 1,
        addedAt = System.currentTimeMillis()
    )

    /**
     * Secondary constructor supporting String ID.
     */
    @Ignore
    constructor(
        id: String,
        name: String,
        price: Double,
        description: String,
        category: String
    ) : this(
        id = id.toIntOrNull() ?: 0,
        name = name,
        price = price,
        description = description,
        category = category,
        isPurchased = false,
        quantity = 1,
        addedAt = System.currentTimeMillis()
    )

    /**
     * Secondary constructor matching: name, price, and description (category defaults to "General").
     */
    @Ignore
    constructor(
        name: String,
        price: Double,
        description: String
    ) : this(
        id = 0,
        name = name,
        price = price,
        description = description,
        category = "General"
    )

    val priceInInr: Double
        get() = price

    val formattedPriceInInr: String
        get() = "₹%,.0f".format(price)

    val totalPrice: Double
        get() = price * quantity
}
