package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Clay Animation color scheme: warm ceramic dough canvas with rich polymer clay accents
private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF0FF),
    onPrimaryContainer = Color(0xFF2A2E9E),
    secondary = ClayCoralAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFECEC),
    onSecondaryContainer = Color(0xFF9E1B1B),
    tertiary = ClayMintAccent,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE6F9F3),
    onTertiaryContainer = Color(0xFF0D6B50),
    background = Color(0xFFF7F5F0), // Handcrafted clay dough canvas
    onBackground = TextDarkHigh,
    surface = Color(0xFFFFFFFF),    // Porcelain white molded clay cards
    onSurface = TextDarkHigh,
    surfaceVariant = Color(0xFFEFECE4), // Soft ceramic container
    onSurfaceVariant = TextDarkMedium,
    outline = Color(0xFFDDD8CD)
)

@Composable
fun CartifyTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
