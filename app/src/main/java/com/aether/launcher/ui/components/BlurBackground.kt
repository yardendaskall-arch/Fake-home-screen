package com.aether.launcher.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

/**
 * A frosted-glass surface: real backdrop blur on API 31+ (RenderEffect, via Modifier.blur),
 * degrading gracefully to a translucent scrim on older devices where live blur isn't available.
 */
@Composable
fun FrostedSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    tint: Color = Color.Black.copy(alpha = 0.28f),
    content: @Composable () -> Unit,
) {
    val supportsRealBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .then(if (supportsRealBlur) Modifier.blur(20.dp) else Modifier)
            .background(tint, shape)
    ) {
        content()
    }
}

@Composable
fun BlurredScrim(modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
    )
}
