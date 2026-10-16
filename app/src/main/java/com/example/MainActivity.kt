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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.credentials.CredentialManager
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.attemptAutoSignIn
import com.example.ui.screens.signOut
import com.google.firebase.auth.FirebaseAuth
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
import com.example.ui.components.AppDeepLinkDialog
import com.example.ui.components.ProductDetailSheet
import com.example.ui.screens.CartScreen
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

        // Capture the intent that opened the app
        val intentData: Uri? = intent?.data

        if (intentData != null) {
            // This will print e.g. "https://nehamycartapp.com"
            val fullLink = intentData.toString()

            // You can use this to route the user to a specific screen
            Log.d("MainActivity", "Deep link received: $fullLink")
            handleDeepLink(intentData)
        }

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
                val selectedProductDetail by viewModel.selectedProductDetail.collectAsState()
                val promoCode by viewModel.promoCode.collectAsState()
                val isPromoApplied by viewModel.promoApplied.collectAsState()
                val isSimulationPlaying by viewModel.isLiveSimulationPlaying.collectAsState()
                val snackbarEvent by viewModel.snackbarEvent.collectAsState()

                var showDeepLinkDialog by remember { mutableStateOf(false) }
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

                DisposableEffect(Unit) {
                    val auth = FirebaseAuth.getInstance()
                    val listener = FirebaseAuth.AuthStateListener { fbAuth ->
                        currentUser = fbAuth.currentUser
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
                            },
                            onUnauthenticated = {},
                            scope = coroutineScope
                        )
                    }
                }

                if (currentUser == null) {
                    AuthScreen(
                        onAuthSuccess = {
                            currentUser = FirebaseAuth.getInstance().currentUser
                            viewModel.showSnackbar("Welcome ${currentUser?.displayName ?: "back"}!")
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

                                    // Wishlist indicator button
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .padding(end = 4.dp)
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

                                    // Logout / Sign Out Button
                                    IconButton(
                                        onClick = {
                                            val credentialManager = CredentialManager.create(this@MainActivity)
                                            signOut(
                                                context = this@MainActivity,
                                                credentialManager = credentialManager,
                                                onSignOutComplete = {
                                                    currentUser = null
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

                            // 3. Live Tracking Tab
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
                                    onWishlistToggle = viewModel::toggleWishlist,
                                    onAddToCart = viewModel::addToCart,
                                    onNavigateToTracking = { viewModel.selectTab(AppNavTab.TRACKING) },
                                    onNavigateToRecommendations = { viewModel.selectTab(AppNavTab.RECOMMENDATIONS) },
                                    onOpenLinkTester = { showDeepLinkDialog = true }
                                )
                            }
                            AppNavTab.RECOMMENDATIONS -> {
                                RecommendationsScreen(
                                    recommendedProducts = recommendedProducts,
                                    wishlistIds = wishlistIds,
                                    userTaste = userTaste,
                                    onPersonaSelected = viewModel::updateUserPersona,
                                    onFilterToggle = viewModel::toggleTasteFilter,
                                    onProductClick = viewModel::openProductDetail,
                                    onWishlistToggle = viewModel::toggleWishlist,
                                    onAddToCart = viewModel::addToCart
                                )
                            }
                            AppNavTab.TRACKING -> {
                                OrderTrackingScreen(
                                    activeOrder = activeOrder,
                                    pastOrders = pastOrders,
                                    isSimulationPlaying = isSimulationPlaying,
                                    onToggleSimulation = viewModel::toggleLiveSimulation,
                                    onAdvanceSimulation = viewModel::advanceSimulationStep,
                                    onResetSimulation = viewModel::resetSimulation
                                )
                            }
                            AppNavTab.CART -> {
                                CartScreen(
                                    cartItems = cartItems,
                                    promoCode = promoCode,
                                    isPromoApplied = isPromoApplied,
                                    onUpdateQuantity = viewModel::updateCartQuantity,
                                    onApplyPromo = viewModel::applyPromoCode,
                                    onCheckout = { address -> viewModel.checkout(address) },
                                    onStartShopping = { viewModel.selectTab(AppNavTab.SHOP) }
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
                                onWishlistToggle = { viewModel.toggleWishlist(product.id) },
                                onAddToCart = { selectedColor ->
                                    viewModel.addToCart(product, selectedColor)
                                },
                                onBuyNow = { selectedColor ->
                                    viewModel.addToCart(product, selectedColor)
                                    viewModel.selectTab(AppNavTab.CART)
                                }
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
                    }
                }
            }
        }
    }
}

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val intentData: Uri? = intent.data
        if (intentData != null) {
            val fullLink = intentData.toString()
            Log.d("MainActivity", "Deep link received via onNewIntent: $fullLink")
            handleDeepLink(intentData)
        }
    }

    private fun handleDeepLink(uri: Uri) {
        val fullLink = uri.toString()
        val path = uri.path.orEmpty().lowercase()
        val host = uri.host.orEmpty().lowercase()
        when {
            path.startsWith("/profile") || host == "profile" || path.startsWith("/recommendations") || host == "recommendations" -> {
                viewModel.selectTab(AppNavTab.RECOMMENDATIONS)
            }
            path.startsWith("/cart") || host == "cart" -> {
                viewModel.selectTab(AppNavTab.CART)
            }
            path.startsWith("/tracking") || host == "tracking" || path.startsWith("/orders") || host == "orders" -> {
                viewModel.selectTab(AppNavTab.TRACKING)
            }
            else -> {
                viewModel.selectTab(AppNavTab.SHOP)
            }
        }
        viewModel.showSnackbar("Opened link: $fullLink")
    }
}
