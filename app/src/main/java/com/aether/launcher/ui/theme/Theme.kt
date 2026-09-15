package com.aether.launcher.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.aether.launcher.util.WallpaperPalette

/**
 * Aether's color strategy, in priority order:
 *  1. Android 12+ Material You (colors extracted system-wide from the wallpaper)
 *  2. Our own wallpaper Palette extraction (works back to API 26, see ColorExtractor.kt)
 *  3. A static fallback gradient theme
 */
@Composable
fun AetherTheme(
    wallpaperPalette: WallpaperPalette?,
    useDynamicColor: Boolean,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            dynamicDarkColorScheme(context)
        wallpaperPalette != null -> darkColorScheme(
            primary = wallpaperPalette.primary,
            onPrimary = wallpaperPalette.onPrimary,
            secondary = wallpaperPalette.accent,
            background = wallpaperPalette.background,
            surface = wallpaperPalette.background,
        )
        else -> darkColorScheme(
            primary = Color(0xFF6A11CB),
            secondary = Color(0xFF2575FC),
            background = Color(0xFF0E0E12),
            surface = Color(0xFF0E0E12),
        )
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}
