package com.example.data.local.entity

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Product

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val price: Double,
    val originalPrice: Double? = null,
    val rating: Float = 4.8f,
    val reviewsCount: Int = 128,
    val badge: String? = null,
    val description: String,
    val specifications: List<Pair<String, String>> = emptyList(),
    val availableColors: List<Long> = emptyList(),
    val inStock: Boolean = true,
    val estimatedDeliveryDays: Int = 1,
    val recommendationScore: Int = 95,
    val recommendationReason: String = "",
    val primaryColorHex: Long = 0xFF4F46E5,
    @DrawableRes val imageRes: Int? = null
) {
    fun toDomain(): Product = Product(
        id = id,
        title = title,
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
            imageRes = product.imageRes
        )
    }
}

fun Product.toEntity(): ProductEntity = ProductEntity.fromDomain(this)
