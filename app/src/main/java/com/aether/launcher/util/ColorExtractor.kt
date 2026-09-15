package com.aether.launcher.util

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class WallpaperPalette(
    val primary: Color,
    val onPrimary: Color,
    val accent: Color,
    val background: Color,
)

private val fallback = WallpaperPalette(
    primary = Color(0xFF6A11CB),
    onPrimary = Color.White,
    accent = Color(0xFF2575FC),
    background = Color(0xFF0E0E12),
)

/** Reads the current wallpaper and extracts a small usable palette to drive Aether's dynamic theme. */
suspend fun extractWallpaperPalette(context: Context): WallpaperPalette = withContext(Dispatchers.Default) {
    runCatching {
        val wallpaperManager = WallpaperManager.getInstance(context)
        val drawable = wallpaperManager.drawable as? BitmapDrawable ?: return@runCatching fallback
        val bitmap: Bitmap = drawable.bitmap
        val palette = Palette.from(bitmap).generate()

        val vibrant = palette.vibrantSwatch ?: palette.dominantSwatch
        val muted = palette.mutedSwatch

        WallpaperPalette(
            primary = vibrant?.rgb?.let { Color(it) } ?: fallback.primary,
            onPrimary = vibrant?.bodyTextColor?.let { Color(it) } ?: fallback.onPrimary,
            accent = muted?.rgb?.let { Color(it) } ?: fallback.accent,
            background = fallback.background,
        )
    }.getOrDefault(fallback)
}
