package com.example.ui.components

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

const val NETLIFY_DEPLOYED_URL = "https://nehamycartapp.netlify.app/"

/**
 * Interactive In-App Web Viewer for the deployed Netlify web application.
 * Allows the user to view, test, and browse the live deployed site directly
 * inside the app, with quick navigation, refresh, clipboard copy, and external browser launch.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun NetlifyWebStoreDialog(
    initialUrl: String = NETLIFY_DEPLOYED_URL,
    onDismissRequest: () -> Unit,
    onOpenNativeRoute: ((Uri) -> Unit)? = null,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    var currentUrl by remember { mutableStateOf(initialUrl) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadProgress by remember { mutableIntStateOf(0) }
    var hasError by remember { mutableStateOf(false) }
    var rendererCrashed by remember { mutableStateOf(false) }
    var webViewKey by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("netlify_web_store_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. Header with URL & Status
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF00C7B7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Neha My Cart",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF10B981)
                                        ) {
                                            Text(
                                                text = "NETLIFY LIVE",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Deployed Web Application",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Open in External Browser
                                IconButton(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl))
                                            context.startActivity(intent)
                                            onShowMessage("Opening in default browser...")
                                        } catch (e: Exception) {
                                            onShowMessage("Could not launch browser: ${e.message}")
                                        }
                                    },
                                    modifier = Modifier.testTag("netlify_open_browser_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInBrowser,
                                        contentDescription = "Open in Chrome/Browser",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // Refresh
                                IconButton(
                                    onClick = {
                                        hasError = false
                                        webViewInstance?.reload()
                                    },
                                    modifier = Modifier.testTag("netlify_refresh_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh Page"
                                    )
                                }

                                // Close
                                IconButton(
                                    onClick = onDismissRequest,
                                    modifier = Modifier.testTag("netlify_close_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close"
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // URL Address Bar Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "SSL Secure",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = currentUrl,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Copy link button
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Netlify Web URL", currentUrl)
                                            clipboard.setPrimaryClip(clip)
                                            onShowMessage("Copied $currentUrl to clipboard!")
                                        },
                                        modifier = Modifier.size(28.dp).testTag("netlify_copy_url_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Link",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    // Share link button
                                    IconButton(
                                        onClick = {
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, "Check out Neha My Cart live on Netlify: $currentUrl")
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Share Netlify Store"))
                                        },
                                        modifier = Modifier.size(28.dp).testTag("netlify_share_url_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share Link",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick Navigation Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val destinations = listOf(
                                "🏠 Storefront" to NETLIFY_DEPLOYED_URL,
                                "🛒 Cart" to "${NETLIFY_DEPLOYED_URL}cart",
                                "✨ AI Picks" to "${NETLIFY_DEPLOYED_URL}recommendations",
                                "📦 Orders" to "${NETLIFY_DEPLOYED_URL}orders"
                            )

                            destinations.forEach { (label, destUrl) ->
                                val isSelected = currentUrl == destUrl
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .clickable {
                                            currentUrl = destUrl
                                            hasError = false
                                            webViewInstance?.loadUrl(destUrl)
                                        }
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Loading Progress Indicator
                if (isLoading && loadProgress < 100) {
                    LinearProgressIndicator(
                        progress = { loadProgress / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 3. Main WebView or Error View
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (!rendererCrashed) {
                        androidx.compose.runtime.key(webViewKey) {
                            AndroidView(
                                factory = { ctx ->
                                    try {
                                        WebView(ctx).apply {
                                            settings.javaScriptEnabled = true
                                            settings.domStorageEnabled = true
                                            settings.loadWithOverviewMode = true
                                            settings.useWideViewPort = true
                                            settings.setSupportZoom(true)
                                            settings.builtInZoomControls = true
                                            settings.displayZoomControls = false
                                            settings.databaseEnabled = true
                                            settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                                            settings.loadsImagesAutomatically = true

                                            webViewClient = object : WebViewClient() {
                                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                                    super.onPageStarted(view, url, favicon)
                                                    isLoading = true
                                                    hasError = false
                                                    url?.let { currentUrl = it }
                                                }

                                                override fun onPageFinished(view: WebView?, url: String?) {
                                                    super.onPageFinished(view, url)
                                                    isLoading = false
                                                    url?.let { currentUrl = it }
                                                }

                                                override fun onReceivedError(
                                                    view: WebView?,
                                                    request: WebResourceRequest?,
                                                    error: WebResourceError?
                                                ) {
                                                    super.onReceivedError(view, request, error)
                                                    if (request?.isForMainFrame == true) {
                                                        isLoading = false
                                                        hasError = true
                                                        errorMessage = error?.description?.toString() ?: "Failed to connect to Netlify server"
                                                    }
                                                }

                                                /**
                                                 * CRITICAL: Handles Chromium sandboxed renderer process crashes (code -1) in emulator containers.
                                                 * Returning true prevents Android from terminating the host application process.
                                                 * Setting rendererCrashed = true unmounts AndroidView so Compose never touches the dead native view.
                                                 */
                                                override fun onRenderProcessGone(
                                                    view: WebView?,
                                                    detail: RenderProcessGoneDetail?
                                                ): Boolean {
                                                    isLoading = false
                                                    hasError = true
                                                    rendererCrashed = true
                                                    val didCrash = detail?.didCrash() ?: false
                                                    errorMessage = if (didCrash) {
                                                        "Chromium isolated renderer was reset by container memory limit. Tap 'Open in Browser' to view with full performance."
                                                    } else {
                                                        "Renderer was reclaimed by system. Tap 'Open in Browser' to view."
                                                    }
                                                    try {
                                                        (view?.parent as? ViewGroup)?.removeView(view)
                                                        view?.destroy()
                                                    } catch (_: Exception) {}
                                                    webViewInstance = null
                                                    return true
                                                }

                                                override fun shouldOverrideUrlLoading(
                                                    view: WebView?,
                                                    request: WebResourceRequest?
                                                ): Boolean {
                                                    val nextUrl = request?.url ?: return false
                                                    val scheme = nextUrl.scheme.orEmpty().lowercase()
                                                    if (scheme == "nehamycartapp" || scheme == "nehamycart" || scheme == "mycart") {
                                                        onOpenNativeRoute?.invoke(nextUrl)
                                                        onDismissRequest()
                                                        return true
                                                    }
                                                    return false
                                                }
                                            }

                                            webChromeClient = object : WebChromeClient() {
                                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                                    super.onProgressChanged(view, newProgress)
                                                    loadProgress = newProgress
                                                    if (newProgress >= 100) {
                                                        isLoading = false
                                                    }
                                                }
                                            }

                                            loadUrl(currentUrl)
                                            webViewInstance = this
                                        }
                                    } catch (t: Throwable) {
                                        hasError = true
                                        rendererCrashed = true
                                        errorMessage = "WebView could not be initialized in this environment. Tap 'Open in Browser'."
                                        View(ctx)
                                    }
                                },
                                update = { view ->
                                    // Update logic if needed
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Error fallback display if host cannot be reached or renderer process went away
                    if (hasError || rendererCrashed) {
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (rendererCrashed) "Container Renderer Memory Limit" else "Could not load Web Store",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = errorMessage.ifEmpty { "Check internet connection or open directly in your phone's browser." },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            hasError = false
                                            rendererCrashed = false
                                            isLoading = true
                                            webViewKey++
                                        }
                                    ) {
                                        Text("Retry In-App ↻")
                                    }
                                    Button(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                onShowMessage("Could not launch browser: ${e.message}")
                                            }
                                        },
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Text("Open in Browser ↗")
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Footer Bridge Notice
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🔗 Deployed at https://nehamycartapp.netlify.app",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onDismissRequest,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Return to App")
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                webViewInstance?.stopLoading()
                (webViewInstance?.parent as? ViewGroup)?.removeView(webViewInstance)
                webViewInstance?.destroy()
            } catch (_: Exception) {}
            webViewInstance = null
        }
    }
}
