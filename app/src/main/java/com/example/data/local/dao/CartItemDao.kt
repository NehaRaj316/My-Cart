package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CartItem
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) interface for performing Room database operations
 * on [CartItem] entities (shopping cart items).
 *
 * Provides methods for:
 * - Inserting single and multiple cart items
 * - Deleting cart items by entity or by ID, and clearing all items
 * - Querying cart items (as Flow and suspend functions)
 * - Updating cart items and their quantities
 */
@Dao
interface CartItemDao {

    // --- Insert Operations ---

    /**
     * Inserts a cart item into the database. If a conflict occurs, replaces it.
     * @return The auto-generated row ID of the inserted item.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cartItem: CartItem): Long

    /**
     * Alias for [insert] to match different naming conventions.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItem): Long

    /**
     * Inserts a list of cart items into the database.
     * @return List of row IDs of the inserted items.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cartItems: List<CartItem>): List<Long>

    /**
     * Inserts multiple cart items into the database using varargs.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg cartItems: CartItem)

    // --- Delete Operations ---

    /**
     * Deletes a specific cart item from the database.
     */
    @Delete
    suspend fun delete(cartItem: CartItem)

    /**
     * Alias for [delete] to match different naming conventions.
     */
    @Delete
    suspend fun deleteCartItem(cartItem: CartItem)

    /**
     * Deletes a list of cart items.
     */
    @Delete
    suspend fun deleteCartItems(cartItems: List<CartItem>)

    /**
     * Deletes a cart item by its primary key ID.
     */
    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteById(id: Int)

    /**
     * Alias for [deleteById] with Int ID.
     */
    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItemById(id: Int)

    /**
     * Helper to delete by Long ID.
     */
    suspend fun deleteById(id: Long) {
        deleteById(id.toInt())
    }

    /**
     * Deletes all cart items from the database.
     */
    @Query("DELETE FROM cart_items")
    suspend fun deleteAll()

    /**
     * Alias for [deleteAll] to clear the cart table.
     */
    @Query("DELETE FROM cart_items")
    suspend fun clearCartItems()

    // --- Query Operations ---

    /**
     * Observes all cart items in the database ordered by their ID.
     */
    @Query("SELECT * FROM cart_items ORDER BY id ASC")
    fun getAllCartItems(): Flow<List<CartItem>>

    /**
     * Synchronously fetches all cart items in a coroutine.
     */
    @Query("SELECT * FROM cart_items ORDER BY id ASC")
    suspend fun getAllCartItemsSync(): List<CartItem>

    /**
     * Alias for [getAllCartItemsSync] or [getAllCartItems].
     */
    @Query("SELECT * FROM cart_items ORDER BY id ASC")
    suspend fun getCartItems(): List<CartItem>

    /**
     * Queries a specific cart item by ID as a [Flow].
     */
    @Query("SELECT * FROM cart_items WHERE id = :id LIMIT 1")
    fun getCartItemByIdFlow(id: Int): Flow<CartItem?>

    /**
     * Queries a specific cart item by ID synchronously.
     */
    @Query("SELECT * FROM cart_items WHERE id = :id LIMIT 1")
    suspend fun getCartItemById(id: Int): CartItem?

    /**
     * Helper overload to query by Long ID.
     */
    suspend fun getCartItemById(id: Long): CartItem? {
        return getCartItemById(id.toInt())
    }

    /**
     * Queries a cart item by its name.
     */
    @Query("SELECT * FROM cart_items WHERE name = :name LIMIT 1")
    suspend fun getCartItemByName(name: String): CartItem?

    /**
     * Gets the total number of distinct cart item rows.
     */
    @Query("SELECT COUNT(*) FROM cart_items")
    suspend fun getCartItemCount(): Int

    /**
     * Observes the total number of distinct cart items.
     */
    @Query("SELECT COUNT(*) FROM cart_items")
    fun getCartItemCountFlow(): Flow<Int>

    /**
     * Observes the total quantity of all items in the cart (sum of quantities).
     */
    @Query("SELECT COALESCE(SUM(quantity), 0) FROM cart_items")
    fun getTotalQuantity(): Flow<Int>

    /**
     * Gets the total quantity of all items in the cart synchronously.
     */
    @Query("SELECT COALESCE(SUM(quantity), 0) FROM cart_items")
    suspend fun getTotalQuantitySync(): Int

    /**
     * Observes the total cost of all cart items (price * quantity).
     */
    @Query("SELECT COALESCE(SUM(price * quantity), 0.0) FROM cart_items")
    fun getTotalPrice(): Flow<Double>

    /**
     * Gets the total cost of all cart items synchronously.
     */
    @Query("SELECT COALESCE(SUM(price * quantity), 0.0) FROM cart_items")
    suspend fun getTotalPriceSync(): Double

    // --- Update Operations ---

    /**
     * Updates an existing cart item in the database.
     */
    @Update
    suspend fun update(cartItem: CartItem)

    /**
     * Alias for [update].
     */
    @Update
    suspend fun updateCartItem(cartItem: CartItem)

    /**
     * Updates the quantity of a specific cart item by ID.
     */
    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: Int, quantity: Int)

    /**
     * Helper overload to update quantity with Long ID.
     */
    suspend fun updateQuantity(id: Long, quantity: Int) {
        updateQuantity(id.toInt(), quantity)
    }
}
