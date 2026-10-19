package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) interface for performing Room database operations
 * on [ProductEntity] (products catalog).
 *
 * Provides methods to store and retrieve product data, including names, prices in INR,
 * and descriptions:
 * - Insert operations (single, batch, varargs)
 * - Retrieve operations by ID, name, price range in INR, and description search
 * - Update operations (full entity, price in INR, description)
 * - Delete operations (single, by ID, clear all)
 */
@Dao
interface ProductDao {

    // --- Insert / Store Operations ---

    /**
     * Inserts a product into the database. Replaces on conflict.
     * @return The SQLite rowid of the inserted product.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    /**
     * Alias for [insertProduct].
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity): Long

    /**
     * Inserts a list of products into the database.
     * @return The list of SQLite rowids for the inserted products.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>): List<Long>

    /**
     * Alias for [insertProducts].
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>): List<Long>

    /**
     * Inserts multiple products using varargs.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg products: ProductEntity)

    // --- Retrieve / Query Operations ---

    /**
     * Observes all products in the database ordered by ID.
     */
    @Query("SELECT * FROM products ORDER BY id ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    /**
     * Synchronously fetches all products.
     */
    @Query("SELECT * FROM products ORDER BY id ASC")
    suspend fun getAllProductsSync(): List<ProductEntity>

    /**
     * Alias for [getAllProductsSync].
     */
    @Query("SELECT * FROM products ORDER BY id ASC")
    suspend fun getProducts(): List<ProductEntity>

    /**
     * Observes a single product by its primary key ID.
     */
    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    fun getProductById(id: String): Flow<ProductEntity?>

    /**
     * Alias for [getProductById].
     */
    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    fun getProductByIdFlow(id: String): Flow<ProductEntity?>

    /**
     * Synchronously fetches a product by its primary key ID.
     */
    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductByIdSync(id: String): ProductEntity?

    // Query by Name

    /**
     * Retrieves a single product matching the given name or title.
     */
    @Query("SELECT * FROM products WHERE name = :name OR title = :name LIMIT 1")
    suspend fun getProductByName(name: String): ProductEntity?

    /**
     * Observes products matching the given name or title.
     */
    @Query("SELECT * FROM products WHERE name = :name OR title = :name")
    fun getProductsByName(name: String): Flow<List<ProductEntity>>

    /**
     * Searches products whose name or title contains the query substring.
     */
    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchProductsByName(query: String): Flow<List<ProductEntity>>

    /**
     * Synchronously searches products by name.
     */
    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun searchProductsByNameSync(query: String): List<ProductEntity>

    // Query by Description & Full Search

    /**
     * Retrieves products matching keywords in their description.
     */
    @Query("SELECT * FROM products WHERE description LIKE '%' || :query || '%'")
    fun getProductsByDescription(query: String): Flow<List<ProductEntity>>

    /**
     * Performs a comprehensive search matching against name, title, description, or category.
     */
    @Query("SELECT * FROM products WHERE name LIKE '%' || :keyword || '%' OR title LIKE '%' || :keyword || '%' OR description LIKE '%' || :keyword || '%' OR category LIKE '%' || :keyword || '%' ORDER BY price ASC")
    fun searchProducts(keyword: String): Flow<List<ProductEntity>>

    /**
     * Synchronous search against name, title, description, or category.
     */
    @Query("SELECT * FROM products WHERE name LIKE '%' || :keyword || '%' OR title LIKE '%' || :keyword || '%' OR description LIKE '%' || :keyword || '%' OR category LIKE '%' || :keyword || '%' ORDER BY price ASC")
    suspend fun searchProductsSync(keyword: String): List<ProductEntity>

    // Query by Price in INR

    /**
     * Observes products within an INR price range (minPrice to maxPrice).
     */
    @Query("SELECT * FROM products WHERE price BETWEEN :minPrice AND :maxPrice ORDER BY price ASC")
    fun getProductsByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<ProductEntity>>

    /**
     * Synchronously fetches products within an INR price range.
     */
    @Query("SELECT * FROM products WHERE price BETWEEN :minPrice AND :maxPrice ORDER BY price ASC")
    suspend fun getProductsByPriceRangeSync(minPrice: Double, maxPrice: Double): List<ProductEntity>

    /**
     * Observes products under or equal to a specified maximum INR price.
     */
    @Query("SELECT * FROM products WHERE price <= :maxPrice ORDER BY price ASC")
    fun getProductsUnderPrice(maxPrice: Double): Flow<List<ProductEntity>>

    /**
     * Synchronously fetches products under or equal to a specified maximum INR price.
     */
    @Query("SELECT * FROM products WHERE price <= :maxPrice ORDER BY price ASC")
    suspend fun getProductsUnderPriceSync(maxPrice: Double): List<ProductEntity>

    /**
     * Observes all products sorted by price in INR ascending (lowest first).
     */
    @Query("SELECT * FROM products ORDER BY price ASC")
    fun getProductsOrderedByPriceAsc(): Flow<List<ProductEntity>>

    /**
     * Observes all products sorted by price in INR descending (highest first).
     */
    @Query("SELECT * FROM products ORDER BY price DESC")
    fun getProductsOrderedByPriceDesc(): Flow<List<ProductEntity>>

    // Query by Category

    /**
     * Observes products in a specific category.
     */
    @Query("SELECT * FROM products WHERE category = :category ORDER BY id ASC")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    /**
     * Synchronously fetches products in a specific category.
     */
    @Query("SELECT * FROM products WHERE category = :category ORDER BY id ASC")
    suspend fun getProductsByCategorySync(category: String): List<ProductEntity>

    // Count

    /**
     * Gets the total count of products in the database.
     */
    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    /**
     * Observes the total count of products in the database.
     */
    @Query("SELECT COUNT(*) FROM products")
    fun getProductCountFlow(): Flow<Int>

    // --- Update Operations ---

    /**
     * Updates an existing product.
     */
    @Update
    suspend fun updateProduct(product: ProductEntity)

    /**
     * Alias for [updateProduct].
     */
    @Update
    suspend fun update(product: ProductEntity)

    /**
     * Updates the INR price of a product by ID.
     */
    @Query("UPDATE products SET price = :priceInInr WHERE id = :id")
    suspend fun updatePrice(id: String, priceInInr: Double)

    /**
     * Updates the description of a product by ID.
     */
    @Query("UPDATE products SET description = :description WHERE id = :id")
    suspend fun updateDescription(id: String, description: String)

    // --- Delete Operations ---

    /**
     * Deletes a product entity.
     */
    @Delete
    suspend fun delete(product: ProductEntity)

    /**
     * Alias for [delete].
     */
    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    /**
     * Deletes a product by its primary key ID.
     */
    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: String)

    /**
     * Alias for [deleteProductById].
     */
    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * Deletes all products from the database.
     */
    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    /**
     * Alias for [deleteAllProducts].
     */
    @Query("DELETE FROM products")
    suspend fun deleteAll()
}
