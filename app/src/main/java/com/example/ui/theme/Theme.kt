package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light, crisp, airy color scheme adhering to "background keep it light colours"
private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = Color(0xFF312E81),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = OrangeFlame,
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC), // Clean, light porcelain background
    onBackground = TextDarkHigh,
    surface = Color(0xFFFFFFFF),    // Pure white cards & sheets
    onSurface = TextDarkHigh,
    surfaceVariant = Color(0xFFF1F5F9), // Soft light-gray container
    onSurfaceVariant = TextDarkMedium,
    outline = Color(0xFFE2E8F0)
)

@Composable
fun CartifyTheme(
    darkTheme: Boolean = false, // Forced false per user request to keep background light
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
