package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.CartItem
import com.example.data.local.entity.CartItemEntity
import com.example.data.local.entity.CartItemWithProduct
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    // --- CartItemEntity operations (with Product relation) ---

    @Transaction
    @Query("SELECT * FROM cart_item_entities ORDER BY addedAtTimestamp ASC")
    fun getCartItemsWithProduct(): Flow<List<CartItemWithProduct>>

    @Transaction
    @Query("SELECT * FROM cart_item_entities ORDER BY addedAtTimestamp ASC")
    suspend fun getCartItemsWithProductSync(): List<CartItemWithProduct>

    @Query("SELECT * FROM cart_item_entities WHERE productId = :productId AND selectedColorHex = :selectedColorHex LIMIT 1")
    suspend fun getCartItem(productId: String, selectedColorHex: Long): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItemEntity)

    @Query("UPDATE cart_item_entities SET quantity = :quantity WHERE productId = :productId AND selectedColorHex = :selectedColorHex")
    suspend fun updateQuantity(productId: String, selectedColorHex: Long, quantity: Int)

    @Query("DELETE FROM cart_item_entities WHERE productId = :productId AND selectedColorHex = :selectedColorHex")
    suspend fun deleteCartItem(productId: String, selectedColorHex: Long)

    @Query("DELETE FROM cart_item_entities")
    suspend fun clearCart()

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM cart_item_entities")
    fun getTotalCartQuantity(): Flow<Int>

    @Query("SELECT COUNT(*) FROM cart_item_entities")
    suspend fun getCartItemCount(): Int

    // --- CartItem Room @Entity operations (id, name, price, quantity) ---

    @Query("SELECT * FROM cart_items ORDER BY id ASC")
    fun getAllCartItems(): Flow<List<CartItem>>

    @Query("SELECT * FROM cart_items ORDER BY id ASC")
    suspend fun getAllCartItemsSync(): List<CartItem>

    @Query("SELECT * FROM cart_items WHERE id = :id LIMIT 1")
    suspend fun getCartItemById(id: Int): CartItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cartItem: CartItem): Long

    @Update
    suspend fun update(cartItem: CartItem)

    @Delete
    suspend fun delete(cartItem: CartItem)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItemById(id: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearAllCartItems()
}
