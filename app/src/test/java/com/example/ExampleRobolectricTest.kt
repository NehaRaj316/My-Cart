package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.TastePreference
import com.example.data.model.UserPersona
import com.example.data.repository.ShoppingRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("My Cart", appName)
    }

    @Test
    fun `shopping repository has initial products and active tracking order`() {
        val repo = ShoppingRepository()
        val products = repo.products.value
        assertTrue("Catalog should contain items", products.isNotEmpty())

        val activeOrder = repo.activeOrder.value
        assertNotNull("Should have active order for tracking demo", activeOrder)
        assertEquals("Liam Walker", activeOrder?.courier?.name)
    }

    @Test
    fun `catalog contains indian ethnic clothes groceries laptop and smartphone with images`() {
        val repo = ShoppingRepository()
        val products = repo.products.value

        val ethnic = products.find { it.id == "prod_ethnic" }
        assertNotNull("Indian ethnic wear should be present", ethnic)
        assertNotNull("Ethnic wear should have image resource", ethnic?.imageRes)

        val groceries = products.find { it.id == "prod_groceries" }
        assertNotNull("Indian groceries should be present", groceries)
        assertNotNull("Groceries should have image resource", groceries?.imageRes)

        val laptop = products.find { it.id == "prod_laptop" }
        assertNotNull("Laptop should be present", laptop)
        assertNotNull("Laptop should have image resource", laptop?.imageRes)

        val smartphone = products.find { it.id == "prod_smartphone" }
        assertNotNull("Smartphone should be present", smartphone)
        assertNotNull("Smartphone should have image resource", smartphone?.imageRes)
    }

    @Test
    fun `recommendation engine recalculates scores on persona change`() {
        val repo = ShoppingRepository()
        repo.updateTastePreference(TastePreference(selectedPersona = UserPersona.INDIAN_ETHNIC))
        val products = repo.products.value
        val fashionItem = products.find { it.category == "Fashion" }
        assertNotNull(fashionItem)
        assertTrue(fashionItem!!.recommendationScore >= 85)
    }

    @Test
    fun `nehamycartapp com deep link intent filter resolves to MainActivity`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageManager = context.packageManager

        // Root url
        val rootIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://nehamycartapp.com")).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        val rootResolveInfo = packageManager.resolveActivity(rootIntent, 0)
        assertNotNull("Root deep link for nehamycartapp.com should resolve to an activity", rootResolveInfo)
        assertEquals(MainActivity::class.java.name, rootResolveInfo?.activityInfo?.name)

        // Path url
        val profileIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://nehamycartapp.com/profile")).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        val profileResolveInfo = packageManager.resolveActivity(profileIntent, 0)
        assertNotNull("Profile deep link for nehamycartapp.com should resolve to an activity", profileResolveInfo)
        assertEquals(MainActivity::class.java.name, profileResolveInfo?.activityInfo?.name)
    }

    @Test
    fun `nehamycartapp custom scheme resolves to MainActivity`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageManager = context.packageManager

        val customSchemeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("nehamycartapp://cart")).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        val resolveInfo = packageManager.resolveActivity(customSchemeIntent, 0)
        assertNotNull("Custom scheme link should resolve to an activity", resolveInfo)
        assertEquals(MainActivity::class.java.name, resolveInfo?.activityInfo?.name)
    }

    @Test
    fun `firebase dynamic link nehamycartapp page link resolves to MainActivity`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageManager = context.packageManager

        val dynamicLinkIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://nehamycartapp.page.link/cart")).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        val resolveInfo = packageManager.resolveActivity(dynamicLinkIntent, 0)
        assertNotNull("Firebase dynamic link should resolve to an activity", resolveInfo)
        assertEquals(MainActivity::class.java.name, resolveInfo?.activityInfo?.name)
    }

    @Test
    fun `main activity handles deep link data`() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://nehamycartapp.com/profile"))
        val controller = Robolectric.buildActivity(MainActivity::class.java, intent)
        val activity = controller.create().start().resume().get()
        assertNotNull(activity)
    }

    @Test
    fun `nehamycartapp netlify app deep link intent filter resolves to MainActivity`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageManager = context.packageManager

        // Root Netlify url
        val netlifyRootIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://nehamycartapp.netlify.app/")).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        val rootResolveInfo = packageManager.resolveActivity(netlifyRootIntent, 0)
        assertNotNull("Root deep link for nehamycartapp.netlify.app should resolve to an activity", rootResolveInfo)
        assertEquals(MainActivity::class.java.name, rootResolveInfo?.activityInfo?.name)

        // Cart Netlify url
        val netlifyCartIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://nehamycartapp.netlify.app/cart")).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        val cartResolveInfo = packageManager.resolveActivity(netlifyCartIntent, 0)
        assertNotNull("Cart deep link for nehamycartapp.netlify.app should resolve to an activity", cartResolveInfo)
        assertEquals(MainActivity::class.java.name, cartResolveInfo?.activityInfo?.name)

        // Product Netlify url
        val netlifyProductIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://nehamycartapp.netlify.app/product?id=prod_1")).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        val productResolveInfo = packageManager.resolveActivity(netlifyProductIntent, 0)
        assertNotNull("Product deep link for nehamycartapp.netlify.app should resolve to an activity", productResolveInfo)
        assertEquals(MainActivity::class.java.name, productResolveInfo?.activityInfo?.name)
    }

    @Test
    fun `main activity handles netlify app deep link data`() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://nehamycartapp.netlify.app/cart"))
        val controller = Robolectric.buildActivity(MainActivity::class.java, intent)
        val activity = controller.create().start().resume().get()
        assertNotNull(activity)
    }

    @Test
    fun `firestore order model holds order dates total amounts and statuses`() {
        val now = com.google.firebase.Timestamp.now()
        val order = com.example.data.repository.FirestoreOrder(
            id = "ORD_CLOUD_991",
            userId = "test_user_uid",
            totalAmount = 4999.0,
            status = "DELIVERED",
            deliveryAddress = "Flat 101, Lakeview Residency, Hyderabad",
            itemsCount = 2,
            createdAt = now
        )

        assertEquals("ORD_CLOUD_991", order.id)
        assertEquals("test_user_uid", order.userId)
        assertEquals(4999.0, order.totalAmount, 0.001)
        assertEquals("DELIVERED", order.status)
        assertEquals(now, order.createdAt)
        assertEquals("Flat 101, Lakeview Residency, Hyderabad", order.deliveryAddress)
        assertEquals(2, order.itemsCount)

        val formattedAmount = com.example.util.CurrencyUtil.formatInr(order.totalAmount)
        assertEquals("₹4,999", formattedAmount)
    }

    @Test
    fun `firestore order supports all lifecycle statuses`() {
        val statuses = listOf("CONFIRMED", "PREPARING", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED")
        statuses.forEach { status ->
            val order = com.example.data.repository.FirestoreOrder(
                id = "ORD_${status}",
                totalAmount = 1499.0,
                status = status,
                deliveryAddress = "Telangana, India",
                itemsCount = 1
            )
            assertEquals(status, order.status)
            assertTrue(order.totalAmount > 0.0)
        }
    }

    @Test
    fun `mock notification system triggers toast when status changes to Out for Delivery`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.util.MockNotificationSystem.reset()

        val activeOrder = com.example.data.model.Order(
            id = "CR-9001",
            dateCreated = "Today, 10:00 AM",
            items = emptyList(),
            subtotal = 1999.0,
            totalAmount = 1999.0,
            status = com.example.data.model.OrderStatus.PREPARING,
            courier = com.example.data.model.CourierInfo(name = "Liam Walker")
        )

        // Initial status PREPARING does not trigger delivery/out for delivery toast
        com.example.util.MockNotificationSystem.checkAndNotifyOrderStatus(context, activeOrder)
        var notification = com.example.util.MockNotificationSystem.activeNotification.value
        assertEquals(null, notification)

        // Status transitions to OUT_FOR_DELIVERY -> triggers toast notification
        val outForDeliveryOrder = activeOrder.copy(status = com.example.data.model.OrderStatus.OUT_FOR_DELIVERY)
        com.example.util.MockNotificationSystem.checkAndNotifyOrderStatus(context, outForDeliveryOrder)

        notification = com.example.util.MockNotificationSystem.activeNotification.value
        assertNotNull("Should emit active notification on Out for Delivery", notification)
        assertEquals("Out for Delivery", notification?.title)
        assertEquals(com.example.data.model.OrderStatus.OUT_FOR_DELIVERY, notification?.status)
        assertEquals("CR-9001", notification?.orderId)
        assertTrue(notification?.message?.contains("Out for Delivery") == true)
        assertTrue(notification?.message?.contains("Liam Walker") == true)
    }

    @Test
    fun `mock notification system triggers toast when status changes to Delivered`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.util.MockNotificationSystem.reset()

        val activeOrder = com.example.data.model.Order(
            id = "CR-9002",
            dateCreated = "Today, 11:00 AM",
            items = emptyList(),
            subtotal = 3499.0,
            totalAmount = 3499.0,
            status = com.example.data.model.OrderStatus.NEARBY
        )

        // Transition to DELIVERED -> triggers delivered toast notification
        val deliveredOrder = activeOrder.copy(status = com.example.data.model.OrderStatus.DELIVERED)
        com.example.util.MockNotificationSystem.checkAndNotifyOrderStatus(context, deliveredOrder)

        val notification = com.example.util.MockNotificationSystem.activeNotification.value
        assertNotNull("Should emit active notification on Delivered", notification)
        assertEquals("Order Delivered", notification?.title)
        assertEquals(com.example.data.model.OrderStatus.DELIVERED, notification?.status)
        assertEquals("CR-9002", notification?.orderId)
        assertTrue(notification?.message?.contains("Delivered") == true)
    }

    @Test
    fun `mock notification system manual simulation fires toast`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.util.MockNotificationSystem.reset()

        com.example.util.MockNotificationSystem.simulateNotification(
            context = context,
            status = com.example.data.model.OrderStatus.OUT_FOR_DELIVERY,
            orderId = "CR-TEST-1"
        )
        val notif1 = com.example.util.MockNotificationSystem.activeNotification.value
        assertNotNull(notif1)
        assertEquals("Out for Delivery", notif1?.title)

        com.example.util.MockNotificationSystem.simulateNotification(
            context = context,
            status = com.example.data.model.OrderStatus.DELIVERED,
            orderId = "CR-TEST-2"
        )
        val notif2 = com.example.util.MockNotificationSystem.activeNotification.value
        assertNotNull(notif2)
        assertEquals("Order Delivered", notif2?.title)
    }

    @Test
    fun `toggleQuickSort toggles between Price Low to High and Newest Arrivals`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val repo = com.example.data.repository.ShoppingRepository()
        val viewModel = com.example.ui.viewmodel.ShoppingViewModel(app, repo)

        // Initial default is RELEVANCE
        assertEquals(com.example.ui.viewmodel.ProductSortOrder.RELEVANCE, viewModel.sortOrder.value)

        // First toggle switches to PRICE_LOW_TO_HIGH
        viewModel.toggleQuickSort()
        assertEquals(com.example.ui.viewmodel.ProductSortOrder.PRICE_LOW_TO_HIGH, viewModel.sortOrder.value)

        // Second toggle switches to NEWEST_ARRIVALS
        viewModel.toggleQuickSort()
        assertEquals(com.example.ui.viewmodel.ProductSortOrder.NEWEST_ARRIVALS, viewModel.sortOrder.value)

        // Third toggle switches back to PRICE_LOW_TO_HIGH
        viewModel.toggleQuickSort()
        assertEquals(com.example.ui.viewmodel.ProductSortOrder.PRICE_LOW_TO_HIGH, viewModel.sortOrder.value)
    }

    @Test
    fun `product sort order enum includes Newest Arrivals`() {
        val newestOrder = com.example.ui.viewmodel.ProductSortOrder.NEWEST_ARRIVALS
        assertEquals("Newest Arrivals", newestOrder.label)
        val lowToHigh = com.example.ui.viewmodel.ProductSortOrder.PRICE_LOW_TO_HIGH
        assertEquals("Price: Low to High", lowToHigh.label)
    }
}


