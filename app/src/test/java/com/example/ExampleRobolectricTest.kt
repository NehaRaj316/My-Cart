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
    fun `main activity handles deep link data`() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://nehamycartapp.com/profile"))
        val controller = Robolectric.buildActivity(MainActivity::class.java, intent)
        val activity = controller.create().start().resume().get()
        assertNotNull(activity)
    }
}
