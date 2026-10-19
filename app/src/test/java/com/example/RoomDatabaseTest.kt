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

    @Test
    fun storeAndRetrieveProductWithNamePriceInInrAndDescription() = runBlocking {
        productDao.deleteAll()
        assertEquals(0, productDao.getProductCount())

        // Create product with name, price in INR, and description
        val product = ProductEntity(
            name = "Pure Katan Banarasi Saree",
            price = 3499.0, // In INR
            description = "Hand-woven Banarasi silk with exquisite golden floral zari work"
        )

        assertEquals("Pure Katan Banarasi Saree", product.name)
        assertEquals(3499.0, product.price, 0.001)
        assertEquals(3499.0, product.priceInInr, 0.001)
        assertEquals("INR", product.currency)
        assertEquals("₹3,499", product.formattedPriceInInr)
        assertTrue(product.description.contains("Banarasi silk"))

        // Store in database
        val rowId = productDao.insert(product)
        assertTrue("SQLite rowid should be > 0", rowId > 0)
        assertEquals(1, productDao.getProductCount())

        // Retrieve by ID
        val retrievedById = productDao.getProductByIdSync(product.id)
        assertNotNull(retrievedById)
        assertEquals("Pure Katan Banarasi Saree", retrievedById?.name)
        assertEquals(3499.0, retrievedById?.priceInInr ?: 0.0, 0.001)
        assertEquals("INR", retrievedById?.currency)
        assertEquals("Hand-woven Banarasi silk with exquisite golden floral zari work", retrievedById?.description)

        // Retrieve by name
        val retrievedByName = productDao.getProductByName("Pure Katan Banarasi Saree")
        assertNotNull(retrievedByName)
        assertEquals(product.id, retrievedByName?.id)
        assertEquals(3499.0, retrievedByName?.price ?: 0.0, 0.001)

        // Retrieve via Flow
        val flowProducts = productDao.getAllProducts().first()
        assertEquals(1, flowProducts.size)
        assertEquals("Pure Katan Banarasi Saree", flowProducts[0].name)
    }

    @Test
    fun retrieveProductsByInrPriceRangeAndSorting() = runBlocking {
        productDao.deleteAll()

        val p1 = ProductEntity(name = "Organic Turmeric Powder", price = 150.0, description = "Freshly ground Lakadong turmeric")
        val p2 = ProductEntity(name = "A2 Vedic Cow Bilona Ghee", price = 750.0, description = "Pure grass-fed cow ghee")
        val p3 = ProductEntity(name = "Royal Basmati Rice 5kg", price = 1200.0, description = "Extra long aged aromatic grain")
        val p4 = ProductEntity(name = "AeroSound ANC Headphones", price = 4999.0, description = "Active noise cancelling wireless")

        productDao.insertAll(p1, p2, p3, p4)
        assertEquals(4, productDao.getProductCount())

        // Query products between ₹500 and ₹2000
        val midRange = productDao.getProductsByPriceRangeSync(500.0, 2000.0)
        assertEquals(2, midRange.size)
        assertEquals("A2 Vedic Cow Bilona Ghee", midRange[0].name)
        assertEquals("Royal Basmati Rice 5kg", midRange[1].name)

        // Query products under ₹1000
        val under1000 = productDao.getProductsUnderPriceSync(1000.0)
        assertEquals(2, under1000.size)
        assertEquals("Organic Turmeric Powder", under1000[0].name)
        assertEquals("A2 Vedic Cow Bilona Ghee", under1000[1].name)

        // Query products ordered by price in INR ascending
        val sortedAsc = productDao.getProductsOrderedByPriceAsc().first()
        assertEquals(150.0, sortedAsc[0].price, 0.001)
        assertEquals(4999.0, sortedAsc[3].price, 0.001)

        // Query products ordered by price in INR descending
        val sortedDesc = productDao.getProductsOrderedByPriceDesc().first()
        assertEquals(4999.0, sortedDesc[0].price, 0.001)
        assertEquals(150.0, sortedDesc[3].price, 0.001)
    }

    @Test
    fun searchProductsByNameAndDescription() = runBlocking {
        productDao.deleteAll()

        val p1 = ProductEntity(name = "Zenith AI Laptop", price = 68999.0, description = "Ultra slim Intel Core Ultra 7 with OLED display")
        val p2 = ProductEntity(name = "Phantom 5G Smartphone", price = 42999.0, description = "Snapdragon 8 Gen 3 with 50MP Sony camera")
        val p3 = ProductEntity(name = "Laptop Protective Sleeve", price = 999.0, description = "Water-resistant padded neoprene sleeve for 16-inch laptops")

        productDao.insertProducts(listOf(p1, p2, p3))

        // Search by name query "Laptop"
        val laptopNameMatches = productDao.searchProductsByNameSync("Laptop")
        assertEquals(2, laptopNameMatches.size)

        // Full search matching keyword in description: "Sony"
        val sonySearch = productDao.searchProductsSync("Sony")
        assertEquals(1, sonySearch.size)
        assertEquals("Phantom 5G Smartphone", sonySearch[0].name)

        // Full search matching keyword in description: "OLED"
        val oledSearch = productDao.searchProductsSync("OLED")
        assertEquals(1, oledSearch.size)
        assertEquals("Zenith AI Laptop", oledSearch[0].name)
    }

    @Test
    fun updateProductPriceInInrAndDescription() = runBlocking {
        productDao.deleteAll()

        val product = ProductEntity(id = "p_update", name = "Smart Fitness Band", price = 1999.0, description = "Heart rate and sleep tracker")
        productDao.insertProduct(product)

        // Update price in INR
        productDao.updatePrice("p_update", 1499.0)
        val updatedPrice = productDao.getProductByIdSync("p_update")
        assertEquals(1499.0, updatedPrice?.priceInInr ?: 0.0, 0.001)
        assertEquals(1499.0, updatedPrice?.price ?: 0.0, 0.001)

        // Update description
        productDao.updateDescription("p_update", "Updated with SPO2 blood oxygen tracking")
        val updatedDesc = productDao.getProductByIdSync("p_update")
        assertEquals("Updated with SPO2 blood oxygen tracking", updatedDesc?.description)

        // Update entire entity
        productDao.update(updatedDesc!!.copy(name = "Smart Fitness Band Pro", price = 1799.0))
        val reloaded = productDao.getProductByIdSync("p_update")
        assertEquals("Smart Fitness Band Pro", reloaded?.name)
        assertEquals(1799.0, reloaded?.priceInInr ?: 0.0, 0.001)

        // Delete
        productDao.delete(reloaded!!)
        assertNull(productDao.getProductByIdSync("p_update"))
        assertEquals(0, productDao.getProductCount())
    }

    @Test
    fun retrieveProductsForGridWithFormattedNamesAndInrPrices() = runBlocking {
        productDao.deleteAll()

        val products = listOf(
            ProductEntity(id = "grid_1", name = "Banarasi Silk Saree", price = 3499.0, description = "Traditional pure silk"),
            ProductEntity(id = "grid_2", name = "A2 Cow Bilona Ghee", price = 1499.0, description = "Vedic organic ghee"),
            ProductEntity(id = "grid_3", name = "Zenith AI Ultrabook", price = 68999.0, description = "Intel Core Ultra 7"),
            ProductEntity(id = "grid_4", name = "Phantom 5G Smartphone", price = 42999.0, description = "Snapdragon 8 Gen 3")
        )

        productDao.insertProducts(products)

        val retrieved = productDao.getAllProducts().first()
        assertEquals(4, retrieved.size)

        // Verify product names
        assertEquals("Banarasi Silk Saree", retrieved[0].name)
        assertEquals("A2 Cow Bilona Ghee", retrieved[1].name)
        assertEquals("Zenith AI Ultrabook", retrieved[2].name)
        assertEquals("Phantom 5G Smartphone", retrieved[3].name)

        // Verify INR prices formatted with Rupee symbol
        assertEquals("₹3,499", com.example.util.CurrencyUtil.formatRupees(retrieved[0].price))
        assertEquals("₹1,499", com.example.util.CurrencyUtil.formatRupees(retrieved[1].price))
        assertEquals("₹68,999", com.example.util.CurrencyUtil.formatRupees(retrieved[2].price))
        assertEquals("₹42,999", com.example.util.CurrencyUtil.formatRupees(retrieved[3].price))

        // Verify entity's formattedPriceInInr helper
        assertEquals("₹3,499", retrieved[0].formattedPriceInInr)
        assertEquals("₹1,499", retrieved[1].formattedPriceInInr)
    }
}

