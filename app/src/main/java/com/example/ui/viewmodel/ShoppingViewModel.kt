package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CartItem
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.data.model.TastePreference
import com.example.data.model.UserPersona
import com.example.data.repository.ShoppingRepository
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

enum class AppNavTab {
    SHOP,
    RECOMMENDATIONS,
    TRACKING,
    CART
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
    }

    // Filtered products for Explore/Shop screen
    val filteredProducts: StateFlow<List<Product>> = combine(
        products,
        _searchQuery,
        _selectedCategory
    ) { allProducts, query, category ->
        allProducts.filter { product ->
            val matchesQuery = query.isBlank() ||
                product.title.contains(query, ignoreCase = true) ||
                product.subtitle.contains(query, ignoreCase = true) ||
                product.description.contains(query, ignoreCase = true)

            val matchesCategory = category == "All" || product.category.equals(category, ignoreCase = true)

            matchesQuery && matchesCategory
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

    fun openProductDetail(product: Product) {
        _selectedProductDetail.value = product
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
    fun checkout(address: String = "785 Oak St, Apt 4B, Metro City") {
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

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
    }
}
