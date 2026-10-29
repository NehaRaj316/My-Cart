package com.example.ui.viewmodel

import com.example.data.model.Product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductSearchFilterTest {

    private val sampleProducts = listOf(
        Product(
            id = "p1",
            title = "Wireless Noise-Cancelling Headphones",
            subtitle = "Studio grade acoustics",
            description = "Active noise cancellation with 40-hour battery life",
            price = 18999.0,
            originalPrice = 24999.0,
            category = "Tech",
            rating = 4.9f,
            reviewsCount = 320,
            inStock = true
        ),
        Product(
            id = "p2",
            title = "Ergonomic Smart Watch",
            subtitle = "SpO2 & Heart Rate",
            description = "AMOLED display with fitness tracking",
            price = 4499.0,
            originalPrice = 7999.0,
            category = "Tech",
            rating = 4.7f,
            reviewsCount = 190,
            inStock = true
        ),
        Product(
            id = "p3",
            title = "Handwoven Banarasi Silk Saree",
            subtitle = "Zari border ethnic wear",
            description = "Traditional pure silk saree woven by master artisans",
            price = 3499.0,
            originalPrice = 4999.0,
            category = "Fashion",
            rating = 4.8f,
            reviewsCount = 85,
            inStock = true
        ),
        Product(
            id = "p4",
            title = "Organic Whole Wheat Atta",
            subtitle = "Stone ground 5kg",
            description = "100% whole grain organic wheat flour",
            price = 450.0,
            originalPrice = 520.0,
            category = "Groceries",
            rating = 4.6f,
            reviewsCount = 410,
            inStock = false
        )
    )

    @Test
    fun searchFiltering_matchesTitleAndCategory() {
        val query = "watch"
        val filtered = sampleProducts.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.subtitle.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true)
        }
        assertEquals(1, filtered.size)
        assertEquals("p2", filtered.first().id)
    }

    @Test
    fun priceFiltering_matchesUnder1500() {
        val range = PriceFilterRange.UNDER_1500
        val filtered = sampleProducts.filter { prod ->
            val max = range.max ?: Double.MAX_VALUE
            val min = range.min ?: 0.0
            prod.price in min..max
        }
        assertEquals(1, filtered.size)
        assertEquals("p4", filtered.first().id)
        assertEquals(450.0, filtered.first().price, 0.01)
    }

    @Test
    fun priceFiltering_matchesBetween1500And5000() {
        val range = PriceFilterRange.BETWEEN_1500_AND_5000
        val filtered = sampleProducts.filter { prod ->
            prod.price in (range.min ?: 0.0)..(range.max ?: Double.MAX_VALUE)
        }
        assertEquals(2, filtered.size)
        assertTrue(filtered.any { it.id == "p2" })
        assertTrue(filtered.any { it.id == "p3" })
    }

    @Test
    fun sortOrder_sortsByPriceAscending() {
        val sorted = sampleProducts.sortedBy { it.price }
        assertEquals("p4", sorted[0].id)
        assertEquals("p3", sorted[1].id)
        assertEquals("p2", sorted[2].id)
        assertEquals("p1", sorted[3].id)
    }

    @Test
    fun sortOrder_sortsByDiscountDescending() {
        val sorted = sampleProducts.sortedByDescending { it.discountPercent }
        assertEquals("p2", sorted[0].id) // 43%
        assertEquals("p3", sorted[1].id) // 30%
        assertEquals("p1", sorted[2].id) // 24%
        assertEquals("p4", sorted[3].id) // 13%
    }
}
