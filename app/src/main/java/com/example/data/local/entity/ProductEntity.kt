package com.example.data.local.entity

import androidx.annotation.DrawableRes
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.data.model.Product
import java.util.UUID

/**
 * Room @Entity data class representing a product stored in the local SQLite database.
 * Stores product data including names, prices in INR, and descriptions.
 */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    @ColumnInfo(name = "name")
    val name: String = title,
    val subtitle: String = "",
    val category: String = "General",
    @ColumnInfo(name = "price")
    val price: Double = 0.0,
    val originalPrice: Double? = null,
    val rating: Float = 4.8f,
    val reviewsCount: Int = 128,
    val badge: String? = null,
    @ColumnInfo(name = "description")
    val description: String = "",
    val specifications: List<Pair<String, String>> = emptyList(),
    val availableColors: List<Long> = emptyList(),
    val inStock: Boolean = true,
    val estimatedDeliveryDays: Int = 1,
    val recommendationScore: Int = 95,
    val recommendationReason: String = "",
    val primaryColorHex: Long = 0xFF4F46E5,
    @DrawableRes val imageRes: Int? = null,
    val currency: String = "INR"
) {
    /**
     * Secondary constructor supporting the core prompt requirements:
     * name, price in INR, and description.
     */
    @Ignore
    constructor(
        name: String,
        price: Double,
        description: String
    ) : this(
        id = UUID.randomUUID().toString(),
        title = name,
        name = name,
        subtitle = "",
        category = "General",
        price = price,
        description = description
    )

    /**
     * Secondary constructor supporting id, name, price in INR, and description.
     */
    @Ignore
    constructor(
        id: String,
        name: String,
        price: Double,
        description: String
    ) : this(
        id = id,
        title = name,
        name = name,
        subtitle = "",
        category = "General",
        price = price,
        description = description
    )

    /**
     * Secondary constructor supporting integer id, name, price, and description.
     */
    @Ignore
    constructor(
        id: Int,
        name: String,
        price: Double,
        description: String
    ) : this(
        id = id.toString(),
        title = name,
        name = name,
        subtitle = "",
        category = "General",
        price = price,
        description = description
    )

    val priceInInr: Double
        get() = price

    val formattedPriceInInr: String
        get() = "₹%,.0f".format(price)

    fun toDomain(): Product = Product(
        id = id,
        title = if (title.isNotEmpty()) title else name,
        subtitle = subtitle,
        category = category,
        price = price,
        originalPrice = originalPrice,
        rating = rating,
        reviewsCount = reviewsCount,
        badge = badge,
        description = description,
        specifications = specifications,
        availableColors = availableColors,
        inStock = inStock,
        estimatedDeliveryDays = estimatedDeliveryDays,
        recommendationScore = recommendationScore,
        recommendationReason = recommendationReason,
        primaryColorHex = primaryColorHex,
        imageRes = imageRes
    )

    companion object {
        fun fromDomain(product: Product): ProductEntity = ProductEntity(
            id = product.id,
            title = product.title,
            name = product.title,
            subtitle = product.subtitle,
            category = product.category,
            price = product.price,
            originalPrice = product.originalPrice,
            rating = product.rating,
            reviewsCount = product.reviewsCount,
            badge = product.badge,
            description = product.description,
            specifications = product.specifications,
            availableColors = product.availableColors,
            inStock = product.inStock,
            estimatedDeliveryDays = product.estimatedDeliveryDays,
            recommendationScore = product.recommendationScore,
            recommendationReason = product.recommendationReason,
            primaryColorHex = product.primaryColorHex,
            imageRes = product.imageRes,
            currency = "INR"
        )
    }
}

fun Product.toEntity(): ProductEntity = ProductEntity.fromDomain(this)
