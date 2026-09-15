package com.aether.launcher.data.model

enum class IconShape { CIRCLE, SQUIRCLE, ROUNDED_SQUARE, TEARDROP, SYSTEM }

enum class GridDensity(val columns: Int, val rows: Int) {
    COMPACT(4, 6),
    COZY(5, 6),
    ROOMY(4, 5),
    SPACIOUS(6, 7),
}

/** Everything the user can tune, persisted in DataStore via SettingsRepository. */
data class LauncherSettings(
    val gridDensity: GridDensity = GridDensity.COZY,
    val iconShape: IconShape = IconShape.SYSTEM,
    val iconScale: Float = 1.0f,
    val showLabels: Boolean = true,
    val useDynamicWallpaperColor: Boolean = true,
    val blurEnabled: Boolean = true,
    val oneHandedMode: Boolean = false,
    val focusModeEnabled: Boolean = false,
    val smartSuggestionsEnabled: Boolean = true,
    val notificationDotsEnabled: Boolean = true,
    val appLockEnabled: Boolean = false,
    val pinHash: String? = null,
    val selectedIconPackPackage: String? = null,
    val gestureMap: Map<GestureSlot, GestureAction> = DEFAULT_GESTURE_MAP,
)
