package com.example.data.model

enum class OrderStatus(val label: String) {
    CONFIRMED("Order Confirmed"),
    PREPARING("Packed & Inspected"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    NEARBY("Arriving Shortly"),
    DELIVERED("Delivered")
}

data class TrackingStep(
    val title: String,
    val description: String,
    val timeLabel: String,
    val location: String,
    val isCompleted: Boolean,
    val isCurrent: Boolean
)

data class CourierInfo(
    val name: String = "Liam Walker",
    val vehicle: String = "Scoot EV Pro (SC-472)",
    val rating: Float = 4.95f,
    val contactNumber: String = "+1 (555) 019-2834",
    val completedDeliveries: Int = 1842
)

data class DeliveryCheckpoint(
    val name: String,
    val latNormalized: Float,
    val lngNormalized: Float
)

data class Order(
    val id: String,
    val dateCreated: String,
    val items: List<CartItem>,
    val subtotal: Double,
    val discount: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val totalAmount: Double,
    val status: OrderStatus = OrderStatus.OUT_FOR_DELIVERY,
    val currentStepIndex: Int = 2,
    val etaMinutes: Int = 12,
    val distanceKm: Float = 2.4f,
    val routeProgress: Float = 0.45f, // 0.0 to 1.0 along the route
    val courier: CourierInfo = CourierInfo(),
    val deliveryAddress: String = "785 Oak St, Apt 4B, Metro City",
    val deliveryInstructions: String = "Please leave at front door or ring bell",
    val trackingSteps: List<TrackingStep> = emptyList()
)
