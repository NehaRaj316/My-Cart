package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CartItem
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.data.model.ProductReview
import com.example.data.model.TastePreference
import com.example.data.model.UserPersona
import com.example.data.repository.ShoppingRepository
import com.example.util.CurrencyUtil
import com.example.util.MockNotification
import com.example.util.MockNotificationSystem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class ProductSortOrder(val label: String) {
    RELEVANCE("Featured"),
    PRICE_LOW_TO_HIGH("Price: Low to High"),
    PRICE_HIGH_TO_LOW("Price: High to Low"),
    RATING("Highest Rated"),
    DISCOUNT("Biggest Discount %")
}

enum class PriceFilterRange(val label: String, val min: Double?, val max: Double?) {
    ALL("All Prices", null, null),
    UNDER_1500("Under ₹1,500", null, 1500.0),
    BETWEEN_1500_AND_5000("₹1,500 – ₹5,000", 1500.0, 5000.0),
    BETWEEN_5000_AND_15000("₹5,000 – ₹15,000", 5000.0, 15000.0),
    ABOVE_15000("Above ₹15,000", 15000.0, null)
}

enum class AppNavTab {
    SHOP,
    RECOMMENDATIONS,
    TRACKING,
    CART,
    FAVORITES,
    ORDERS
}

class ShoppingViewModel(
    application: Application,
    private val repository: ShoppingRepository
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application = application,
        repository = ShoppingRepository.getInstance(application)
    )

    private val _currentTab = MutableStateFlow(AppNavTab.SHOP)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _sortOrder = MutableStateFlow(ProductSortOrder.RELEVANCE)
    val sortOrder: StateFlow<ProductSortOrder> = _sortOrder.asStateFlow()

    private val _priceRange = MutableStateFlow(PriceFilterRange.ALL)
    val priceRange: StateFlow<PriceFilterRange> = _priceRange.asStateFlow()

    private val _minRating = MutableStateFlow(0f)
    val minRating: StateFlow<Float> = _minRating.asStateFlow()

    private val _onlyFastDelivery = MutableStateFlow(false)
    val onlyFastDelivery: StateFlow<Boolean> = _onlyFastDelivery.asStateFlow()

    val products: StateFlow<List<Product>> = repository.products
    val wishlistIds: StateFlow<Set<String>> = repository.wishlistIds
    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
    val userTaste: StateFlow<TastePreference> = repository.userTaste
    val activeOrder: StateFlow<Order?> = repository.activeOrder
    val pastOrders: StateFlow<List<Order>> = repository.pastOrders

    private val _selectedProductDetail = MutableStateFlow<Product?>(null)
    val selectedProductDetail: StateFlow<Product?> = _selectedProductDetail.asStateFlow()

    private val _promoCode = MutableStateFlow("CARTIFY20")
    val promoCode: StateFlow<String> = _promoCode.asStateFlow()

    private val _promoApplied = MutableStateFlow(true)
    val promoApplied: StateFlow<Boolean> = _promoApplied.asStateFlow()

    private val _isLiveSimulationPlaying = MutableStateFlow(true)
    val isLiveSimulationPlaying: StateFlow<Boolean> = _isLiveSimulationPlaying.asStateFlow()

    private val _snackbarEvent = MutableStateFlow<String?>(null)
    val snackbarEvent: StateFlow<String?> = _snackbarEvent.asStateFlow()

    private var simulationJob: Job? = null

    init {
        startSimulationTicker()
        viewModelScope.launch {
            activeOrder.collect { order ->
                MockNotificationSystem.checkAndNotifyOrderStatus(getApplication(), order)
            }
        }
    }

    // Active filter count for badge display
    val activeFilterCount: StateFlow<Int> = combine(
        _selectedCategory,
        _priceRange,
        _minRating,
        _sortOrder,
        _onlyFastDelivery
    ) { category, price, rating, sort, fastDelivery ->
        var count = 0
        if (category != "All") count++
        if (price != PriceFilterRange.ALL) count++
        if (rating > 0f) count++
        if (sort != ProductSortOrder.RELEVANCE) count++
        if (fastDelivery) count++
        count
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    private data class ProductFilterCriteria(
        val query: String = "",
        val category: String = "All",
        val priceRange: PriceFilterRange = PriceFilterRange.ALL,
        val minRating: Float = 0f,
        val onlyFastDelivery: Boolean = false,
        val sortOrder: ProductSortOrder = ProductSortOrder.RELEVANCE
    )

    private val filterCriteria: kotlinx.coroutines.flow.Flow<ProductFilterCriteria> = combine(
        _searchQuery,
        _selectedCategory,
        _priceRange,
        _minRating
    ) { query, category, price, rating ->
        ProductFilterCriteria(query = query, category = category, priceRange = price, minRating = rating)
    }.combine(_onlyFastDelivery) { criteria, fastDelivery ->
        criteria.copy(onlyFastDelivery = fastDelivery)
    }.combine(_sortOrder) { criteria, sort ->
        criteria.copy(sortOrder = sort)
    }

    // Real-time filtered products for Explore/Shop screen
    val filteredProducts: StateFlow<List<Product>> = combine(
        products,
        filterCriteria
    ) { allProducts, criteria ->
        val cleanQuery = criteria.query.trim()
        val filtered = allProducts.filter { product ->
            val matchesQuery = cleanQuery.isBlank() ||
                product.title.contains(cleanQuery, ignoreCase = true) ||
                product.subtitle.contains(cleanQuery, ignoreCase = true) ||
                product.description.contains(cleanQuery, ignoreCase = true) ||
                product.category.contains(cleanQuery, ignoreCase = true) ||
                product.badge?.contains(cleanQuery, ignoreCase = true) == true

            val matchesCategory = criteria.category == "All" || product.category.equals(criteria.category, ignoreCase = true)

            val matchesPrice = when {
                criteria.priceRange.min != null && criteria.priceRange.max != null -> product.price in criteria.priceRange.min..criteria.priceRange.max
                criteria.priceRange.min != null -> product.price >= criteria.priceRange.min
                criteria.priceRange.max != null -> product.price <= criteria.priceRange.max
                else -> true
            }

            val matchesRating = criteria.minRating == 0f || product.rating >= criteria.minRating

            val matchesFastDelivery = !criteria.onlyFastDelivery || product.inStock

            matchesQuery && matchesCategory && matchesPrice && matchesRating && matchesFastDelivery
        }

        when (criteria.sortOrder) {
            ProductSortOrder.RELEVANCE -> filtered
            ProductSortOrder.PRICE_LOW_TO_HIGH -> filtered.sortedBy { it.price }
            ProductSortOrder.PRICE_HIGH_TO_LOW -> filtered.sortedByDescending { it.price }
            ProductSortOrder.RATING -> filtered.sortedByDescending { it.rating }
            ProductSortOrder.DISCOUNT -> filtered.sortedByDescending { it.discountPercent }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Personalized recommendations list sorted by AI score
    val personalizedRecommendations: StateFlow<List<Product>> = combine(
        products,
        userTaste
    ) { allProducts, taste ->
        allProducts
            .filter { product ->
                if (taste.highRatedOnly && product.rating < 4.8f) return@filter false
                if (taste.budgetFriendlyOnly && product.price > 2500.0) return@filter false
                true
            }
            .sortedByDescending { it.recommendationScore }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun selectTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSelectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSortOrder(order: ProductSortOrder) {
        _sortOrder.value = order
    }

    fun setPriceRange(range: PriceFilterRange) {
        _priceRange.value = range
    }

    fun setMinRating(rating: Float) {
        _minRating.value = rating
    }

    fun setOnlyFastDelivery(enabled: Boolean) {
        _onlyFastDelivery.value = enabled
    }

    fun applyFilters(
        sort: ProductSortOrder,
        price: PriceFilterRange,
        rating: Float,
        fastDelivery: Boolean,
        category: String
    ) {
        _sortOrder.value = sort
        _priceRange.value = price
        _minRating.value = rating
        _onlyFastDelivery.value = fastDelivery
        _selectedCategory.value = category
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = "All"
        _priceRange.value = PriceFilterRange.ALL
        _minRating.value = 0f
        _sortOrder.value = ProductSortOrder.RELEVANCE
        _onlyFastDelivery.value = false
    }

    fun openProductDetail(product: Product) {
        _selectedProductDetail.value = product
    }

    fun openProductById(productId: String): Boolean {
        val found = repository.products.value.find { it.id.equals(productId, ignoreCase = true) }
        if (found != null) {
            _selectedProductDetail.value = found
            _currentTab.value = AppNavTab.SHOP
            return true
        }
        return false
    }

    fun closeProductDetail() {
        _selectedProductDetail.value = null
    }

    fun toggleWishlist(productId: String) {
        repository.toggleWishlist(productId)
    }

    fun addToCart(product: Product, selectedColorHex: Long = product.primaryColorHex) {
        repository.addToCart(product, selectedColorHex)
        _snackbarEvent.value = "Added \"${product.title}\" to cart"
    }

    fun updateCartQuantity(productId: String, selectedColorHex: Long, newQuantity: Int) {
        repository.updateCartItemQuantity(productId, selectedColorHex, newQuantity)
    }

    fun applyPromoCode(code: String) {
        _promoCode.value = code
        if (code.trim().equals("CARTIFY20", ignoreCase = true)) {
            _promoApplied.value = true
            _snackbarEvent.value = "Promo code CARTIFY20 applied! 20% discount granted."
        } else {
            _promoApplied.value = false
            _snackbarEvent.value = "Invalid discount code. Try CARTIFY20."
        }
    }

    fun updateUserPersona(persona: UserPersona) {
        val current = repository.userTaste.value
        repository.updateTastePreference(current.copy(selectedPersona = persona))
        _snackbarEvent.value = "Recommendations updated for ${persona.title}!"
    }

    fun toggleTasteFilter(budget: Boolean? = null, fastDelivery: Boolean? = null, highRated: Boolean? = null) {
        val current = repository.userTaste.value
        val updated = current.copy(
            budgetFriendlyOnly = budget ?: current.budgetFriendlyOnly,
            fastDeliveryOnly = fastDelivery ?: current.fastDeliveryOnly,
            highRatedOnly = highRated ?: current.highRatedOnly
        )
        repository.updateTastePreference(updated)
    }

    fun clearSnackbar() {
        _snackbarEvent.value = null
    }

    fun showSnackbar(message: String) {
        _snackbarEvent.value = message
    }

    // Checkout flow
    fun checkout(address: String = "Khammam, Telangana") {
        val currentCart = cartItems.value
        if (currentCart.isEmpty()) {
            _snackbarEvent.value = "Cart is empty"
            return
        }

        val subtotal = currentCart.sumOf { it.totalPrice }
        val discount = if (_promoApplied.value) subtotal * 0.20 else 0.0

        val newOrder = repository.placeOrder(
            address = address,
            discountApplied = discount
        )

        // Persist order to Firestore for authenticated users
        viewModelScope.launch {
            try {
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                if (auth.currentUser != null) {
                    val firestoreOrderRepo = com.example.data.repository.FirestoreOrderRepository(getApplication())
                    val totalQty = newOrder.items.sumOf { it.quantity }.coerceAtLeast(1)
                    firestoreOrderRepo.createOrder(
                        orderId = newOrder.id,
                        totalAmount = newOrder.totalAmount,
                        status = "CONFIRMED",
                        deliveryAddress = address,
                        itemsCount = totalQty
                    )
                }
            } catch (e: Exception) {
                // Graceful fallback
            }
        }

        _snackbarEvent.value = "Order #${newOrder.id} placed! Tracking live now."
        _currentTab.value = AppNavTab.TRACKING
        resumeSimulation()
    }

    // Real-time tracking controls
    fun toggleLiveSimulation() {
        if (_isLiveSimulationPlaying.value) {
            pauseSimulation()
        } else {
            resumeSimulation()
        }
    }

    fun pauseSimulation() {
        _isLiveSimulationPlaying.value = false
        simulationJob?.cancel()
    }

    fun resumeSimulation() {
        _isLiveSimulationPlaying.value = true
        startSimulationTicker()
    }

    fun advanceSimulationStep() {
        repository.advanceLiveTrackingTick()
        val order = activeOrder.value
        if (order?.status == OrderStatus.DELIVERED) {
            _snackbarEvent.value = "Package successfully delivered! Enjoy your items."
        }
    }

    fun resetSimulation() {
        repository.resetOrderSimulation()
        _snackbarEvent.value = "Reset order simulation for testing"
        resumeSimulation()
    }

    private fun startSimulationTicker() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            while (isActive) {
                delay(3000L) // Real-time pulse every 3 seconds
                if (_isLiveSimulationPlaying.value) {
                    val order = activeOrder.value
                    if (order != null && order.status != OrderStatus.DELIVERED) {
                        repository.advanceLiveTrackingTick()
                        if (activeOrder.value?.status == OrderStatus.DELIVERED) {
                            _snackbarEvent.value = "Courier Liam W. has arrived! Package delivered."
                        }
                    }
                }
            }
        }
    }

    // Price Alert State Management
    private val _priceAlerts = MutableStateFlow<Map<String, Double>>(emptyMap())
    val priceAlerts: StateFlow<Map<String, Double>> = _priceAlerts.asStateFlow()

    fun setPriceAlert(productId: String, targetPrice: Double) {
        val updated = _priceAlerts.value.toMutableMap()
        updated[productId] = targetPrice
        _priceAlerts.value = updated
        val product = products.value.find { it.id == productId }
        val name = product?.title ?: "Item"
        _snackbarEvent.value = "🔔 Price alert set for $name at ${CurrencyUtil.formatRupees(targetPrice)}"
    }

    fun removePriceAlert(productId: String) {
        val updated = _priceAlerts.value.toMutableMap()
        updated.remove(productId)
        _priceAlerts.value = updated
        _snackbarEvent.value = "Price alert removed"
    }

    fun simulatePriceDrop(productId: String, droppedPrice: Double) {
        val product = products.value.find { it.id == productId } ?: return
        val target = _priceAlerts.value[productId] ?: (product.price * 0.90)

        // Update product price in repository & Room
        repository.updateProductPrice(productId, droppedPrice)

        if (droppedPrice <= target) {
            _snackbarEvent.value = "🎉 Price Drop Alert! ${product.title} dropped to ${CurrencyUtil.formatRupees(droppedPrice)} (below your target ${CurrencyUtil.formatRupees(target)})!"
        } else {
            _snackbarEvent.value = "Price updated to ${CurrencyUtil.formatRupees(droppedPrice)}"
        }
    }

    val reviews: StateFlow<Map<String, List<ProductReview>>> = repository.reviews

    fun submitReview(
        productId: String,
        rating: Int,
        comment: String,
        userName: String,
        userId: String
    ) {
        val review = repository.addReview(
            productId = productId,
            userId = userId,
            userName = userName,
            rating = rating,
            comment = comment
        )
        val product = products.value.find { it.id == productId }
        _snackbarEvent.value = "⭐ Review submitted! Thank you for rating ${product?.title ?: "this item"}."
    }

    // Mock Notification System
    val activeNotification: StateFlow<MockNotification?> = MockNotificationSystem.activeNotification

    fun dismissNotification() {
        MockNotificationSystem.dismissActiveNotification()
    }

    fun triggerMockNotification(status: OrderStatus) {
        MockNotificationSystem.simulateNotification(getApplication(), status, activeOrder.value?.id ?: "CR-8924")
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
    }
}
