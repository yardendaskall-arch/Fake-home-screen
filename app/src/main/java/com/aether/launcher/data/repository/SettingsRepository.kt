package com.aether.launcher.data.repository

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.aether.launcher.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "aether_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val GRID_DENSITY = stringPreferencesKey("grid_density")
        val ICON_SHAPE = stringPreferencesKey("icon_shape")
        val ICON_SCALE = floatPreferencesKey("icon_scale")
        val SHOW_LABELS = booleanPreferencesKey("show_labels")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val BLUR_ENABLED = booleanPreferencesKey("blur_enabled")
        val ONE_HANDED = booleanPreferencesKey("one_handed")
        val FOCUS_MODE = booleanPreferencesKey("focus_mode")
        val SMART_SUGGESTIONS = booleanPreferencesKey("smart_suggestions")
        val NOTIFICATION_DOTS = booleanPreferencesKey("notification_dots")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val ICON_PACK = stringPreferencesKey("icon_pack_package")
        fun gestureKey(slot: GestureSlot) = stringPreferencesKey("gesture_${slot.name}")
    }

    val settings: Flow<LauncherSettings> = context.dataStore.data.map { prefs ->
        LauncherSettings(
            gridDensity = prefs[Keys.GRID_DENSITY]?.let { runCatching { GridDensity.valueOf(it) }.getOrNull() } ?: GridDensity.COZY,
            iconShape = prefs[Keys.ICON_SHAPE]?.let { runCatching { IconShape.valueOf(it) }.getOrNull() } ?: IconShape.SYSTEM,
            iconScale = prefs[Keys.ICON_SCALE] ?: 1.0f,
            showLabels = prefs[Keys.SHOW_LABELS] ?: true,
            useDynamicWallpaperColor = prefs[Keys.DYNAMIC_COLOR] ?: true,
            blurEnabled = prefs[Keys.BLUR_ENABLED] ?: true,
            oneHandedMode = prefs[Keys.ONE_HANDED] ?: false,
            focusModeEnabled = prefs[Keys.FOCUS_MODE] ?: false,
            smartSuggestionsEnabled = prefs[Keys.SMART_SUGGESTIONS] ?: true,
            notificationDotsEnabled = prefs[Keys.NOTIFICATION_DOTS] ?: true,
            appLockEnabled = prefs[Keys.APP_LOCK_ENABLED] ?: false,
            pinHash = prefs[Keys.PIN_HASH],
            selectedIconPackPackage = prefs[Keys.ICON_PACK],
            gestureMap = GestureSlot.entries.associateWith { slot ->
                prefs[Keys.gestureKey(slot)]?.let { runCatching { GestureAction.valueOf(it) }.getOrNull() }
                    ?: DEFAULT_GESTURE_MAP.getValue(slot)
            },
        )
    }

    suspend fun update(transform: (LauncherSettings) -> LauncherSettings) {
        // Read-modify-write against current snapshot, then persist every field back to DataStore.
        val current = settings.first()
        val next = transform(current)
        context.dataStore.edit { prefs ->
            prefs[Keys.GRID_DENSITY] = next.gridDensity.name
            prefs[Keys.ICON_SHAPE] = next.iconShape.name
            prefs[Keys.ICON_SCALE] = next.iconScale
            prefs[Keys.SHOW_LABELS] = next.showLabels
            prefs[Keys.DYNAMIC_COLOR] = next.useDynamicWallpaperColor
            prefs[Keys.BLUR_ENABLED] = next.blurEnabled
            prefs[Keys.ONE_HANDED] = next.oneHandedMode
            prefs[Keys.FOCUS_MODE] = next.focusModeEnabled
            prefs[Keys.SMART_SUGGESTIONS] = next.smartSuggestionsEnabled
            prefs[Keys.NOTIFICATION_DOTS] = next.notificationDotsEnabled
            prefs[Keys.APP_LOCK_ENABLED] = next.appLockEnabled
            next.pinHash?.let { prefs[Keys.PIN_HASH] = it } ?: prefs.remove(Keys.PIN_HASH)
            next.selectedIconPackPackage?.let { prefs[Keys.ICON_PACK] = it } ?: prefs.remove(Keys.ICON_PACK)
            next.gestureMap.forEach { (slot, action) -> prefs[Keys.gestureKey(slot)] = action.name }
        }
    }
}
