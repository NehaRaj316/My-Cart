package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.TastePreference
import com.example.data.model.UserPersona
import com.example.data.repository.ShoppingRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
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
}
