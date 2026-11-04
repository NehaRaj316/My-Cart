package com.example.data.model

import java.util.UUID

data class ProductReview(
    val id: String = UUID.randomUUID().toString(),
    val productId: String,
    val userId: String,
    val userName: String,
    val rating: Int, // 1 to 5 stars
    val comment: String,
    val timestamp: Long = System.currentTimeMillis(),
    val verifiedPurchase: Boolean = true,
    val helpfulCount: Int = 0
)
