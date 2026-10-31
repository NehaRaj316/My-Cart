package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.Product
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) interface for offline shopping list products ([Product]).
 * Supports offline shopping list management with reactive Flow queries,
 * category filtering, purchasing state toggles, and batch operations.
 */
@Dao
interface OfflineProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg products: Product)

    @Query("SELECT * FROM shopping_list_products ORDER BY id ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM shopping_list_products ORDER BY id ASC")
    suspend fun getAllProductsSync(): List<Product>

    @Query("SELECT * FROM shopping_list_products WHERE id = :id LIMIT 1")
    fun getProductById(id: Int): Flow<Product?>

    @Query("SELECT * FROM shopping_list_products WHERE id = :id LIMIT 1")
    suspend fun getProductByIdSync(id: Int): Product?

    @Query("SELECT * FROM shopping_list_products WHERE name = :name LIMIT 1")
    suspend fun getProductByName(name: String): Product?

    @Query("SELECT * FROM shopping_list_products WHERE category = :category ORDER BY name ASC")
    fun getProductsByCategory(category: String): Flow<List<Product>>

    @Query("SELECT * FROM shopping_list_products WHERE category = :category ORDER BY name ASC")
    suspend fun getProductsByCategorySync(category: String): List<Product>

    @Query("SELECT * FROM shopping_list_products WHERE is_purchased = :isPurchased ORDER BY added_at DESC")
    fun getProductsByPurchasedState(isPurchased: Boolean): Flow<List<Product>>

    @Query("SELECT * FROM shopping_list_products WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<Product>>

    @Query("SELECT COUNT(*) FROM shopping_list_products")
    suspend fun getProductCount(): Int

    @Query("SELECT COUNT(*) FROM shopping_list_products")
    fun getProductCountFlow(): Flow<Int>

    @Update
    suspend fun updateProduct(product: Product)

    @Update
    suspend fun update(product: Product)

    @Query("UPDATE shopping_list_products SET is_purchased = :isPurchased WHERE id = :id")
    suspend fun updatePurchasedState(id: Int, isPurchased: Boolean)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Query("DELETE FROM shopping_list_products WHERE id = :id")
    suspend fun deleteProductById(id: Int)

    @Query("DELETE FROM shopping_list_products WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM shopping_list_products")
    suspend fun deleteAllProducts()

    @Query("DELETE FROM shopping_list_products")
    suspend fun deleteAll()
}
