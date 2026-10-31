package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DeepLinkOption(
    val title: String,
    val icon: String,
    val url: String,
    val description: String,
    val category: String = "Direct Scheme"
)

@Composable
fun AppDeepLinkDialog(
    onDismissRequest: () -> Unit,
    onTestLink: (String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var copiedUrl by remember { mutableStateOf<String?>(null) }

    val linkOptions = remember {
        listOf(
            DeepLinkOption(
                title = "Netlify Live Web App",
                icon = "🌐",
                url = "https://nehamycartapp.netlify.app/",
                description = "Live deployed site on Netlify (tap to test deep link or open)",
                category = "Netlify Web App"
            ),
            DeepLinkOption(
                title = "Netlify Web Cart",
                icon = "🛒",
                url = "https://nehamycartapp.netlify.app/cart",
                description = "Opens cart route on deployed Netlify web app or native app",
                category = "Netlify Web App"
            ),
            DeepLinkOption(
                title = "Netlify Product: Sony ANC",
                icon = "🎧",
                url = "https://nehamycartapp.netlify.app/product?id=prod_1",
                description = "Opens Sony WH-1000XM5 on netlify.app",
                category = "Netlify Web App"
            ),
            DeepLinkOption(
                title = "Netlify AI Picks",
                icon = "✨",
                url = "https://nehamycartapp.netlify.app/recommendations",
                description = "Opens personalized curation route on netlify.app",
                category = "Netlify Web App"
            ),
            DeepLinkOption(
                title = "App Launch (Direct Scheme)",
                icon = "⚡",
                url = "nehamycartapp://open",
                description = "Bypasses web DNS; guaranteed instant opening on any device",
                category = "Direct App Scheme"
            ),
            DeepLinkOption(
                title = "Cart & Checkout",
                icon = "🛒",
                url = "nehamycartapp://cart",
                description = "Opens shopping cart and checkout directly",
                category = "Direct App Scheme"
            ),
            DeepLinkOption(
                title = "Product: Sony WH-1000XM5",
                icon = "🎧",
                url = "nehamycartapp://product?id=prod_1",
                description = "Opens product details sheet directly",
                category = "Direct App Scheme"
            ),
            DeepLinkOption(
                title = "Product: MacBook Air M3",
                icon = "💻",
                url = "nehamycartapp://product?id=prod_3",
                description = "Deep links straight to product modal",
                category = "Direct App Scheme"
            ),
            DeepLinkOption(
                title = "AI Recommendations",
                icon = "✨",
                url = "nehamycartapp://recommendations",
                description = "Opens personalized curation and taste profile",
                category = "Direct App Scheme"
            ),
            DeepLinkOption(
                title = "Live Order Tracking",
                icon = "🚚",
                url = "nehamycartapp://tracking",
                description = "Opens real-time simulated order tracking",
                category = "Direct App Scheme"
            ),
            DeepLinkOption(
                title = "Web Domain App Link",
                icon = "🌐",
                url = "https://nehamycartapp.com/cart",
                description = "Registered Android intent filter for nehamycartapp.com",
                category = "Web / Firebase"
            ),
            DeepLinkOption(
                title = "Firebase Dynamic Link",
                icon = "🔥",
                url = "https://nehamycartapp.page.link/cart",
                description = "Requires domain prefix registered in Firebase Console",
                category = "Web / Firebase"
            )
        )
    }

    val adbSampleCommand = "adb shell am start -W -a android.intent.action.VIEW -d \"nehamycartapp://cart\" com.aistudio.cartify.kxpqz"

    val kotlinSnippet = """// 1. In MainActivity.kt:
FirebaseDynamicLinks.getInstance()
    .getDynamicLink(intent)
    .addOnSuccessListener(this) { pendingData ->
        val dynamicLink = pendingData?.link
        val targetUri = dynamicLink ?: intent?.data
        targetUri?.let { handleDeepLink(it) }
    }
    .addOnFailureListener(this) {
        intent?.data?.let { handleDeepLink(it) }
    }"""

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.testTag("app_deep_link_dialog"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "App Deep Links & Firebase",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "nehamycartapp:// & Dynamic Links",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tab Selection: Quick Links vs Firebase Guide
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Quick Links", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Firebase & Kotlin Guide", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }

                if (selectedTab == 0) {
                    // INFO CARD EXPLAINING "Dynamic Link Not Found"
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(top = 2.dp)
                            )
                            Column {
                                Text(
                                    text = "Why 'Dynamic Link Not Found' Appears",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Google's page.link requires claiming 'nehamycartapp.page.link' in the Firebase Console (Dynamic Links > Get Started) before it goes live on the web.\n\nUse the 'nehamycartapp://' direct schemes below! They bypass web servers completely and open the app instantly on any phone or emulator.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    Text(
                        text = "Tap any link to test in-app routing:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Clickable Links List
                    linkOptions.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onTestLink(item.url)
                                    onDismissRequest()
                                }
                                .testTag("deep_link_item_${item.title.lowercase().replace(" ", "_")}")
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = item.icon, fontSize = 20.sp)
                                    Column {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = item.url,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = item.description,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                            val clip = ClipData.newPlainText("Deep Link", item.url)
                                            clipboard?.setPrimaryClip(clip)
                                            copiedUrl = item.url
                                            onShowMessage("Copied ${item.url}")
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (copiedUrl == item.url) Icons.Default.Check else Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Launch,
                                        contentDescription = "Test Link",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // FIREBASE & KOTLIN INTEGRATION GUIDE
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text("How Kotlin Resolves Firebase Links", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                            }
                            Text(
                                text = "Your app is configured with FirebaseDynamicLinks SDK in Kotlin to intercept and unwrap links from both onCreate and onNewIntent:",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = kotlinSnippet,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // ADB Terminal Test
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Terminal, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text("Test via ADB Command Line", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                            }
                            Text(
                                text = "To simulate an incoming link trigger from terminal or external intent:",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = adbSampleCommand,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                            val clip = ClipData.newPlainText("ADB Command", adbSampleCommand)
                                            clipboard?.setPrimaryClip(clip)
                                            onShowMessage("Copied ADB command!")
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy ADB command", modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Firebase Console Steps
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Steps to make page.link active:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            Text("1. Open console.firebase.google.com with project gen-lang-client-0552066446", fontSize = 11.sp)
                            Text("2. Navigate to Dynamic Links in left sidebar", fontSize = 11.sp)
                            Text("3. Click 'Get Started' and enter 'nehamycartapp.page.link' as your URL prefix", fontSize = 11.sp)
                            Text("4. Once saved, Google's DNS will resolve the domain and forward clicks directly to your app!", fontSize = 11.sp)
                        }
                    }
                }

                // Copy & Share buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            val clip = ClipData.newPlainText("App Deep Link", "nehamycartapp://cart")
                            clipboard?.setPrimaryClip(clip)
                            copiedUrl = "nehamycartapp://cart"
                            onShowMessage("Copied nehamycartapp://cart to clipboard!")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_link_button"),
                        contentPadding = ButtonDefaults.TextButtonContentPadding
                    ) {
                        Icon(
                            imageVector = if (copiedUrl != null) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (copiedUrl != null) "Copied" else "Copy Scheme", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_SUBJECT,
                                    "Shop on Neha My Cart"
                                )
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Check out Neha My Cart! Open app directly: nehamycartapp://open or view cart: nehamycartapp://cart"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share App Link"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_link_button"),
                        contentPadding = ButtonDefaults.TextButtonContentPadding
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Link", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.testTag("dismiss_dialog_button")
            ) {
                Text("Close")
            }
        }
    )
}
