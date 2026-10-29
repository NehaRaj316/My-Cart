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
data class FirestoreOrder(
    val id: String = "",
    val userId: String = "",
    val totalAmount: Double = 0.0,
    val status: String = "CONFIRMED", // 'CONFIRMED', 'PREPARING', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED'
    val deliveryAddress: String = "",
    val itemsCount: Int = 1,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

class FirestoreOrderRepository(
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
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore orders.")
    }

    /**
     * Observes all past and current orders for the authenticated user from Firestore.
     */
    fun observeOrders(): Flow<List<FirestoreOrder>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/orders"
        emitAll(
            db.collection("users").document(uid).collection("orders")
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        val order = doc.toObject(FirestoreOrder::class.java)
                        order?.copy(id = doc.id)
                    }.sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { e ->
                    if (e is Exception) {
                        handleFirestoreError(e, OperationType.LIST, path)
                    }
                    emit(emptyList())
                }
        )
    }

    /**
     * Creates a new order in the user's Firestore order collection.
     * Enforces the exact fields allowed by firestore.rules:
     * ['id', 'userId', 'totalAmount', 'status', 'deliveryAddress', 'itemsCount', 'createdAt', 'updatedAt']
     */
    suspend fun createOrder(
        orderId: String,
        totalAmount: Double,
        status: String = "CONFIRMED",
        deliveryAddress: String,
        itemsCount: Int
    ): Result<String> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }

        val path = "users/$uid/orders/$orderId"
        val data = hashMapOf<String, Any>(
            "id" to orderId,
            "userId" to uid,
            "totalAmount" to totalAmount,
            "status" to status,
            "deliveryAddress" to deliveryAddress,
            "itemsCount" to itemsCount.coerceAtLeast(1),
            "createdAt" to FieldValue.serverTimestamp()
        )

        return try {
            db.collection("users").document(uid).collection("orders")
                .document(orderId)
                .set(data)
                .await()
            Result.success(orderId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    /**
     * Seeds initial realistic sample orders for a newly signed in user if no orders exist yet.
     */
    suspend fun seedSampleOrdersIfEmpty(): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return try {
            val snapshot = db.collection("users").document(uid).collection("orders")
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                // Seed 2 sample historical orders for rich showcase
                val sample1Id = "ORD_${System.currentTimeMillis() % 100000}_A"
                val sample2Id = "ORD_${(System.currentTimeMillis() - 86400000L) % 100000}_B"

                val order1 = hashMapOf<String, Any>(
                    "id" to sample1Id,
                    "userId" to uid,
                    "totalAmount" to 4299.0,
                    "status" to "DELIVERED",
                    "deliveryAddress" to "Flat 302, Palm Heights, Bengaluru, Karnataka 560001",
                    "itemsCount" to 2,
                    "createdAt" to FieldValue.serverTimestamp()
                )

                val order2 = hashMapOf<String, Any>(
                    "id" to sample2Id,
                    "userId" to uid,
                    "totalAmount" to 12499.0,
                    "status" to "OUT_FOR_DELIVERY",
                    "deliveryAddress" to "Flat 302, Palm Heights, Bengaluru, Karnataka 560001",
                    "itemsCount" to 1,
                    "createdAt" to FieldValue.serverTimestamp()
                )

                val ordersColl = db.collection("users").document(uid).collection("orders")
                ordersColl.document(sample1Id).set(order1).await()
                ordersColl.document(sample2Id).set(order2).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            val path = "users/$uid/orders"
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }
}
