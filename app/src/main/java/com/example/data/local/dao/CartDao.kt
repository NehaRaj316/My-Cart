package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.CartItemEntity
import com.example.data.local.entity.CartItemWithProduct
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Transaction
    @Query("SELECT * FROM cart_items ORDER BY addedAtTimestamp ASC")
    fun getCartItemsWithProduct(): Flow<List<CartItemWithProduct>>

    @Transaction
    @Query("SELECT * FROM cart_items ORDER BY addedAtTimestamp ASC")
    suspend fun getCartItemsWithProductSync(): List<CartItemWithProduct>

    @Query("SELECT * FROM cart_items WHERE productId = :productId AND selectedColorHex = :selectedColorHex LIMIT 1")
    suspend fun getCartItem(productId: String, selectedColorHex: Long): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE productId = :productId AND selectedColorHex = :selectedColorHex")
    suspend fun updateQuantity(productId: String, selectedColorHex: Long, quantity: Int)

    @Query("DELETE FROM cart_items WHERE productId = :productId AND selectedColorHex = :selectedColorHex")
    suspend fun deleteCartItem(productId: String, selectedColorHex: Long)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM cart_items")
    fun getTotalCartQuantity(): Flow<Int>

    @Query("SELECT COUNT(*) FROM cart_items")
    suspend fun getCartItemCount(): Int
}
