package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class FirestoreOrderRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun unauthenticatedOrderAccessFailsFast() = runBlocking {
        auth.signOut()
        val repo = FirestoreOrderRepository(firestore, auth)

        try {
            repo.createOrder(
                orderId = "ORD_FAIL_TEST",
                totalAmount = 2499.0,
                status = "CONFIRMED",
                deliveryAddress = "123 Test Street",
                itemsCount = 2
            )
            fail("Expected IllegalStateException for unauthenticated user creating order")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("User must be signed in with Google") == true)
        }
    }

    @Test
    fun authenticatedUserCanCreateAndObserveOrder() = runBlocking {
        val uid = signInTestUser("carol_order_test@example.com")
        val repo = FirestoreOrderRepository(firestore, auth)
        val orderId = "ORD_TEST_${System.currentTimeMillis()}"

        val result = repo.createOrder(
            orderId = orderId,
            totalAmount = 5999.0,
            status = "CONFIRMED",
            deliveryAddress = "Plot 45, Indiranagar, Bengaluru, Karnataka 560038",
            itemsCount = 3
        )

        assertTrue("Order creation should succeed", result.isSuccess)

        val orders = repo.observeOrders().first()
        val saved = orders.find { it.id == orderId }
        assertNotNull("Order should be found in user's orders collection", saved)
        assertEquals("CONFIRMED", saved?.status)
        assertEquals(5999.0, saved?.totalAmount ?: 0.0, 0.01)
        assertEquals(3, saved?.itemsCount)
        assertEquals("Plot 45, Indiranagar, Bengaluru, Karnataka 560038", saved?.deliveryAddress)
    }

    @Test
    fun authenticatedUserCanSeedSampleOrders() = runBlocking {
        val uid = signInTestUser("david_seed_test@example.com")
        val repo = FirestoreOrderRepository(firestore, auth)

        val seedResult = repo.seedSampleOrdersIfEmpty()
        assertTrue("Seed should succeed", seedResult.isSuccess)

        val orders = repo.observeOrders().first()
        assertTrue("Should have seeded sample orders", orders.isNotEmpty())
    }
}
