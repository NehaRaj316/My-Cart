package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.dao.CartDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.entity.CartItemEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.model.Product
import com.example.data.repository.ShoppingRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoomDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var productDao: ProductDao
    private lateinit var cartDao: CartDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        productDao = db.productDao()
        cartDao = db.cartDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetProducts() = runBlocking {
        val product = ProductEntity(
            id = "test_1",
            title = "Test Saree",
            subtitle = "Pure Silk",
            category = "Fashion",
            price = 2999.0,
            description = "High quality silk",
            specifications = listOf("Fabric" to "Silk", "Care" to "Dry Clean"),
            availableColors = listOf(0xFF15803D, 0xFFB91C1C)
        )

        productDao.insertProduct(product)
        val loaded = productDao.getProductByIdSync("test_1")

        assertNotNull(loaded)
        assertEquals("Test Saree", loaded?.title)
        assertEquals(2999.0, loaded?.price ?: 0.0, 0.001)
        assertEquals(2, loaded?.specifications?.size)
        assertEquals("Fabric", loaded?.specifications?.first()?.first)
        assertEquals("Silk", loaded?.specifications?.first()?.second)
        assertEquals(2, loaded?.availableColors?.size)
    }

    @Test
    fun insertAndGetCartItemWithProductRelation() = runBlocking {
        val product = ProductEntity(
            id = "test_groceries",
            title = "Basmati Rice 5kg",
            subtitle = "Aged Royal Rice",
            category = "Groceries",
            price = 1200.0,
            description = "Fragrant long grain rice"
        )
        productDao.insertProduct(product)

        val cartItem = CartItemEntity(
            productId = "test_groceries",
            selectedColorHex = 0xFFD97706,
            quantity = 3
        )
        cartDao.insertCartItem(cartItem)

        val items = cartDao.getCartItemsWithProductSync()
        assertEquals(1, items.size)
        assertEquals("test_groceries", items[0].product.id)
        assertEquals("Basmati Rice 5kg", items[0].product.title)
        assertEquals(3, items[0].cartItem.quantity)
        assertEquals(0xFFD97706, items[0].cartItem.selectedColorHex)

        // Convert to domain model and verify
        val domainItem = items[0].toDomain()
        assertEquals(3, domainItem.quantity)
        assertEquals(3600.0, domainItem.totalPrice, 0.001)
    }

    @Test
    fun updateCartItemQuantityAndDelete() = runBlocking {
        val product = ProductEntity(
            id = "test_phone",
            title = "Flagship Phone",
            subtitle = "5G Smartphone",
            category = "Tech",
            price = 39999.0,
            description = "Flagship specs"
        )
        productDao.insertProduct(product)

        val cartItem = CartItemEntity(
            productId = "test_phone",
            selectedColorHex = 0xFF000000,
            quantity = 1
        )
        cartDao.insertCartItem(cartItem)

        // Update quantity to 4
        cartDao.updateQuantity("test_phone", 0xFF000000, 4)
        val loaded = cartDao.getCartItem("test_phone", 0xFF000000)
        assertNotNull(loaded)
        assertEquals(4, loaded?.quantity)

        // Delete item
        cartDao.deleteCartItem("test_phone", 0xFF000000)
        val deleted = cartDao.getCartItem("test_phone", 0xFF000000)
        assertNull(deleted)
    }

    @Test
    fun clearCartRemovesAllCartItems() = runBlocking {
        val p1 = ProductEntity(id = "p1", title = "P1", subtitle = "", category = "Tech", price = 100.0, description = "")
        val p2 = ProductEntity(id = "p2", title = "P2", subtitle = "", category = "Tech", price = 200.0, description = "")
        productDao.insertProducts(listOf(p1, p2))

        cartDao.insertCartItem(CartItemEntity(productId = "p1", selectedColorHex = 0L, quantity = 2))
        cartDao.insertCartItem(CartItemEntity(productId = "p2", selectedColorHex = 1L, quantity = 1))

        assertEquals(2, cartDao.getCartItemCount())
        cartDao.clearCart()
        assertEquals(0, cartDao.getCartItemCount())
    }

    @Test
    fun cascadeDeleteRemovesCartItemWhenProductDeleted() = runBlocking {
        val product = ProductEntity(
            id = "p_cascade",
            title = "Temp Product",
            subtitle = "",
            category = "Fashion",
            price = 500.0,
            description = ""
        )
        productDao.insertProduct(product)
        cartDao.insertCartItem(CartItemEntity(productId = "p_cascade", selectedColorHex = 0L, quantity = 1))

        assertEquals(1, cartDao.getCartItemCount())

        // Delete parent product
        productDao.deleteProductById("p_cascade")

        // Foreign key cascade should delete cart item
        val remainingCartItems = cartDao.getCartItemsWithProductSync()
        assertTrue("Cart item should be removed via cascade delete", remainingCartItems.isEmpty())
    }
}
