package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Product
import com.example.util.CurrencyUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// 3D Math structures
private data class Vec3(val x: Float, val y: Float, val z: Float) {
    fun rotateY(angleRad: Float): Vec3 {
        val cosA = cos(angleRad)
        val sinA = sin(angleRad)
        return Vec3(
            x = x * cosA + z * sinA,
            y = y,
            z = -x * sinA + z * cosA
        )
    }

    fun rotateX(angleRad: Float): Vec3 {
        val cosA = cos(angleRad)
        val sinA = sin(angleRad)
        return Vec3(
            x = x,
            y = y * cosA - z * sinA,
            z = y * sinA + z * cosA
        )
    }

    fun subtract(other: Vec3): Vec3 = Vec3(x - other.x, y - other.y, z - other.z)

    fun cross(other: Vec3): Vec3 = Vec3(
        x = y * other.z - z * other.y,
        y = z * other.x - x * other.z,
        z = x * other.y - y * other.x
    )

    fun normalize(): Vec3 {
        val len = sqrt(x * x + y * y + z * z).coerceAtLeast(0.0001f)
        return Vec3(x / len, y / len, z / len)
    }

    fun dot(other: Vec3): Float = x * other.x + y * other.y + z * other.z
}

private data class Face3D(
    val vertexIndices: List<Int>,
    val colorTintModifier: Float = 1.0f
)

private data class Mesh3D(
    val vertices: List<Vec3>,
    val faces: List<Face3D>
)

/**
 * Builds a 3D geometry mesh tailored to the product category/type.
 */
private fun buildCategoryMesh(product: Product): Mesh3D {
    val categoryLower = product.category.lowercase()
    val titleLower = product.title.lowercase()

    return when {
        titleLower.contains("watch") || categoryLower.contains("wearable") -> {
            // Cylindrical Smart Watch with dial and wristband
            val verts = mutableListOf<Vec3>()
            val faces = mutableListOf<Face3D>()
            val segments = 12
            val radius = 1.0f
            val halfHeight = 0.25f

            // Top dial ring
            for (i in 0 until segments) {
                val a = (i.toFloat() / segments) * 2f * Math.PI.toFloat()
                verts.add(Vec3(radius * cos(a), -halfHeight, radius * sin(a)))
            }
            // Bottom dial ring
            for (i in 0 until segments) {
                val a = (i.toFloat() / segments) * 2f * Math.PI.toFloat()
                verts.add(Vec3(radius * cos(a), halfHeight, radius * sin(a)))
            }
            // Watch glass center
            val topCenterIdx = verts.size
            verts.add(Vec3(0f, -halfHeight - 0.05f, 0f))
            val bottomCenterIdx = verts.size
            verts.add(Vec3(0f, halfHeight + 0.05f, 0f))

            // Dial side faces
            for (i in 0 until segments) {
                val next = (i + 1) % segments
                faces.add(Face3D(listOf(i, next, segments + next, segments + i), 0.85f))
                // Top cap
                faces.add(Face3D(listOf(topCenterIdx, i, next), 1.15f))
                // Bottom cap
                faces.add(Face3D(listOf(bottomCenterIdx, segments + next, segments + i), 0.7f))
            }

            // Strap top
            val s1 = verts.size
            verts.add(Vec3(-0.45f, -halfHeight, -radius))
            verts.add(Vec3(0.45f, -halfHeight, -radius))
            verts.add(Vec3(0.40f, -halfHeight + 0.05f, -radius - 1.2f))
            verts.add(Vec3(-0.40f, -halfHeight + 0.05f, -radius - 1.2f))
            faces.add(Face3D(listOf(s1, s1 + 1, s1 + 2, s1 + 3), 0.6f))

            // Strap bottom
            val s2 = verts.size
            verts.add(Vec3(-0.45f, halfHeight, radius))
            verts.add(Vec3(0.45f, halfHeight, radius))
            verts.add(Vec3(0.40f, halfHeight - 0.05f, radius + 1.2f))
            verts.add(Vec3(-0.40f, halfHeight - 0.05f, radius + 1.2f))
            faces.add(Face3D(listOf(s2, s2 + 3, s2 + 2, s2 + 1), 0.6f))

            Mesh3D(verts, faces)
        }
        titleLower.contains("headphone") || categoryLower.contains("audio") -> {
            // Over-ear headphones: Headband arch + dual acoustic earcups
            val verts = mutableListOf<Vec3>()
            val faces = mutableListOf<Face3D>()

            // Headband arch points
            val archSegments = 8
            for (i in 0..archSegments) {
                val t = i.toFloat() / archSegments
                val angle = Math.PI.toFloat() * (0.1f + t * 0.8f)
                val y = -1.1f * sin(angle) - 0.2f
                val x = 1.3f * cos(angle)
                verts.add(Vec3(x, y, -0.1f))
                verts.add(Vec3(x, y, 0.1f))
            }
            for (i in 0 until archSegments) {
                val idx = i * 2
                faces.add(Face3D(listOf(idx, idx + 1, idx + 3, idx + 2), 0.75f))
            }

            // Left Cup
            val leftCupStart = verts.size
            addCylinder(verts, faces, center = Vec3(-1.1f, 0.3f, 0f), radius = 0.55f, depth = 0.4f, axis = 'X')

            // Right Cup
            val rightCupStart = verts.size
            addCylinder(verts, faces, center = Vec3(1.1f, 0.3f, 0f), radius = 0.55f, depth = 0.4f, axis = 'X')

            Mesh3D(verts, faces)
        }
        titleLower.contains("shoe") || titleLower.contains("sneaker") || categoryLower.contains("footwear") -> {
            // Aerodynamic athletic sneaker 3D mesh
            val verts = listOf(
                // Sole bottom
                Vec3(-0.6f, 0.8f, -1.3f), Vec3(0.6f, 0.8f, -1.3f), // Heel bottom
                Vec3(0.7f, 0.8f, 0.4f), Vec3(-0.7f, 0.8f, 0.4f),   // Mid bottom
                Vec3(0.5f, 0.7f, 1.4f), Vec3(-0.5f, 0.7f, 1.4f),   // Toe bottom
                // Sole top
                Vec3(-0.55f, 0.5f, -1.25f), Vec3(0.55f, 0.5f, -1.25f),
                Vec3(0.65f, 0.5f, 0.4f), Vec3(-0.65f, 0.5f, 0.4f),
                Vec3(0.45f, 0.45f, 1.35f), Vec3(-0.45f, 0.45f, 1.35f),
                // Upper / Ankle collar
                Vec3(-0.45f, -0.5f, -0.9f), Vec3(0.45f, -0.5f, -0.9f), // Heel collar
                Vec3(0.4f, -0.2f, 0.1f), Vec3(-0.4f, -0.2f, 0.1f),     // Tongue
                Vec3(0.35f, 0.1f, 0.9f), Vec3(-0.35f, 0.1f, 0.9f)      // Toe cap
            )
            val faces = listOf(
                // Sole bottom
                Face3D(listOf(0, 1, 2, 3), 0.5f),
                Face3D(listOf(3, 2, 4, 5), 0.5f),
                // Sole walls
                Face3D(listOf(0, 6, 7, 1), 0.7f),
                Face3D(listOf(1, 7, 8, 2), 0.75f),
                Face3D(listOf(2, 8, 10, 4), 0.8f),
                Face3D(listOf(4, 10, 11, 5), 0.85f),
                Face3D(listOf(5, 11, 9, 3), 0.8f),
                Face3D(listOf(3, 9, 6, 0), 0.75f),
                // Upper body
                Face3D(listOf(6, 12, 13, 7), 0.95f),
                Face3D(listOf(7, 13, 14, 8), 1.0f),
                Face3D(listOf(8, 14, 16, 10), 1.05f),
                Face3D(listOf(10, 16, 17, 11), 1.1f),
                Face3D(listOf(11, 17, 15, 9), 1.05f),
                Face3D(listOf(9, 15, 12, 6), 1.0f),
                // Tongue & Top
                Face3D(listOf(12, 13, 14, 15), 0.9f),
                Face3D(listOf(15, 14, 16, 17), 1.15f)
            )
            Mesh3D(verts, faces)
        }
        else -> {
            // Sleek faceted rounded tech prism / modern product box
            val verts = listOf(
                Vec3(-0.9f, -1.2f, -0.7f), Vec3(0.9f, -1.2f, -0.7f),
                Vec3(0.9f, -1.2f, 0.7f), Vec3(-0.9f, -1.2f, 0.7f),
                Vec3(-1.0f, 1.2f, -0.8f), Vec3(1.0f, 1.2f, -0.8f),
                Vec3(1.0f, 1.2f, 0.8f), Vec3(-1.0f, 1.2f, 0.8f),
                // Beveled top facet
                Vec3(0f, -1.35f, 0f),
                // Front detail plate
                Vec3(-0.6f, -0.7f, 0.72f), Vec3(0.6f, -0.7f, 0.72f),
                Vec3(0.6f, 0.7f, 0.82f), Vec3(-0.6f, 0.7f, 0.82f)
            )
            val faces = listOf(
                // Front
                Face3D(listOf(3, 2, 6, 7), 1.0f),
                // Back
                Face3D(listOf(1, 0, 4, 5), 0.7f),
                // Left
                Face3D(listOf(0, 3, 7, 4), 0.85f),
                // Right
                Face3D(listOf(2, 1, 5, 6), 0.9f),
                // Bottom
                Face3D(listOf(4, 7, 6, 5), 0.5f),
                // Top pyramid facets
                Face3D(listOf(8, 0, 1), 1.15f),
                Face3D(listOf(8, 1, 2), 1.25f),
                Face3D(listOf(8, 2, 3), 1.2f),
                Face3D(listOf(8, 3, 0), 1.1f),
                // Front accent panel
                Face3D(listOf(9, 10, 11, 12), 1.35f)
            )
            Mesh3D(verts, faces)
        }
    }
}

private fun addCylinder(
    verts: MutableList<Vec3>,
    faces: MutableList<Face3D>,
    center: Vec3,
    radius: Float,
    depth: Float,
    axis: Char
) {
    val startIdx = verts.size
    val segs = 8
    val halfD = depth / 2f

    for (i in 0 until segs) {
        val a = (i.toFloat() / segs) * 2f * Math.PI.toFloat()
        val c = radius * cos(a)
        val s = radius * sin(a)
        if (axis == 'X') {
            verts.add(Vec3(center.x - halfD, center.y + c, center.z + s))
            verts.add(Vec3(center.x + halfD, center.y + c, center.z + s))
        }
    }

    for (i in 0 until segs) {
        val i1 = startIdx + i * 2
        val i2 = startIdx + i * 2 + 1
        val nextI = (i + 1) % segs
        val i3 = startIdx + nextI * 2 + 1
        val i4 = startIdx + nextI * 2
        faces.add(Face3D(listOf(i1, i2, i3, i4), 0.9f))
    }
}

@Composable
fun Product3DViewerDialog(
    product: Product,
    onDismissRequest: () -> Unit,
    onAddToCart: (selectedColor: Long) -> Unit
) {
    var rotationY by remember { mutableFloatStateOf(25f) }
    var rotationX by remember { mutableFloatStateOf(-15f) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var isWireframe by remember { mutableStateOf(false) }
    var isAutoSpinning by remember { mutableStateOf(true) }

    val colorOptions = remember(product) {
        listOf(
            product.primaryColorHex,
            0xFF1E293B, // Matte Obsidian
            0xFF2563EB, // Royal Blue
            0xFF059669, // Emerald Green
            0xFFE11D48, // Crimson Red
            0xFFD97706  // Amber Gold
        ).distinct()
    }

    var selectedColor by remember(product) { mutableStateOf(product.primaryColorHex) }
    val mesh = remember(product) { buildCategoryMesh(product) }

    // Auto-spin coroutine
    LaunchedEffect(isAutoSpinning) {
        while (isActive && isAutoSpinning) {
            delay(16L) // ~60fps
            rotationY = (rotationY + 0.6f) % 360f
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("dialog_3d_viewer"),
            color = Color(0xFF0F172A) // Sleek dark studio backdrop
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background radial glow
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(selectedColor).copy(alpha = 0.35f),
                                Color(0xFF0F172A).copy(alpha = 0.95f),
                                Color(0xFF020617)
                            ),
                            center = center,
                            radius = size.minDimension * 0.7f
                        )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ViewInAr,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "3D 360° VIEW",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            Text(
                                text = product.category.uppercase(),
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier
                                .testTag("btn_close_3d")
                                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close 3D View",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Title & INR Price
                    Text(
                        text = product.title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = CurrencyUtil.formatInr(product.price),
                        color = Color(0xFF38BDF8),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )

                    // Main 3D Canvas Area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    isAutoSpinning = false
                                    zoomScale = (zoomScale * zoom).coerceIn(0.6f, 2.2f)
                                    rotationY += pan.x * 0.4f
                                    rotationX = (rotationX - pan.y * 0.4f).coerceIn(-80f, 80f)
                                }
                            }
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { isAutoSpinning = false },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        rotationY += dragAmount.x * 0.5f
                                        rotationX = (rotationX - dragAmount.y * 0.5f).coerceIn(-80f, 80f)
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().testTag("canvas_3d_render")) {
                            render3DScene(
                                mesh = mesh,
                                rotXDeg = rotationX,
                                rotYDeg = rotationY,
                                scale = zoomScale,
                                isWireframe = isWireframe,
                                baseColor = Color(selectedColor)
                            )
                        }

                        // Gentle hint overlay
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Black.copy(alpha = 0.5f),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = "👆 Drag to Rotate 360° • Pinch to Zoom",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Interactive Controls Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Auto-spin toggle chip
                        FilterChip(
                            selected = isAutoSpinning,
                            onClick = { isAutoSpinning = !isAutoSpinning },
                            label = { Text(if (isAutoSpinning) "Spinning" else "Paused") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )

                        // Wireframe / Shaded toggle
                        FilterChip(
                            selected = isWireframe,
                            onClick = { isWireframe = !isWireframe },
                            label = { Text(if (isWireframe) "Wireframe" else "Solid Shaded") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White
                            )
                        )

                        // Reset orientation
                        IconButton(
                            onClick = {
                                rotationX = -15f
                                rotationY = 25f
                                zoomScale = 1.0f
                                isAutoSpinning = true
                            },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.12f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset Angle",
                                tint = Color.White
                            )
                        }
                    }

                    // Material Color Swatches
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "COLOR FINISH",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            colorOptions.forEach { colorHex ->
                                val isSelected = selectedColor == colorHex
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(colorHex))
                                        .clickable { selectedColor = colorHex }
                                        .then(
                                            if (isSelected) Modifier.border(3.dp, Color.White, CircleShape)
                                            else Modifier.border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(Color.White, CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bottom Action Button: Add to Cart directly from 3D View
                    Button(
                        onClick = {
                            onAddToCart(selectedColor)
                            onDismissRequest()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_3d_add_to_cart"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add to Cart • ${CurrencyUtil.formatInr(product.price)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.render3DScene(
    mesh: Mesh3D,
    rotXDeg: Float,
    rotYDeg: Float,
    scale: Float,
    isWireframe: Boolean,
    baseColor: Color
) {
    val rotXRad = Math.toRadians(rotXDeg.toDouble()).toFloat()
    val rotYRad = Math.toRadians(rotYDeg.toDouble()).toFloat()

    val fovDistance = 4.5f
    val baseScale = size.minDimension * 0.28f * scale
    val centerX = size.width / 2f
    val centerY = size.height / 2f

    // Directional light vector pointing from top-right-front
    val lightDir = Vec3(0.577f, -0.65f, 0.50f).normalize()

    // Transform and project all vertices
    val transformed = mesh.vertices.map { v ->
        v.rotateY(rotYRad).rotateX(rotXRad)
    }

    val projected = transformed.map { v ->
        val zDist = (v.z + fovDistance).coerceAtLeast(0.5f)
        val projFactor = fovDistance / zDist
        Offset(
            x = centerX + v.x * baseScale * projFactor,
            y = centerY + v.y * baseScale * projFactor
        )
    }

    // Shadow on the ground
    drawOval(
        color = Color.Black.copy(alpha = 0.45f),
        topLeft = Offset(centerX - baseScale * 0.85f, centerY + baseScale * 1.15f),
        size = androidx.compose.ui.geometry.Size(baseScale * 1.7f, baseScale * 0.4f)
    )

    // Calculate face depth and normal for backface culling / depth sorting
    data class ProjectedFace(
        val face: Face3D,
        val avgZ: Float,
        val normal: Vec3,
        val isFrontFacing: Boolean
    )

    val faceData = mesh.faces.map { face ->
        val v0 = transformed[face.vertexIndices[0]]
        val v1 = transformed[face.vertexIndices[1]]
        val v2 = transformed[face.vertexIndices[2]]

        val edge1 = v1.subtract(v0)
        val edge2 = v2.subtract(v0)
        val normal = edge1.cross(edge2).normalize()

        val avgZ = face.vertexIndices.map { transformed[it].z }.average().toFloat()
        // In this camera coordinate system, front-facing faces have normal.z < 0 or > 0 depending on winding
        val isFront = normal.z < 0.1f

        ProjectedFace(face, avgZ, normal, isFront)
    }

    // Painter's algorithm: sort faces from back to front (largest Z to smallest Z)
    val sortedFaces = faceData.sortedByDescending { it.avgZ }

    for (item in sortedFaces) {
        val face = item.face
        val path = Path().apply {
            val firstPt = projected[face.vertexIndices.first()]
            moveTo(firstPt.x, firstPt.y)
            for (i in 1 until face.vertexIndices.size) {
                val pt = projected[face.vertexIndices[i]]
                lineTo(pt.x, pt.y)
            }
            close()
        }

        if (isWireframe) {
            drawPath(
                path = path,
                color = Color(0xFF38BDF8),
                style = Stroke(width = 2.dp.toPx())
            )
        } else {
            // Lighting calculation (ambient + diffuse + specular)
            val nDotL = item.normal.dot(lightDir).coerceAtLeast(0f)
            val ambient = 0.35f
            val diffuse = 0.65f * nDotL
            val brightness = ((ambient + diffuse) * face.colorTintModifier).coerceIn(0.15f, 1.4f)

            val shadedColor = Color(
                red = (baseColor.red * brightness).coerceIn(0f, 1f),
                green = (baseColor.green * brightness).coerceIn(0f, 1f),
                blue = (baseColor.blue * brightness).coerceIn(0f, 1f),
                alpha = 0.95f
            )

            // Fill face
            drawPath(path = path, color = shadedColor)

            // Edge highlight outline for crisp polygonal definition
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.18f),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}
