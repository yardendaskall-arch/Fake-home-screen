package com.aether.launcher.data.model

/** Anything that can occupy a cell (or block of cells) on a home page or in the dock. */
sealed interface HomeItem {
    val id: String
    val page: Int
    val col: Int
    val row: Int
    val spanX: Int
    val spanY: Int

    data class AppIcon(
        override val id: String,
        override val page: Int,
        override val col: Int,
        override val row: Int,
        val appKey: String,
    ) : HomeItem {
        override val spanX = 1
        override val spanY = 1
    }

    data class Folder(
        override val id: String,
        override val page: Int,
        override val col: Int,
        override val row: Int,
        val name: String,
        val appKeys: List<String>,
    ) : HomeItem {
        override val spanX = 1
        override val spanY = 1
    }

    data class Widget(
        override val id: String,
        override val page: Int,
        override val col: Int,
        override val row: Int,
        override val spanX: Int,
        override val spanY: Int,
        val appWidgetId: Int,
    ) : HomeItem
}

/** A pinned slot in the dock, independent of home pages. */
data class DockItem(
    val id: String,
    val slot: Int,
    val appKey: String,
)
