package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

@IgnoreExtraProperties
data class FirestoreCartItem(
    val id: String = "",
    val userId: String = "",
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val imageUrl: String? = null,
    val selectedColor: String? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

class FirestoreCartRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore cart.")
    }

    fun observeCartItems(): Flow<List<FirestoreCartItem>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/cart"
        emitAll(
            db.collection("users").document(uid).collection("cart")
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        val item = doc.toObject(FirestoreCartItem::class.java)
                        item?.copy(id = doc.id)
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun addOrUpdateCartItem(
        itemId: String,
        productId: String,
        name: String,
        price: Double,
        quantity: Int,
        imageUrl: String? = null,
        selectedColor: String? = null
    ) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("cart").document(itemId)
        val payload = mutableMapOf<String, Any>(
            "id" to itemId,
            "userId" to uid,
            "productId" to productId,
            "name" to name,
            "price" to price,
            "quantity" to quantity,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (imageUrl != null) payload["imageUrl"] = imageUrl
        if (selectedColor != null) payload["selectedColor"] = selectedColor

        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun updateQuantity(itemId: String, newQuantity: Int) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("cart").document(itemId)
        try {
            docRef.update(
                mapOf(
                    "quantity" to newQuantity,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun removeCartItem(itemId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("cart").document(itemId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun clearCart() {
        val uid = requireUserId()
        val collectionRef = db.collection("users").document(uid).collection("cart")
        try {
            val snapshot = collectionRef.get().await()
            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, collectionRef.path)
            throw e
        }
    }
}
