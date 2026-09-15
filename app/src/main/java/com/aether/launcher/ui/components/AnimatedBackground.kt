package com.aether.launcher.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Aether's signature look: two soft, slowly-drifting color blobs behind the whole home screen,
 * tinted from the current Material You / wallpaper-derived color scheme. Pure GPU-cheap radial
 * gradients on a Canvas — no bitmaps, no blur — so it costs nothing to keep animating forever.
 */
@Composable
fun AnimatedAuroraBackground(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    val transition = rememberInfiniteTransition(label = "aurora")
    val t1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Reverse),
        label = "auroraDrift1",
    )
    val t2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(19000, easing = LinearEasing), RepeatMode.Reverse),
        label = "auroraDrift2",
    )

    // A soft scrim (not opaque) so the user's real wallpaper still shows through underneath —
    // this is a glow layer, not a replacement background.
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(Color(0xFF07070B).copy(alpha = 0.45f))
        drawBlob(center = Offset(size.width * (0.15f + 0.25f * t1), size.height * (0.18f + 0.10f * t2)), radius = size.maxDimension * 0.55f, color = primary)
        drawBlob(center = Offset(size.width * (0.85f - 0.20f * t2), size.height * (0.75f - 0.15f * t1)), radius = size.maxDimension * 0.5f, color = secondary)
    }
}

private fun DrawScope.drawBlob(center: Offset, radius: Float, color: Color) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.38f), color.copy(alpha = 0f)),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}
