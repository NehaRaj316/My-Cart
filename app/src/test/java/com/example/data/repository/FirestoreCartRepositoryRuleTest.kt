package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class FirestoreCartRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun unauthenticatedAccessFailsFast() = runBlocking {
        auth.signOut()
        val repo = FirestoreCartRepository(firestore, auth)

        try {
            repo.addOrUpdateCartItem(
                itemId = "test_item_1",
                productId = "prod_1",
                name = "Test Item",
                price = 199.0,
                quantity = 1
            )
            fail("Expected IllegalStateException for unauthenticated user")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("User must be signed in with Google") == true)
        }
    }

    @Test
    fun authenticatedUserCanAddAndObserveCartItem() = runBlocking {
        val uid = signInTestUser("alice_rule_test@example.com")
        val repo = FirestoreCartRepository(firestore, auth)
        val itemId = "cart_item_alice_${System.currentTimeMillis()}"

        repo.addOrUpdateCartItem(
            itemId = itemId,
            productId = "prod_ethnic",
            name = "Banarasi Silk Saree",
            price = 3499.0,
            quantity = 2,
            selectedColor = "#15803D"
        )

        val items = repo.observeCartItems().first()
        val savedItem = items.find { it.productId == "prod_ethnic" }
        assertNotNull("Saved item should be retrieved from Firestore emulator", savedItem)
        assertEquals(2, savedItem?.quantity)
        assertEquals(3499.0, savedItem?.price ?: 0.0, 0.01)

        repo.removeCartItem(itemId)
    }

    @Test
    fun authenticatedUserCanUpdateQuantityAndRemoveItem() = runBlocking {
        val uid = signInTestUser("bob_rule_test@example.com")
        val repo = FirestoreCartRepository(firestore, auth)
        val itemId = "cart_item_bob_${System.currentTimeMillis()}"

        repo.addOrUpdateCartItem(
            itemId = itemId,
            productId = "prod_audio",
            name = "ANC Wireless Headphones",
            price = 4999.0,
            quantity = 1
        )

        repo.updateQuantity(itemId, 3)
        val itemsAfterUpdate = repo.observeCartItems().first()
        val updated = itemsAfterUpdate.find { it.productId == "prod_audio" }
        assertEquals(3, updated?.quantity)

        repo.removeCartItem(itemId)
        val itemsAfterDelete = repo.observeCartItems().first()
        assertTrue(itemsAfterDelete.none { it.productId == "prod_audio" })
    }
}
