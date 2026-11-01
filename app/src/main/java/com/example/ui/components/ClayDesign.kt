package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Clay Animation Design Template:
 * Tactile, puffy, squishy 3D clay components inspired by claymation (plasticine stop-motion).
 * Features soft dual lighting (top-left highlight + bottom-right shadow) and bouncy spring physics.
 */

// Clay Theme Color Constants
val ClayWarmDough = Color(0xFFF7F5F0)
val ClayPureWhite = Color(0xFFFFFFFF)
val ClayIndigo = Color(0xFF5256EC)
val ClayCoral = Color(0xFFFF6B6B)
val ClayMint = Color(0xFF20C997)
val ClayHoney = Color(0xFFFAB005)
val ClaySky = Color(0xFF38BDF8)
val ClayDarkText = Color(0xFF1E293B)
val ClayMutedText = Color(0xFF64748B)

/**
 * Modifier that adds tactile clay squish-and-bounce spring animation on press.
 */
@Composable
fun Modifier.claySquish(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    squishScale: Float = 0.95f
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) squishScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "clay_squish_scale"
    )
    return this.scale(scale)
}

/**
 * Sculpted Clay Card with soft 3D elevation, rounded corners, and subtle top bevel highlight.
 */
@Composable
fun ClayCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    elevation: Dp = 6.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val baseModifier = modifier
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = Color(0x33A0ACC3),
            spotColor = Color(0x33A0ACC3)
        )
        .clip(shape)
        .background(containerColor)
        .then(
            if (onClick != null) {
                Modifier
                    .claySquish(interactionSource)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
            } else Modifier
        )

    Box(modifier = baseModifier) {
        // Sculpted clay top highlight stroke overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.55f),
                            Color.Transparent,
                            Color(0xFFCBD5E1).copy(alpha = 0.15f)
                        )
                    )
                )
        )
        content()
    }
}

/**
 * Tactile Clay Pill for category filters and interactive tabs.
 */
@Composable
fun ClayPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    selectedColor: Color = ClayIndigo,
    selectedTextColor: Color = Color.White
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pill_scale"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) selectedColor else MaterialTheme.colorScheme.surface,
        border = if (isSelected) {
            BorderStroke(1.5.dp, Color.White.copy(alpha = 0.4f))
        } else {
            BorderStroke(1.dp, Color(0xFFE2E8F0))
        },
        shadowElevation = if (isSelected) 5.dp else 2.dp,
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                leadingIcon()
            }
            Text(
                text = label,
                color = if (isSelected) selectedTextColor else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
    }
}

/**
 * 3D Clay Button with plasticine bottom depth and squishy press physics.
 */
@Composable
fun ClayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = ClayIndigo,
    contentColor: Color = Color.White,
    shape: Shape = RoundedCornerShape(18.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "clay_btn_scale"
    )

    Surface(
        shape = shape,
        color = containerColor,
        shadowElevation = if (isPressed) 2.dp else 6.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box {
            // Subtle top highlight
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Row(
                modifier = Modifier.padding(contentPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                content()
            }
        }
    }
}

/**
 * Small Clay 3D Badge for tags and highlights (e.g. "🎨 Clay 3D", "Handcrafted").
 */
@Composable
fun ClayBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = ClayCoral,
    textColor: Color = Color.White,
    fontSize: Int = 10
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            letterSpacing = 0.5.sp
        )
    }
}
