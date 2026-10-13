package com.example.data.model

import androidx.annotation.DrawableRes

data class Product(
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
    val availableColors: List<Long> = listOf(0xFF1E293B, 0xFF4F46E5, 0xFF06B6D4),
    val inStock: Boolean = true,
    val estimatedDeliveryDays: Int = 1,
    val recommendationScore: Int = 95,
    val recommendationReason: String = "High affinity match based on your preferences",
    val primaryColorHex: Long = 0xFF4F46E5,
    @DrawableRes val imageRes: Int? = null
)
