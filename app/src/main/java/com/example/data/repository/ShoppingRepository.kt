package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.dao.CartDao
import com.example.data.local.dao.CartItemDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.entity.CartItemEntity
import com.example.data.local.entity.toEntity
import com.example.data.model.CartItem
import com.example.data.model.CourierInfo
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.data.model.TastePreference
import com.example.data.model.TrackingStep
import com.example.data.model.UserPersona
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShoppingRepository(
    private val productDao: ProductDao? = null,
    private val cartDao: CartDao? = null,
    private val cartItemDao: CartItemDao? = null,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {

    init {
        if (productDao != null && cartDao != null) {
            coroutineScope.launch {
                // Seed initial products in Room if not populated
                if (productDao.getProductCount() == 0) {
                    productDao.insertProducts(initialProducts.map { it.toEntity() })
                }
                // Seed initial cart items in Room if empty
                if (cartDao.getCartItemCount() == 0) {
                    cartDao.insertCartItem(
                        CartItemEntity(
                            productId = initialProducts[0].id,
                            selectedColorHex = initialProducts[0].primaryColorHex,
                            quantity = 1
                        )
                    )
                    cartDao.insertCartItem(
                        CartItemEntity(
                            productId = initialProducts[1].id,
                            selectedColorHex = initialProducts[1].primaryColorHex,
                            quantity = 1
                        )
                    )
                }

                // Collect products reactively from Room
                launch {
                    productDao.getAllProducts().collect { entities ->
                        if (entities.isNotEmpty()) {
                            _products.value = entities.map { it.toDomain() }
                        }
                    }
                }

                // Collect cart items reactively with their full product details
                launch {
                    cartDao.getCartItemsWithProduct().collect { itemsWithProduct ->
                        _cartItems.value = itemsWithProduct.map { it.toDomain() }
                    }
                }
            }
        }
    }

    private val initialProducts = listOf(
        Product(
            id = "prod_ethnic",
            title = "Banarasi Silk Saree & Royal Kurta Set",
            subtitle = "Pure Katan Silk with Golden Zari & Embroidery",
            category = "Fashion",
            price = 3499.0,
            originalPrice = 4999.0,
            rating = 4.9f,
            reviewsCount = 584,
            badge = "Festival Special",
            description = "Authentic hand-woven Banarasi pure silk saree with opulent golden floral zari border paired with a matching embroidered royal blue Chanderi silk kurta and dupatta.",
            specifications = listOf(
                "Fabric" to "Banarasi Pure Katan Silk & Chanderi",
                "Saree Length" to "5.5 Meters + 0.8m Blouse Piece",
                "Embroidery" to "Traditional Kadwa Golden Zari",
                "Care" to "Dry Clean Only"
            ),
            availableColors = listOf(0xFF15803D, 0xFF1E3A8A, 0xFFB91C1C),
            recommendationScore = 99,
            recommendationReason = "99% match for your Indian Ethnic style preference",
            primaryColorHex = 0xFF15803D,
            imageRes = R.drawable.img_indian_ethnic
        ),
        Product(
            id = "prod_groceries",
            title = "Royal Pantry: Aged Basmati Rice & Desi Ghee",
            subtitle = "Daawat 5kg Rice, Cow Bilona Ghee & Whole Spices Box",
            category = "Groceries",
            price = 1499.0,
            originalPrice = 1999.0,
            rating = 4.9f,
            reviewsCount = 742,
            badge = "Kitchen Staple",
            description = "Premium Indian culinary box featuring 5kg Daawat aged extra-long grain basmati rice, 750ml pure A2 cow bilona ghee jar, organic Lakadong turmeric, Alleppey green cardamom, and split red lentils.",
            specifications = listOf(
                "Rice" to "5kg 2-Year Aged Royal Basmati",
                "Ghee" to "750ml Pure A2 Vedic Cow Bilona Ghee",
                "Spices Included" to "Turmeric, Cumin, Cardamom, Cloves",
                "Shelf Life" to "12 Months in cool dry place"
            ),
            availableColors = listOf(0xFFD97706, 0xFFB45309),
            recommendationScore = 97,
            recommendationReason = "Essential grocery staples with next-day express delivery",
            primaryColorHex = 0xFFD97706,
            imageRes = R.drawable.img_indian_groceries
        ),
        Product(
            id = "prod_laptop",
            title = "Zenith Pro 16 AI Ultrabook Laptop",
            subtitle = "Intel Core Ultra 7, 32GB RAM, 3.2K OLED 120Hz",
            category = "Tech",
            price = 68999.0,
            originalPrice = 79999.0,
            rating = 4.9f,
            reviewsCount = 412,
            badge = "Flagship Ultrabook",
            description = "Premium ultra-slim aluminum laptop engineered for productivity and creators. Features 16-inch 3.2K OLED 120Hz Dolby Vision display, Intel Core Ultra 7 155H with dedicated NPU, 32GB LPDDR5X RAM, 1TB Gen4 SSD, and illuminated keyboard.",
            specifications = listOf(
                "Display" to "16-inch 3.2K OLED 120Hz 500nits",
                "Processor" to "Intel Core Ultra 7 155H (16 Cores, 22 Threads)",
                "Memory & Storage" to "32GB LPDDR5X 7467MHz | 1TB PCIe 4.0 NVMe",
                "Battery" to "75Wh with 100W GaN Type-C Fast Charger"
            ),
            availableColors = listOf(0xFF334155, 0xFF94A3B8),
            recommendationScore = 96,
            recommendationReason = "Top rated laptop recommendation for creators & professionals",
            primaryColorHex = 0xFF334155,
            imageRes = R.drawable.img_laptop
        ),
        Product(
            id = "prod_smartphone",
            title = "Phantom X Pro 5G Flagship Mobile",
            subtitle = "Snapdragon 8 Gen 3, 50MP Sony Sensor, 120W Charge",
            category = "Tech",
            price = 42999.0,
            originalPrice = 49999.0,
            rating = 4.8f,
            reviewsCount = 920,
            badge = "Top 5G Mobile",
            description = "Next-generation flagship smartphone with Snapdragon 8 Gen 3, Sony LYT-900 1-inch 50MP OIS camera, 6.78-inch 144Hz LTPO AMOLED curved display, and 120W HyperCharge powering 0 to 100% in 19 minutes.",
            specifications = listOf(
                "Camera" to "50MP 1-inch Sony OIS + 50MP 3x Periscope + 50MP UW",
                "Display" to "6.78-inch 1.5K 144Hz LTPO Curved AMOLED 4500nits",
                "Chipset" to "Qualcomm Snapdragon 8 Gen 3 (4nm)",
                "Battery & Charging" to "5400mAh Battery | 120W Wired + 50W Wireless"
            ),
            availableColors = listOf(0xFF0284C7, 0xFF0F172A, 0xFFE2E8F0),
            recommendationScore = 98,
            recommendationReason = "98% match for your flagship mobile phone search",
            primaryColorHex = 0xFF0284C7,
            imageRes = R.drawable.img_smartphone
        ),
        Product(
            id = "prod_1",
            title = "AeroSound Pro Wireless",
            subtitle = "Active Noise Cancelling Hi-Res Audio",
            category = "Tech",
            price = 4999.0,
            originalPrice = 6499.0,
            rating = 4.9f,
            reviewsCount = 428,
            badge = "Top AI Pick",
            description = "Studio-grade 40mm beryllium drivers with adaptive hybrid ANC, 45-hour battery life, and spatial audio with dynamic head tracking.",
            specifications = listOf(
                "Driver" to "40mm Custom Beryllium",
                "Battery" to "45 Hours (Fast Charge 15m = 6h)",
                "Connectivity" to "Bluetooth 5.4 & Multipoint",
                "Weight" to "245g Ultralight Carbon"
            ),
            availableColors = listOf(0xFF0F172A, 0xFF475569, 0xFFE2E8F0),
            recommendationScore = 94,
            recommendationReason = "94% match with your Audiophile taste profile",
            primaryColorHex = 0xFF4F46E5
        ),
        Product(
            id = "prod_2",
            title = "NeoGlow Smart Ambient Bar",
            subtitle = "RGBIC Multi-Zone Sound Reactive Lighting",
            category = "Living",
            price = 1999.0,
            originalPrice = 2499.0,
            rating = 4.8f,
            reviewsCount = 215,
            badge = "Trending",
            description = "Immersive backlighting with 16 million colors, millimeter-wave motion sensor, and real-time audio sync for home theater or workspace.",
            specifications = listOf(
                "Luminance" to "1200 Lumens Peak",
                "Color Tech" to "RGBIC Independent Segments",
                "Ecosystem" to "Matter, HomeKit, Google Home",
                "Power" to "USB-C PD 30W"
            ),
            availableColors = listOf(0xFF0284C7, 0xFF4F46E5, 0xFF9333EA),
            recommendationScore = 92,
            recommendationReason = "Perfect complement to your tech desk setup",
            primaryColorHex = 0xFF0284C7
        ),
        Product(
            id = "prod_3",
            title = "Nomad Tactical Sling Bag",
            subtitle = "Weatherproof Cordura with Magnetic Buckle",
            category = "Fashion",
            price = 2199.0,
            originalPrice = 2799.0,
            rating = 4.7f,
            reviewsCount = 184,
            badge = "Editor's Choice",
            description = "Engineered from ballistic Cordura 1000D with Fidlock V-buckle, expandable 7L compartment, and dedicated padded tablet sleeve.",
            specifications = listOf(
                "Material" to "Cordura 1000D Waterproof",
                "Hardware" to "German Fidlock Magnetic Buckle",
                "Capacity" to "5L - 7L Expandable",
                "Compatibility" to "Up to 11-inch iPad Pro"
            ),
            availableColors = listOf(0xFF1E293B, 0xFF334155, 0xFF14532D),
            recommendationScore = 90,
            recommendationReason = "High match for urban everyday carry aesthetic",
            primaryColorHex = 0xFF334155
        ),
        Product(
            id = "prod_4",
            title = "ApexPulse Ultra GPS Watch",
            subtitle = "Titanium Bezel, Dual-Frequency GNSS",
            category = "Fitness",
            price = 7499.0,
            originalPrice = 8999.0,
            rating = 4.9f,
            reviewsCount = 512,
            badge = "Best Seller",
            description = "Grade 5 Titanium with sapphire crystal, ECG sensor, dual-band GPS route navigation, and up to 14 days endurance in smartwatch mode.",
            specifications = listOf(
                "Chassis" to "Grade 5 Titanium & Ceramic",
                "Display" to "1.43-inch Sapphire AMOLED 1500nits",
                "Water Resistance" to "10 ATM (100 meters)",
                "Battery" to "Up to 14 Days Typical Use"
            ),
            availableColors = listOf(0xFF0F172A, 0xFFEA580C, 0xFF64748B),
            recommendationScore = 95,
            recommendationReason = "Recommended for precision fitness & battery performance",
            primaryColorHex = 0xFFEA580C
        )
    )

    private val _products = MutableStateFlow(initialProducts)
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _wishlistIds = MutableStateFlow<Set<String>>(setOf("prod_ethnic", "prod_smartphone"))
    val wishlistIds: StateFlow<Set<String>> = _wishlistIds.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(
        listOf(
            CartItem(product = initialProducts[0], quantity = 1),
            CartItem(product = initialProducts[1], quantity = 1)
        )
    )
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _userTaste = MutableStateFlow(TastePreference())
    val userTaste: StateFlow<TastePreference> = _userTaste.asStateFlow()

    private val initialSteps = listOf(
        TrackingStep(
            title = "Order Confirmed",
            description = "Payment verified and order accepted by Express Logistics",
            timeLabel = "10:14 AM",
            location = "Central Fulfillment Hub",
            isCompleted = true,
            isCurrent = false
        ),
        TrackingStep(
            title = "Packed & Quality Checked",
            description = "Carefully boxed with eco-friendly protective cushioning",
            timeLabel = "10:38 AM",
            location = "Warehouse Bay 4",
            isCompleted = true,
            isCurrent = false
        ),
        TrackingStep(
            title = "Out for Delivery",
            description = "Courier Liam W. picked up your package on Scoot EV Pro",
            timeLabel = "11:15 AM",
            location = "Metro Dispatch Center",
            isCompleted = true,
            isCurrent = true
        ),
        TrackingStep(
            title = "In Transit - Approaching",
            description = "On Broadway Ave navigating towards your neighborhood",
            timeLabel = "Est. 11:32 AM",
            location = "Pine & Broadway Crossing",
            isCompleted = false,
            isCurrent = false
        ),
        TrackingStep(
            title = "Delivered to Door",
            description = "Recipient signature or front door drop-off",
            timeLabel = "Est. 11:40 AM",
            location = "785 Oak St, Apt 4B",
            isCompleted = false,
            isCurrent = false
        )
    )

    private val initialActiveOrder = Order(
        id = "CR-8924",
        dateCreated = "Today, 10:14 AM",
        items = listOf(
            CartItem(product = initialProducts[0], quantity = 1),
            CartItem(product = initialProducts[1], quantity = 1)
        ),
        subtotal = 4998.0,
        discount = 500.0,
        deliveryFee = 0.0,
        totalAmount = 4498.0,
        status = OrderStatus.OUT_FOR_DELIVERY,
        currentStepIndex = 2,
        etaMinutes = 11,
        distanceKm = 2.1f,
        routeProgress = 0.52f,
        courier = CourierInfo(),
        deliveryAddress = "785 Oak St, Apt 4B, Metro City",
        deliveryInstructions = "Please leave at front door and ring doorbell",
        trackingSteps = initialSteps
    )

    private val initialPastOrders = listOf(
        Order(
            id = "CR-7102",
            dateCreated = "Oct 8, 2026",
            items = listOf(
                CartItem(product = initialProducts[2], quantity = 1)
            ),
            subtotal = 68999.0,
            discount = 0.0,
            deliveryFee = 0.0,
            totalAmount = 68999.0,
            status = OrderStatus.DELIVERED,
            currentStepIndex = 4,
            etaMinutes = 0,
            distanceKm = 0.0f,
            routeProgress = 1.0f,
            courier = CourierInfo(name = "Marcus Chen", vehicle = "EcoVan (EV-102)"),
            deliveryAddress = "785 Oak St, Apt 4B, Metro City",
            trackingSteps = initialSteps.map { it.copy(isCompleted = true, isCurrent = false) }
        )
    )

    private val _activeOrder = MutableStateFlow<Order?>(initialActiveOrder)
    val activeOrder: StateFlow<Order?> = _activeOrder.asStateFlow()

    private val _pastOrders = MutableStateFlow(initialPastOrders)
    val pastOrders: StateFlow<List<Order>> = _pastOrders.asStateFlow()

    fun advanceLiveTrackingTick() {
        val current = _activeOrder.value ?: return
        if (current.status == OrderStatus.DELIVERED) return

        val newProgress = (current.routeProgress + 0.04f).coerceAtMost(1.0f)
        val newEta = ((1.0f - newProgress) * 20).toInt().coerceAtLeast(0)
        val newDist = ((1.0f - newProgress) * 4.2f).coerceAtLeast(0.0f)

        val newStatus = when {
            newProgress >= 1.0f -> OrderStatus.DELIVERED
            newProgress >= 0.85f -> OrderStatus.NEARBY
            newProgress >= 0.35f -> OrderStatus.OUT_FOR_DELIVERY
            newProgress >= 0.15f -> OrderStatus.PREPARING
            else -> OrderStatus.CONFIRMED
        }

        val stepIdx = when (newStatus) {
            OrderStatus.CONFIRMED -> 0
            OrderStatus.PREPARING -> 1
            OrderStatus.OUT_FOR_DELIVERY -> 2
            OrderStatus.NEARBY -> 3
            OrderStatus.DELIVERED -> 4
        }

        val updatedSteps = current.trackingSteps.mapIndexed { idx, step ->
            when {
                idx < stepIdx -> step.copy(isCompleted = true, isCurrent = false)
                idx == stepIdx -> step.copy(isCompleted = (newStatus == OrderStatus.DELIVERED), isCurrent = (newStatus != OrderStatus.DELIVERED))
                else -> step.copy(isCompleted = false, isCurrent = false)
            }
        }

        val updatedOrder = current.copy(
            routeProgress = newProgress,
            etaMinutes = newEta,
            distanceKm = String.format(Locale.US, "%.1f", newDist).toFloat(),
            status = newStatus,
            currentStepIndex = stepIdx,
            trackingSteps = updatedSteps
        )

        _activeOrder.value = updatedOrder

        if (newStatus == OrderStatus.DELIVERED) {
            if (_pastOrders.value.none { it.id == updatedOrder.id }) {
                _pastOrders.value = listOf(updatedOrder) + _pastOrders.value
            }
        }
    }

    fun resetOrderSimulation() {
        _activeOrder.value = initialActiveOrder.copy(
            routeProgress = 0.15f,
            etaMinutes = 18,
            distanceKm = 3.8f,
            status = OrderStatus.OUT_FOR_DELIVERY,
            currentStepIndex = 2
        )
    }

    fun updateTastePreference(preference: TastePreference) {
        _userTaste.value = preference
        recomputeRecommendations(preference)
    }

    private fun recomputeRecommendations(taste: TastePreference) {
        val updated = _products.value.map { product ->
            var score = 75
            var reason = "Matches your lifestyle and shopping interests"

            if (product.category == taste.selectedPersona.primaryCategory) {
                score += 18
                reason = "9${(7..9).random()}% match for your ${taste.selectedPersona.title} focus"
            } else if (taste.favoriteCategories.contains(product.category)) {
                score += 10
                reason = "Aligned with your favorite ${product.category} category"
            }

            if (taste.highRatedOnly && product.rating >= 4.8f) {
                score += 5
            }
            if (taste.budgetFriendlyOnly && product.price <= 3500.0) {
                score += 5
            }
            if (taste.fastDeliveryOnly && product.estimatedDeliveryDays == 1) {
                score += 4
            }

            product.copy(
                recommendationScore = score.coerceIn(70, 99),
                recommendationReason = reason
            )
        }
        _products.value = updated
    }

    fun toggleWishlist(productId: String) {
        val current = _wishlistIds.value.toMutableSet()
        if (current.contains(productId)) {
            current.remove(productId)
        } else {
            current.add(productId)
        }
        _wishlistIds.value = current
    }

    fun addToCart(product: Product, selectedColorHex: Long = product.primaryColorHex) {
        if (cartDao != null) {
            coroutineScope.launch {
                productDao?.insertProduct(product.toEntity())
                val existing = cartDao.getCartItem(product.id, selectedColorHex)
                if (existing != null) {
                    cartDao.updateQuantity(product.id, selectedColorHex, existing.quantity + 1)
                } else {
                    cartDao.insertCartItem(
                        CartItemEntity(
                            productId = product.id,
                            selectedColorHex = selectedColorHex,
                            quantity = 1
                        )
                    )
                }
            }
        } else {
            val current = _cartItems.value.toMutableList()
            val index = current.indexOfFirst { it.product.id == product.id && it.selectedColorHex == selectedColorHex }
            if (index >= 0) {
                val existing = current[index]
                current[index] = existing.copy(quantity = existing.quantity + 1)
            } else {
                current.add(CartItem(product = product, quantity = 1, selectedColorHex = selectedColorHex))
            }
            _cartItems.value = current
        }
    }

    fun updateCartItemQuantity(productId: String, selectedColorHex: Long, newQuantity: Int) {
        if (cartDao != null) {
            coroutineScope.launch {
                if (newQuantity <= 0) {
                    cartDao.deleteCartItem(productId, selectedColorHex)
                } else {
                    cartDao.updateQuantity(productId, selectedColorHex, newQuantity)
                }
            }
        } else {
            val current = _cartItems.value.toMutableList()
            val index = current.indexOfFirst { it.product.id == productId && it.selectedColorHex == selectedColorHex }
            if (index >= 0) {
                if (newQuantity <= 0) {
                    current.removeAt(index)
                } else {
                    current[index] = current[index].copy(quantity = newQuantity)
                }
                _cartItems.value = current
            }
        }
    }

    fun clearCart() {
        if (cartDao != null) {
            coroutineScope.launch {
                cartDao.clearCart()
            }
        } else {
            _cartItems.value = emptyList()
        }
    }

    fun placeOrder(
        address: String,
        discountApplied: Double,
        deliveryInstructions: String = "Please leave at front door"
    ): Order {
        val items = _cartItems.value
        val subtotal = items.sumOf { it.totalPrice }
        val total = (subtotal - discountApplied).coerceAtLeast(0.0)

        val newId = "CR-" + (1000..9999).random()
        val dateStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())

        val steps = listOf(
            TrackingStep(
                title = "Order Placed",
                description = "Confirmed and assigned to Express Logistics",
                timeLabel = dateStr,
                location = "Metro Dispatch Center",
                isCompleted = true,
                isCurrent = true
            ),
            TrackingStep(
                title = "Packing & Quality Inspection",
                description = "Preparing your items for transport",
                timeLabel = "Est. 10m",
                location = "Warehouse Zone A",
                isCompleted = false,
                isCurrent = false
            ),
            TrackingStep(
                title = "Courier Pickup",
                description = "Liam Walker taking possession of parcel",
                timeLabel = "Est. 20m",
                location = "Express Bay",
                isCompleted = false,
                isCurrent = false
            ),
            TrackingStep(
                title = "Out for Delivery",
                description = "Courier en route to your destination",
                timeLabel = "Est. 35m",
                location = "Neighborhood Route",
                isCompleted = false,
                isCurrent = false
            ),
            TrackingStep(
                title = "Delivered",
                description = "Delivered to $address",
                timeLabel = "Est. 45m",
                location = address,
                isCompleted = false,
                isCurrent = false
            )
        )

        val order = Order(
            id = newId,
            dateCreated = "Today, $dateStr",
            items = items,
            subtotal = subtotal,
            discount = discountApplied,
            deliveryFee = 0.0,
            totalAmount = total,
            status = OrderStatus.CONFIRMED,
            currentStepIndex = 0,
            etaMinutes = 25,
            distanceKm = 5.2f,
            routeProgress = 0.08f,
            courier = CourierInfo(),
            deliveryAddress = address,
            deliveryInstructions = deliveryInstructions,
            trackingSteps = steps
        )

        _activeOrder.value = order
        clearCart()
        return order
    }

    companion object {
        @Volatile
        private var INSTANCE: ShoppingRepository? = null

        fun getInstance(context: Context): ShoppingRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context)
                val instance = ShoppingRepository(
                    productDao = db.productDao(),
                    cartDao = db.cartDao(),
                    cartItemDao = db.cartItemDao()
                )
                INSTANCE = instance
                instance
            }
        }
    }
}

