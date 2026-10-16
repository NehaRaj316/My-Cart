package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.dao.CartDao
import com.example.data.local.dao.CartItemDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.entity.CartItem
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
    private lateinit var cartItemDao: CartItemDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        productDao = db.productDao()
        cartDao = db.cartDao()
        cartItemDao = db.cartItemDao()
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

    @Test
    fun cartItemEntityStoresIdNamePriceAndQuantity() = runBlocking {
        val cartItem = CartItem(
            name = "Royal Banarasi Silk Saree",
            price = 3499.0,
            quantity = 2
        )
        assertEquals("Royal Banarasi Silk Saree", cartItem.name)
        assertEquals(3499.0, cartItem.price, 0.001)
        assertEquals(2, cartItem.quantity)
        assertEquals(6998.0, cartItem.totalPrice, 0.001)

        val insertedId = cartDao.insert(cartItem)
        assertTrue("Auto-generated ID should be greater than 0", insertedId > 0)

        val loaded = cartDao.getCartItemById(insertedId.toInt())
        assertNotNull("Loaded cart item should not be null", loaded)
        assertEquals("Royal Banarasi Silk Saree", loaded?.name)
        assertEquals(3499.0, loaded?.price ?: 0.0, 0.001)
        assertEquals(2, loaded?.quantity)

        // Test update
        val updated = loaded!!.copy(quantity = 3)
        cartDao.update(updated)

        val reloaded = cartDao.getCartItemById(insertedId.toInt())
        assertEquals(3, reloaded?.quantity)
        assertEquals(10497.0, reloaded?.totalPrice ?: 0.0, 0.001)

        // Test delete
        cartDao.delete(reloaded!!)
        val deleted = cartDao.getCartItemById(insertedId.toInt())
        assertNull("Cart item should be deleted", deleted)
    }

    @Test
    fun cartItemSecondaryConstructorsSupportVariousIdTypes() {
        val fromInt = CartItem(id = 10, name = "Basmati Rice", price = 149.0, quantity = 3)
        assertEquals(10, fromInt.id)
        assertEquals("Basmati Rice", fromInt.name)
        assertEquals(149.0, fromInt.price, 0.001)
        assertEquals(3, fromInt.quantity)

        val fromLong = CartItem(id = 20L, name = "Ghee", price = 499.0, quantity = 1)
        assertEquals(20, fromLong.id)
        assertEquals("Ghee", fromLong.name)

        val fromString = CartItem(id = "30", name = "Masala Chai", price = 79.0, quantity = 4)
        assertEquals(30, fromString.id)
        assertEquals("Masala Chai", fromString.name)
    }

    @Test
    fun cartItemDaoInsertAndQuery() = runBlocking {
        // Clear before testing CartItemDao
        cartItemDao.deleteAll()
        assertEquals(0, cartItemDao.getCartItemCount())

        val item1 = CartItem(name = "Kashmiri Kahwa Tea", price = 249.0, quantity = 2)
        val id1 = cartItemDao.insert(item1)
        assertTrue("Generated ID should be > 0", id1 > 0)

        val item2 = CartItem(name = "Organic Turmeric Powder", price = 120.0, quantity = 3)
        val id2 = cartItemDao.insertCartItem(item2)
        assertTrue("Generated ID should be > 0", id2 > 0)

        // Query by ID
        val queried1 = cartItemDao.getCartItemById(id1.toInt())
        assertNotNull(queried1)
        assertEquals("Kashmiri Kahwa Tea", queried1?.name)
        assertEquals(249.0, queried1?.price ?: 0.0, 0.001)
        assertEquals(2, queried1?.quantity)

        // Query by name
        val queriedByName = cartItemDao.getCartItemByName("Organic Turmeric Powder")
        assertNotNull(queriedByName)
        assertEquals(id2.toInt(), queriedByName?.id)

        // Query all items
        val allItems = cartItemDao.getAllCartItemsSync()
        assertEquals(2, allItems.size)
        assertEquals(2, cartItemDao.getCartItemCount())

        // Query total quantity and total price
        assertEquals(5, cartItemDao.getTotalQuantitySync())
        // Total price: (249 * 2) + (120 * 3) = 498 + 360 = 858
        assertEquals(858.0, cartItemDao.getTotalPriceSync(), 0.001)

        // Query via Flow
        val flowItems = cartItemDao.getAllCartItems().first()
        assertEquals(2, flowItems.size)
    }

    @Test
    fun cartItemDaoUpdateOperations() = runBlocking {
        cartItemDao.deleteAll()
        val id = cartItemDao.insert(CartItem(name = "Basmati Rice 5kg", price = 650.0, quantity = 1))

        // Update quantity via query method
        cartItemDao.updateQuantity(id.toInt(), 4)
        val updatedQty = cartItemDao.getCartItemById(id.toInt())
        assertEquals(4, updatedQty?.quantity)
        assertEquals(2600.0, cartItemDao.getTotalPriceSync(), 0.001)

        // Update entity via @Update
        cartItemDao.update(updatedQty!!.copy(name = "Premium Basmati Rice 5kg", price = 700.0))
        val updatedEntity = cartItemDao.getCartItemById(id.toInt())
        assertEquals("Premium Basmati Rice 5kg", updatedEntity?.name)
        assertEquals(700.0, updatedEntity?.price ?: 0.0, 0.001)
        assertEquals(2800.0, cartItemDao.getTotalPriceSync(), 0.001)
    }

    @Test
    fun cartItemDaoDeleteOperations() = runBlocking {
        cartItemDao.deleteAll()
        val id1 = cartItemDao.insert(CartItem(name = "Item 1", price = 10.0, quantity = 1))
        val id2 = cartItemDao.insert(CartItem(name = "Item 2", price = 20.0, quantity = 2))
        val id3 = cartItemDao.insert(CartItem(name = "Item 3", price = 30.0, quantity = 3))
        assertEquals(3, cartItemDao.getCartItemCount())

        // Delete by entity
        val item1 = cartItemDao.getCartItemById(id1.toInt())
        assertNotNull(item1)
        cartItemDao.delete(item1!!)
        assertNull(cartItemDao.getCartItemById(id1.toInt()))
        assertEquals(2, cartItemDao.getCartItemCount())

        // Delete by ID
        cartItemDao.deleteById(id2.toInt())
        assertNull(cartItemDao.getCartItemById(id2.toInt()))
        assertEquals(1, cartItemDao.getCartItemCount())

        // Delete all
        cartItemDao.deleteAll()
        assertEquals(0, cartItemDao.getCartItemCount())
        assertTrue(cartItemDao.getAllCartItemsSync().isEmpty())
    }
}
