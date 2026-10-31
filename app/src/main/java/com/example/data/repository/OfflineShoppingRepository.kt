package com.example.data.repository

import com.example.data.local.dao.OfflineProductDao
import com.example.data.local.entity.Product
import kotlinx.coroutines.flow.Flow

/**
 * Repository providing access to offline shopping list products stored in Room.
 * Follows Clean Architecture / Repository pattern.
 */
class OfflineShoppingRepository(private val productDao: OfflineProductDao) {

    val shoppingList: Flow<List<Product>> = productDao.getAllProducts()

    suspend fun addProduct(product: Product): Long = productDao.insert(product)

    suspend fun addProducts(products: List<Product>): List<Long> = productDao.insertAll(products)

    suspend fun getProductById(id: Int): Product? = productDao.getProductByIdSync(id)

    fun observeProductById(id: Int): Flow<Product?> = productDao.getProductById(id)

    fun getProductsByCategory(category: String): Flow<List<Product>> =
        productDao.getProductsByCategory(category)

    fun getPendingItems(): Flow<List<Product>> = productDao.getProductsByPurchasedState(false)

    fun getPurchasedItems(): Flow<List<Product>> = productDao.getProductsByPurchasedState(true)

    fun search(query: String): Flow<List<Product>> = productDao.searchProducts(query)

    suspend fun updateProduct(product: Product) = productDao.update(product)

    suspend fun togglePurchased(id: Int, isPurchased: Boolean) =
        productDao.updatePurchasedState(id, isPurchased)

    suspend fun deleteProduct(id: Int) = productDao.deleteById(id)

    suspend fun deleteProduct(product: Product) = productDao.delete(product)

    suspend fun clearList() = productDao.deleteAll()
}
