package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.credentials.CredentialManager
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.SignInPromptDialog
import com.example.ui.screens.attemptAutoSignIn
import com.example.ui.screens.signOut
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderStatus
import androidx.compose.material.icons.filled.ReceiptLong
import com.example.ui.components.AppDeepLinkDialog
import com.example.ui.components.Product3DViewerDialog
import com.example.ui.components.ProductDetailSheet
import com.example.ui.components.ProductShareDialog
import com.example.ui.screens.CartScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.OrderHistoryScreen
import com.example.ui.screens.OrderTrackingScreen
import com.example.ui.screens.RecommendationsScreen
import com.example.ui.screens.ShopHomeScreen
import com.example.ui.theme.CartifyTheme
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.ShoppingViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: ShoppingViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Process incoming Firebase Dynamic Links and deep links
        processIncomingLink(intent)

        setContent {
            CartifyTheme {
                val currentTab by viewModel.currentTab.collectAsState()
                val products by viewModel.filteredProducts.collectAsState()
                val recommendedProducts by viewModel.personalizedRecommendations.collectAsState()
                val wishlistIds by viewModel.wishlistIds.collectAsState()
                val cartItems by viewModel.cartItems.collectAsState()
                val userTaste by viewModel.userTaste.collectAsState()
                val activeOrder by viewModel.activeOrder.collectAsState()
                val pastOrders by viewModel.pastOrders.collectAsState()
                val searchQuery by viewModel.searchQuery.collectAsState()
                val selectedCategory by viewModel.selectedCategory.collectAsState()
                val sortOrder by viewModel.sortOrder.collectAsState()
                val priceRange by viewModel.priceRange.collectAsState()
                val minRating by viewModel.minRating.collectAsState()
                val onlyFastDelivery by viewModel.onlyFastDelivery.collectAsState()
                val activeFilterCount by viewModel.activeFilterCount.collectAsState()
                val selectedProductDetail by viewModel.selectedProductDetail.collectAsState()
                val promoCode by viewModel.promoCode.collectAsState()
                val isPromoApplied by viewModel.promoApplied.collectAsState()
                val isSimulationPlaying by viewModel.isLiveSimulationPlaying.collectAsState()
                val snackbarEvent by viewModel.snackbarEvent.collectAsState()

                var showDeepLinkDialog by remember { mutableStateOf(false) }
                var active3DProduct by remember { mutableStateOf<com.example.data.model.Product?>(null) }
                var activeShareProduct by remember { mutableStateOf<com.example.data.model.Product?>(null) }
                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                // Handle back press if not on SHOP tab
                BackHandler(enabled = currentTab != AppNavTab.SHOP) {
                    viewModel.selectTab(AppNavTab.SHOP)
                }

                LaunchedEffect(snackbarEvent) {
                    snackbarEvent?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearSnackbar()
                    }
                }

                var currentUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }
                var isGuestMode by remember { mutableStateOf(false) }
                var showSignInPrompt by remember { mutableStateOf(false) }
                var signInPromptReason by remember { mutableStateOf("Sign in with your Google account to unlock full features.") }

                DisposableEffect(Unit) {
                    val auth = FirebaseAuth.getInstance()
                    val listener = FirebaseAuth.AuthStateListener { fbAuth ->
                        currentUser = fbAuth.currentUser
                        if (fbAuth.currentUser != null) {
                            isGuestMode = false
                        }
                    }
                    auth.addAuthStateListener(listener)
                    onDispose {
                        auth.removeAuthStateListener(listener)
                    }
                }

                LaunchedEffect(Unit) {
                    if (currentUser == null) {
                        val credentialManager = CredentialManager.create(this@MainActivity)
                        attemptAutoSignIn(
                            context = this@MainActivity,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                currentUser = FirebaseAuth.getInstance().currentUser
                                isGuestMode = false
                            },
                            onUnauthenticated = {},
                            scope = coroutineScope
                        )
                    }
                }

                if (currentUser == null && !isGuestMode) {
                    AuthScreen(
                        onAuthSuccess = {
                            currentUser = FirebaseAuth.getInstance().currentUser
                            isGuestMode = false
                            viewModel.showSnackbar("Welcome ${currentUser?.displayName ?: "back"}!")
                        },
                        onBrowseAsGuest = {
                            isGuestMode = true
                            viewModel.showSnackbar("Browsing as Guest. Sign in anytime to unlock full cart and features.")
                        }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "🛍️", fontSize = 16.sp)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.app_name),
                                            fontWeight = FontWeight.ExtraBold,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    }
                                },
                                actions = {
                                    // App Deep Link / Share Button
                                    IconButton(
                                        onClick = { showDeepLinkDialog = true },
                                        modifier = Modifier.testTag("top_bar_link_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Link,
                                            contentDescription = "App Deep Links & Share",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    if (currentUser == null) {
                                        // Guest Mode indicator & Sign In button
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier
                                                .padding(end = 4.dp)
                                                .clickable {
                                                    signInPromptReason = "Sign in with your Google account to unlock cloud cart, AI recommendations, and checkout."
                                                    showSignInPrompt = true
                                                }
                                                .testTag("top_bar_guest_sign_in")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "Sign In",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "Sign In",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    } else {
                                        // Wishlist indicator button (Click to open Favorites)
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier
                                                .padding(end = 4.dp)
                                                .clickable { viewModel.selectTab(AppNavTab.FAVORITES) }
                                                .testTag("top_bar_wishlist")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Favorite,
                                                    contentDescription = "Wishlist count",
                                                    tint = Color(0xFFF43F5E),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${wishlistIds.size}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        // Order History button
                                        IconButton(
                                            onClick = { viewModel.selectTab(AppNavTab.ORDERS) },
                                            modifier = Modifier.testTag("top_bar_orders_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ReceiptLong,
                                                contentDescription = "Order History",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        // Logout / Sign Out Button
                                        IconButton(
                                            onClick = {
                                                val credentialManager = CredentialManager.create(this@MainActivity)
                                                signOut(
                                                    context = this@MainActivity,
                                                    credentialManager = credentialManager,
                                                    onSignOutComplete = {
                                                        currentUser = null
                                                        isGuestMode = false
                                                        viewModel.showSnackbar("Signed out")
                                                    },
                                                    scope = coroutineScope
                                                )
                                            },
                                            modifier = Modifier.testTag("top_bar_sign_out_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Logout,
                                                contentDescription = "Sign Out",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        },
                        bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("bottom_nav_bar"),
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp
                        ) {
                            // 1. Discover Tab
                            NavigationBarItem(
                                selected = currentTab == AppNavTab.SHOP,
                                onClick = { viewModel.selectTab(AppNavTab.SHOP) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Explore,
                                        contentDescription = "Discover"
                                    )
                                },
                                label = { Text("Discover") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                    indicatorColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("nav_tab_shop")
                            )

                            // 2. AI Picks Tab
                            NavigationBarItem(
                                selected = currentTab == AppNavTab.RECOMMENDATIONS,
                                onClick = { viewModel.selectTab(AppNavTab.RECOMMENDATIONS) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI Recommendations"
                                    )
                                },
                                label = { Text("AI Picks") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                    indicatorColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("nav_tab_recommendations")
                            )

                            // 3. Live Tracking Tab (AUTHENTICATED USERS ONLY)
                            if (currentUser != null) {
                                val hasLiveOrder = activeOrder != null && activeOrder?.status != OrderStatus.DELIVERED
                                NavigationBarItem(
                                    selected = currentTab == AppNavTab.TRACKING,
                                    onClick = { viewModel.selectTab(AppNavTab.TRACKING) },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                if (hasLiveOrder) {
                                                    Badge(
                                                        containerColor = Color(0xFF10B981)
                                                    ) {
                                                        Text("LIVE")
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DirectionsBike,
                                                contentDescription = "Order Tracking"
                                            )
                                        }
                                    },
                                    label = { Text("Live Track") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                        indicatorColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("nav_tab_tracking")
                                )
                            }

                            // 4. Cart Tab
                            val totalCartCount = cartItems.sumOf { it.quantity }
                            NavigationBarItem(
                                selected = currentTab == AppNavTab.CART,
                                onClick = { viewModel.selectTab(AppNavTab.CART) },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (totalCartCount > 0) {
                                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                                    Text("$totalCartCount")
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ShoppingBag,
                                            contentDescription = "Cart"
                                        )
                                    }
                                },
                                label = { Text("Cart") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                    indicatorColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("nav_tab_cart")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            AppNavTab.SHOP -> {
                                ShopHomeScreen(
                                    products = products,
                                    recommendedProducts = recommendedProducts,
                                    wishlistIds = wishlistIds,
                                    activeOrder = activeOrder,
                                    searchQuery = searchQuery,
                                    selectedCategory = selectedCategory,
                                    onSearchChange = viewModel::onSearchQueryChange,
                                    onCategorySelect = viewModel::onSelectCategory,
                                    onProductClick = viewModel::openProductDetail,
                                    onWishlistToggle = { id ->
                                        if (currentUser == null) {
                                            signInPromptReason = "Sign in with your Google account to save items to your wishlist."
                                            showSignInPrompt = true
                                        } else {
                                            viewModel.toggleWishlist(id)
                                        }
                                    },
                                    onAddToCart = { product ->
                                        if (currentUser == null) {
                                            signInPromptReason = "Sign in with your Google account to add \"${product.title}\" to your cart and checkout."
                                            showSignInPrompt = true
                                        } else {
                                            viewModel.addToCart(product)
                                        }
                                    },
                                    onNavigateToTracking = {
                                        if (currentUser == null) {
                                            signInPromptReason = "Sign in with your Google account to access live order tracking."
                                            showSignInPrompt = true
                                        } else {
                                            viewModel.selectTab(AppNavTab.TRACKING)
                                        }
                                    },
                                    onNavigateToRecommendations = { viewModel.selectTab(AppNavTab.RECOMMENDATIONS) },
                                    onOpenLinkTester = { showDeepLinkDialog = true },
                                    isGuest = currentUser == null,
                                    onRequireSignIn = { reason ->
                                        signInPromptReason = reason
                                        showSignInPrompt = true
                                    },
                                    sortOrder = sortOrder,
                                    priceRange = priceRange,
                                    minRating = minRating,
                                    onlyFastDelivery = onlyFastDelivery,
                                    activeFilterCount = activeFilterCount,
                                    onApplyFilters = { sort, price, rating, fastDelivery, category ->
                                        viewModel.applyFilters(sort, price, rating, fastDelivery, category)
                                    },
                                    onResetFilters = viewModel::resetFilters,
                                    onOpen3DView = { product -> active3DProduct = product },
                                    onShareProduct = { product -> activeShareProduct = product }
                                )
                            }
                            AppNavTab.RECOMMENDATIONS -> {
                                RecommendationsScreen(
                                    recommendedProducts = recommendedProducts,
                                    wishlistIds = wishlistIds,
                                    userTaste = userTaste,
                                    onPersonaSelected = { persona ->
                                        if (currentUser == null) {
                                            signInPromptReason = "Sign in with your Google account to customize style personas and unlock personalized AI picks."
                                            showSignInPrompt = true
                                        } else {
                                            viewModel.updateUserPersona(persona)
                                        }
                                    },
                                    onFilterToggle = { budget, fastDelivery, highRated ->
                                        if (currentUser == null) {
                                            signInPromptReason = "Sign in with your Google account to customize AI taste filters."
                                            showSignInPrompt = true
                                        } else {
                                            viewModel.toggleTasteFilter(budget, fastDelivery, highRated)
                                        }
                                    },
                                    onProductClick = viewModel::openProductDetail,
                                    onWishlistToggle = { id ->
                                        if (currentUser == null) {
                                            signInPromptReason = "Sign in with your Google account to save items to your wishlist."
                                            showSignInPrompt = true
                                        } else {
                                            viewModel.toggleWishlist(id)
                                        }
                                    },
                                    onAddToCart = { product ->
                                        if (currentUser == null) {
                                            signInPromptReason = "Sign in with your Google account to add \"${product.title}\" to your cart."
                                            showSignInPrompt = true
                                        } else {
                                            viewModel.addToCart(product)
                                        }
                                    },
                                    isGuest = currentUser == null,
                                    onRequireSignIn = { reason ->
                                        signInPromptReason = reason
                                        showSignInPrompt = true
                                    }
                                )
                            }
                            AppNavTab.TRACKING -> {
                                if (currentUser == null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(24.dp)
                                            .testTag("tracking_auth_required_container"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("tracking_auth_required_card"),
                                            shape = RoundedCornerShape(24.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            ),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(28.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(16.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(72.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(text = "🔒", fontSize = 34.sp)
                                                }

                                                Text(
                                                    text = "Authentication Required",
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )

                                                Text(
                                                    text = "Order tracking is only available for authenticated accounts. Please sign in with your Google account to track your orders.",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                )

                                                Spacer(modifier = Modifier.height(4.dp))

                                                Button(
                                                    onClick = {
                                                        signInPromptReason = "Sign in with your Google account to access live order tracking."
                                                        showSignInPrompt = true
                                                    },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(48.dp)
                                                        .testTag("tracking_sign_in_button"),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text("Sign In with Google Account", fontWeight = FontWeight.Bold)
                                                }

                                                OutlinedButton(
                                                    onClick = { viewModel.selectTab(AppNavTab.SHOP) },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(44.dp),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text("Back to Discover")
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    OrderTrackingScreen(
                                        activeOrder = activeOrder,
                                        pastOrders = pastOrders,
                                        isSimulationPlaying = isSimulationPlaying,
                                        onToggleSimulation = viewModel::toggleLiveSimulation,
                                        onAdvanceSimulation = viewModel::advanceSimulationStep,
                                        onResetSimulation = viewModel::resetSimulation,
                                        isGuest = false,
                                        onRequireSignIn = { reason ->
                                            signInPromptReason = reason
                                            showSignInPrompt = true
                                        }
                                    )
                                }
                            }
                            AppNavTab.CART -> {
                                CartScreen(
                                    cartItems = cartItems,
                                    promoCode = promoCode,
                                    isPromoApplied = isPromoApplied,
                                    onUpdateQuantity = viewModel::updateCartQuantity,
                                    onApplyPromo = viewModel::applyPromoCode,
                                    onCheckout = { address ->
                                        if (currentUser == null) {
                                            signInPromptReason = "Sign in with your Google account to complete checkout and place orders."
                                            showSignInPrompt = true
                                        } else {
                                            viewModel.checkout(address)
                                        }
                                    },
                                    onStartShopping = { viewModel.selectTab(AppNavTab.SHOP) },
                                    isGuest = currentUser == null,
                                    onRequireSignIn = { reason ->
                                        signInPromptReason = reason
                                        showSignInPrompt = true
                                    }
                                )
                            }
                            AppNavTab.FAVORITES -> {
                                FavoritesScreen(
                                    allProducts = viewModel.products.collectAsState().value,
                                    wishlistIds = wishlistIds,
                                    onWishlistToggle = { id ->
                                        if (currentUser == null) {
                                            signInPromptReason = "Sign in with your Google account to save favorites."
                                            showSignInPrompt = true
                                        } else {
                                            viewModel.toggleWishlist(id)
                                        }
                                    },
                                    onAddToCart = { product ->
                                        if (currentUser == null) {
                                            signInPromptReason = "Sign in with your Google account to add items to cart."
                                            showSignInPrompt = true
                                        } else {
                                            viewModel.addToCart(product)
                                        }
                                    },
                                    onProductClick = viewModel::openProductDetail,
                                    onOpen3DView = { product -> active3DProduct = product },
                                    onShareProduct = { product -> activeShareProduct = product },
                                    onNavigateBack = { viewModel.selectTab(AppNavTab.SHOP) },
                                    onBrowseCatalog = { viewModel.selectTab(AppNavTab.SHOP) }
                                )
                            }
                            AppNavTab.ORDERS -> {
                                OrderHistoryScreen(
                                    onNavigateBack = { viewModel.selectTab(AppNavTab.SHOP) },
                                    onTrackOrder = { orderId ->
                                        viewModel.selectTab(AppNavTab.TRACKING)
                                    },
                                    onStartShopping = { viewModel.selectTab(AppNavTab.SHOP) },
                                    onRequireSignIn = { reason ->
                                        signInPromptReason = reason
                                        showSignInPrompt = true
                                    }
                                )
                            }
                        }

                        // Product Detail Modal Sheet
                        selectedProductDetail?.let { product ->
                            ProductDetailSheet(
                                product = product,
                                isWishlisted = wishlistIds.contains(product.id),
                                sheetState = sheetState,
                                onDismiss = viewModel::closeProductDetail,
                                onWishlistToggle = {
                                    if (currentUser == null) {
                                        signInPromptReason = "Sign in with your Google account to save favorites to your wishlist."
                                        showSignInPrompt = true
                                    } else {
                                        viewModel.toggleWishlist(product.id)
                                    }
                                },
                                onAddToCart = { selectedColor ->
                                    if (currentUser == null) {
                                        signInPromptReason = "Sign in with your Google account to add \"${product.title}\" to your cart."
                                        showSignInPrompt = true
                                    } else {
                                        viewModel.addToCart(product, selectedColor)
                                    }
                                },
                                onBuyNow = { selectedColor ->
                                    if (currentUser == null) {
                                        signInPromptReason = "Sign in with your Google account to proceed to checkout."
                                        showSignInPrompt = true
                                    } else {
                                        viewModel.addToCart(product, selectedColor)
                                        viewModel.selectTab(AppNavTab.CART)
                                    }
                                },
                                onOpen3DView = { active3DProduct = product },
                                onShareProduct = { activeShareProduct = product },
                                onShowMessage = { msg -> viewModel.showSnackbar(msg) }
                            )
                        }

                        // App Deep Link Tester & Share Dialog
                        if (showDeepLinkDialog) {
                            AppDeepLinkDialog(
                                onDismissRequest = { showDeepLinkDialog = false },
                                onTestLink = { url ->
                                    handleDeepLink(Uri.parse(url))
                                },
                                onShowMessage = { msg ->
                                    viewModel.showSnackbar(msg)
                                }
                            )
                        }

                        // 3D Product Inspection Dialog
                        active3DProduct?.let { product ->
                            Product3DViewerDialog(
                                product = product,
                                onDismissRequest = { active3DProduct = null },
                                onAddToCart = { colorHex ->
                                    if (currentUser == null) {
                                        signInPromptReason = "Sign in with your Google account to add items to cart."
                                        showSignInPrompt = true
                                    } else {
                                        viewModel.addToCart(product, colorHex)
                                    }
                                    active3DProduct = null
                                }
                            )
                        }

                        // Social Share Dialog
                        activeShareProduct?.let { product ->
                            ProductShareDialog(
                                product = product,
                                onDismissRequest = { activeShareProduct = null },
                                onShowMessage = { msg ->
                                    viewModel.showSnackbar(msg)
                                }
                            )
                        }

                        // Sign In Prompt Modal for Guest Mode
                        if (showSignInPrompt) {
                            SignInPromptDialog(
                                reason = signInPromptReason,
                                onDismiss = { showSignInPrompt = false },
                                onSignInSuccess = {
                                    currentUser = FirebaseAuth.getInstance().currentUser
                                    isGuestMode = false
                                    showSignInPrompt = false
                                    viewModel.showSnackbar("Welcome ${currentUser?.displayName ?: "back"}!")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        processIncomingLink(intent)
    }

    private fun processIncomingLink(incomingIntent: Intent?) {
        if (incomingIntent == null) return

        // 1. Process Firebase Dynamic Link via SDK
        try {
            FirebaseDynamicLinks.getInstance()
                .getDynamicLink(incomingIntent)
                .addOnSuccessListener(this) { pendingDynamicLinkData ->
                    val dynamicLinkUri = pendingDynamicLinkData?.link
                    if (dynamicLinkUri != null) {
                        Log.d("MainActivity", "Firebase Dynamic Link detected: $dynamicLinkUri")
                        handleDeepLink(dynamicLinkUri)
                    } else {
                        // 2. Fall back to standard deep link / app link from Intent data
                        val intentData = incomingIntent.data
                        if (intentData != null) {
                            Log.d("MainActivity", "Standard deep link detected: $intentData")
                            handleDeepLink(intentData)
                        }
                    }
                }
                .addOnFailureListener(this) { e ->
                    Log.w("MainActivity", "Could not fetch dynamic link", e)
                    val intentData = incomingIntent.data
                    if (intentData != null) {
                        handleDeepLink(intentData)
                    }
                }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in FirebaseDynamicLinks resolution", e)
            incomingIntent.data?.let { handleDeepLink(it) }
        }
    }

    private fun handleDeepLink(uri: Uri) {
        // Support unwrapping nested links inside Firebase Dynamic Links or query parameters (e.g. ?link=... or ?deep_link_id=...)
        val targetUri = uri.getQueryParameter("link")?.let { runCatching { Uri.parse(it) }.getOrNull() }
            ?: uri.getQueryParameter("deep_link_id")?.let { runCatching { Uri.parse(it) }.getOrNull() }
            ?: uri

        val fullLink = uri.toString()
        val path = targetUri.path.orEmpty().lowercase()
        val host = targetUri.host.orEmpty().lowercase()
        val segments = targetUri.pathSegments ?: emptyList()

        // 1. Check for product detail linking: /product/{id} or ?product_id={id} or ?id={id}
        val productIdFromQuery = targetUri.getQueryParameter("product_id") ?: targetUri.getQueryParameter("id")
        val productIdFromPath = if (segments.size >= 2 && segments[0].equals("product", ignoreCase = true)) {
            segments[1]
        } else null

        val targetProductId = productIdFromQuery ?: productIdFromPath
        if (!targetProductId.isNullOrBlank()) {
            val opened = viewModel.openProductById(targetProductId)
            if (opened) {
                viewModel.showSnackbar("Opened product: $targetProductId")
                return
            }
        }

        // 2. Tab routing
        when {
            path.startsWith("/profile") || host == "profile" ||
            path.startsWith("/recommendations") || host == "recommendations" -> {
                viewModel.selectTab(AppNavTab.RECOMMENDATIONS)
            }
            path.startsWith("/cart") || host == "cart" -> {
                viewModel.selectTab(AppNavTab.CART)
            }
            path.startsWith("/tracking") || host == "tracking" ||
            path.startsWith("/orders") || host == "orders" -> {
                if (FirebaseAuth.getInstance().currentUser == null) {
                    viewModel.showSnackbar("Sign in with Google to access order tracking.")
                    viewModel.selectTab(AppNavTab.SHOP)
                } else {
                    viewModel.selectTab(AppNavTab.TRACKING)
                }
            }
            else -> {
                viewModel.selectTab(AppNavTab.SHOP)
            }
        }
        viewModel.showSnackbar("Opened link: $fullLink")
    }
}
