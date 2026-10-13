package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun OrderRouteCanvas(
    progress: Float, // 0.0 to 1.0
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "routeProgress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    // Light-mode city route canvas
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF1F5F9))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw subtle light street grid lines
            val gridColor = Color(0xFFE2E8F0)
            val strokeWidthGrid = 2.0f

            val stepX = width / 6f
            for (i in 1..5) {
                drawLine(
                    color = gridColor,
                    start = Offset(stepX * i, 0f),
                    end = Offset(stepX * i, height),
                    strokeWidth = strokeWidthGrid
                )
            }
            val stepY = height / 5f
            for (i in 1..4) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, stepY * i),
                    end = Offset(width, stepY * i),
                    strokeWidth = strokeWidthGrid
                )
            }

            // 2. Define route waypoints
            val p0 = Offset(width * 0.12f, height * 0.82f)
            val p1 = Offset(width * 0.28f, height * 0.65f)
            val p2 = Offset(width * 0.48f, height * 0.68f)
            val p3 = Offset(width * 0.68f, height * 0.35f)
            val p4 = Offset(width * 0.88f, height * 0.22f)

            val fullPath = Path().apply {
                moveTo(p0.x, p0.y)
                cubicTo(p1.x, p0.y, p1.x, p1.y, p2.x, p2.y)
                cubicTo(p2.x + 30f, p2.y - 40f, p3.x - 30f, p3.y + 40f, p3.x, p3.y)
                lineTo(p4.x, p4.y)
            }

            // Draw unvisited route path (dashed soft slate)
            drawPath(
                path = fullPath,
                color = Color(0xFFCBD5E1),
                style = Stroke(
                    width = 8f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f)
                )
            )

            // Approximate courier position along segments
            val courierPos = when {
                animatedProgress <= 0.25f -> {
                    val t = animatedProgress / 0.25f
                    Offset(p0.x + (p1.x - p0.x) * t, p0.y + (p1.y - p0.y) * t)
                }
                animatedProgress <= 0.55f -> {
                    val t = (animatedProgress - 0.25f) / 0.30f
                    Offset(p1.x + (p2.x - p1.x) * t, p1.y + (p2.y - p1.y) * t)
                }
                animatedProgress <= 0.80f -> {
                    val t = (animatedProgress - 0.55f) / 0.25f
                    Offset(p2.x + (p3.x - p2.x) * t, p2.y + (p3.y - p2.y) * t)
                }
                else -> {
                    val t = (animatedProgress - 0.80f) / 0.20f
                    Offset(p3.x + (p4.x - p3.x) * t, p3.y + (p4.y - p3.y) * t)
                }
            }

            // Draw completed route path
            val completedPath = Path().apply {
                moveTo(p0.x, p0.y)
                if (animatedProgress > 0.25f) lineTo(p1.x, p1.y)
                if (animatedProgress > 0.55f) lineTo(p2.x, p2.y)
                if (animatedProgress > 0.80f) lineTo(p3.x, p3.y)
                lineTo(courierPos.x, courierPos.y)
            }

            drawPath(
                path = completedPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF4F46E5), Color(0xFF0284C7), Color(0xFFEA580C)),
                    start = p0,
                    end = p4
                ),
                style = Stroke(
                    width = 10f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Waypoint 0: Dispatch Hub Marker
            drawCircle(
                color = Color(0xFF4F46E5),
                radius = 11f,
                center = p0
            )
            drawCircle(
                color = Color.White,
                radius = 5f,
                center = p0
            )

            // Waypoint 4: Destination Marker
            drawCircle(
                color = Color(0xFF10B981).copy(alpha = 0.25f),
                radius = 20f,
                center = p4
            )
            drawCircle(
                color = Color(0xFF10B981),
                radius = 11f,
                center = p4
            )
            drawCircle(
                color = Color.White,
                radius = 5f,
                center = p4
            )

            // Courier Pulse & Vehicle Marker
            if (animatedProgress < 1.0f) {
                drawCircle(
                    color = Color(0xFFEA580C).copy(alpha = pulseAlpha),
                    radius = pulseRadius * 1.5f,
                    center = courierPos
                )
                drawCircle(
                    color = Color(0xFFEA580C).copy(alpha = 0.35f),
                    radius = 18f,
                    center = courierPos
                )
                drawCircle(
                    color = Color(0xFFEA580C),
                    radius = 12f,
                    center = courierPos
                )
                drawCircle(
                    color = Color.White,
                    radius = 5f,
                    center = courierPos
                )
            } else {
                drawCircle(
                    color = Color(0xFF10B981),
                    radius = 16f,
                    center = p4
                )
                drawCircle(
                    color = Color.White,
                    radius = 6f,
                    center = p4
                )
            }
        }
    }
}
