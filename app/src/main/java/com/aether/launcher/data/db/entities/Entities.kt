package com.aether.launcher.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "home_items")
data class HomeItemEntity(
    @PrimaryKey val id: String,
    val type: String, // "app" | "folder" | "widget"
    val page: Int,
    val col: Int,
    val row: Int,
    val spanX: Int,
    val spanY: Int,
    val appKey: String? = null,        // for type == app
    val folderName: String? = null,    // for type == folder
    val folderAppKeys: String? = null, // for type == folder, comma-joined app keys
    val appWidgetId: Int? = null,      // for type == widget
)

@Entity(tableName = "dock_items")
data class DockItemEntity(
    @PrimaryKey val id: String,
    val slot: Int,
    val appKey: String,
)

@Entity(tableName = "hidden_apps")
data class HiddenAppEntity(
    @PrimaryKey val appKey: String,
)

/** One row per app-open, feeding the on-device Smart Suggestions ranking. Nothing here ever leaves the device. */
@Entity(tableName = "app_launch_events")
data class AppLaunchEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val appKey: String,
    val timestampMillis: Long,
    val hourOfDay: Int,
    val dayOfWeek: Int,
)
