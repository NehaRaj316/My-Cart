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
    fun `recommendation engine recalculates scores on persona change`() {
        val repo = ShoppingRepository()
        repo.updateTastePreference(TastePreference(selectedPersona = UserPersona.URBAN_MINIMALIST))
        val products = repo.products.value
        val fashionItem = products.find { it.category == "Fashion" }
        assertNotNull(fashionItem)
        assertTrue(fashionItem!!.recommendationScore >= 85)
    }
}
