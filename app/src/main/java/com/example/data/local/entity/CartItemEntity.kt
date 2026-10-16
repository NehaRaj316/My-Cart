package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "cart_item_entities",
    primaryKeys = ["productId", "selectedColorHex"],
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["productId"])
    ]
)
data class CartItemEntity(
    val productId: String,
    val selectedColorHex: Long,
    val quantity: Int = 1,
    val addedAtTimestamp: Long = System.currentTimeMillis()
)
