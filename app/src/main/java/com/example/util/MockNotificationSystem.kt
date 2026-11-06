package com.example.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class MockNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val status: OrderStatus,
    val orderId: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Mock notification system that triggers an Android Toast message and emits a notification event
 * whenever an order status changes to 'Delivered' or 'Out for Delivery'.
 */
object MockNotificationSystem {

    private var lastNotifiedStatus: OrderStatus? = null
    private var lastOrderId: String? = null

    private val _activeNotification = MutableStateFlow<MockNotification?>(null)
    val activeNotification: StateFlow<MockNotification?> = _activeNotification.asStateFlow()

    private val _notificationHistory = MutableStateFlow<List<MockNotification>>(emptyList())
    val notificationHistory: StateFlow<List<MockNotification>> = _notificationHistory.asStateFlow()

    /**
     * Inspects the active order and triggers a toast notification if the status has transitioned
     * to 'OUT_FOR_DELIVERY' or 'DELIVERED'.
     */
    fun checkAndNotifyOrderStatus(context: Context, order: Order?) {
        if (order == null) {
            lastNotifiedStatus = null
            lastOrderId = null
            return
        }

        // If order changed, reset tracked status so new order can trigger notifications
        if (lastOrderId != order.id) {
            lastOrderId = order.id
            lastNotifiedStatus = null
        }

        val currentStatus = order.status
        if (currentStatus == lastNotifiedStatus) {
            return
        }

        when (currentStatus) {
            OrderStatus.OUT_FOR_DELIVERY -> {
                val courierName = order.courier.name.ifBlank { "Liam Walker" }
                val title = "Out for Delivery"
                val message = "🛵 Order #${order.id} is Out for Delivery! Courier $courierName is on the way."
                triggerNotification(context, OrderStatus.OUT_FOR_DELIVERY, order.id, title, message)
                lastNotifiedStatus = currentStatus
            }
            OrderStatus.DELIVERED -> {
                val title = "Order Delivered"
                val message = "🎉 Order #${order.id} has been Delivered to your door! Enjoy your items."
                triggerNotification(context, OrderStatus.DELIVERED, order.id, title, message)
                lastNotifiedStatus = currentStatus
            }
            else -> {
                // Other intermediary statuses don't trigger the toast notification, but we update tracker
                lastNotifiedStatus = currentStatus
            }
        }
    }

    /**
     * Triggers the Toast on the main looper thread and updates the reactive StateFlows.
     */
    fun triggerNotification(
        context: Context,
        status: OrderStatus,
        orderId: String,
        title: String,
        message: String
    ) {
        val notification = MockNotification(
            title = title,
            message = message,
            status = status,
            orderId = orderId
        )

        _activeNotification.value = notification
        _notificationHistory.value = listOf(notification) + _notificationHistory.value

        // Guarantee Toast executes on the UI thread
        if (Looper.myLooper() == Looper.getMainLooper()) {
            showToast(context.applicationContext, message)
        } else {
            Handler(Looper.getMainLooper()).post {
                showToast(context.applicationContext, message)
            }
        }
    }

    private fun showToast(context: Context, message: String) {
        try {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        } catch (_: Exception) {
            // Safe handling in headless test environments if Looper/WindowToken is not attached
        }
    }

    /**
     * Helper to manually test/simulate toast notifications for specific statuses.
     */
    fun simulateNotification(context: Context, status: OrderStatus, orderId: String = "CR-8924") {
        when (status) {
            OrderStatus.OUT_FOR_DELIVERY -> {
                triggerNotification(
                    context = context,
                    status = OrderStatus.OUT_FOR_DELIVERY,
                    orderId = orderId,
                    title = "Out for Delivery",
                    message = "🛵 Order #$orderId is Out for Delivery! Courier Liam Walker is on the way."
                )
            }
            OrderStatus.DELIVERED -> {
                triggerNotification(
                    context = context,
                    status = OrderStatus.DELIVERED,
                    orderId = orderId,
                    title = "Order Delivered",
                    message = "🎉 Order #$orderId has been Delivered to your door! Enjoy your items."
                )
            }
            else -> {
                val title = status.label
                val message = "Order #$orderId status updated to ${status.label}."
                triggerNotification(
                    context = context,
                    status = status,
                    orderId = orderId,
                    title = title,
                    message = message
                )
            }
        }
    }

    fun dismissActiveNotification() {
        _activeNotification.value = null
    }

    fun reset() {
        lastNotifiedStatus = null
        lastOrderId = null
        _activeNotification.value = null
        _notificationHistory.value = emptyList()
    }
}
