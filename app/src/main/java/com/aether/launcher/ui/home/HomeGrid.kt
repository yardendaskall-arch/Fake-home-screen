package com.aether.launcher.ui.home

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.aether.launcher.data.model.AppInfo
import com.aether.launcher.data.model.HomeItem
import com.aether.launcher.ui.components.AppIconView

/**
 * A single home page as a fixed [columns] x [rows] grid. Long-press an icon to pick it up; drag
 * it to another cell and release to drop — the nearest empty cell under your finger wins.
 */
@Composable
fun HomeGrid(
    columns: Int,
    rows: Int,
    items: List<HomeItem>,
    appFor: (String) -> AppInfo?,
    badgedPackages: Set<String>,
    showLabels: Boolean,
    onAppClick: (AppInfo) -> Unit,
    onAppLongPressRemove: (HomeItem) -> Unit,
    onMove: (HomeItem, col: Int, row: Int) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var gridSizePx by remember { mutableStateOf(IntSize.Zero) }
    var draggingItem by remember { mutableStateOf<HomeItem?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
            .onSizeChanged { gridSizePx = it }
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { pos ->
                        val cellW = size.width / columns
                        val cellH = size.height / rows
                        val col = (pos.x / cellW).toInt().coerceIn(0, columns - 1)
                        val row = (pos.y / cellH).toInt().coerceIn(0, rows - 1)
                        draggingItem = items.firstOrNull { it.col == col && it.row == row }
                        dragOffset = Offset.Zero
                    },
                    onDrag = { change, delta ->
                        change.consume()
                        dragOffset += delta
                    },
                    onDragEnd = {
                        val item = draggingItem
                        if (item != null && gridSizePx.width > 0) {
                            val cellW = gridSizePx.width / columns
                            val cellH = gridSizePx.height / rows
                            val originX = item.col * cellW + cellW / 2 + dragOffset.x
                            val originY = item.row * cellH + cellH / 2 + dragOffset.y
                            val targetCol = (originX / cellW).toInt().coerceIn(0, columns - 1)
                            val targetRow = (originY / cellH).toInt().coerceIn(0, rows - 1)
                            val occupied = items.any { it.id != item.id && it.col == targetCol && it.row == targetRow }
                            if (!occupied) onMove(item, targetCol, targetRow)
                        }
                        draggingItem = null
                        dragOffset = Offset.Zero
                    },
                    onDragCancel = { draggingItem = null; dragOffset = Offset.Zero },
                )
            },
    ) {
        if (gridSizePx.width > 0) {
            val cellW = gridSizePx.width / columns
            val cellH = gridSizePx.height / rows
            val localDensity = androidx.compose.ui.platform.LocalDensity.current
            val cellWDp = with(localDensity) { cellW.toDp() }
            val cellHDp = with(localDensity) { cellH.toDp() }

            items.forEach { item ->
                val isDragging = item.id == draggingItem?.id
                val xPx = item.col * cellW + (if (isDragging) dragOffset.x.toInt() else 0)
                val yPx = item.row * cellH + (if (isDragging) dragOffset.y.toInt() else 0)

                Box(
                    modifier = Modifier
                        .offset { androidx.compose.ui.unit.IntOffset(xPx, yPx) }
                        .size(cellWDp, cellHDp)
                        .graphicsLayer { alpha = if (isDragging) 0.85f else 1f; scaleX = if (isDragging) 1.1f else 1f; scaleY = if (isDragging) 1.1f else 1f },
                    contentAlignment = Alignment.Center,
                ) {
                    when (item) {
                        is HomeItem.AppIcon -> {
                            val app = appFor(item.appKey)
                            if (app != null) {
                                AppIconView(
                                    app = app,
                                    iconSizeDp = 44.dp,
                                    showLabel = showLabels,
                                    showBadge = app.packageName in badgedPackages,
                                    onClick = { onAppClick(app) },
                                    onLongClick = { onAppLongPressRemove(item) },
                                )
                            }
                        }
                        is HomeItem.Folder -> {
                            FolderIconView(
                                folder = item,
                                appFor = appFor,
                                onAppClick = onAppClick,
                            )
                        }
                        is HomeItem.Widget -> {
                            com.aether.launcher.ui.widget.HostedWidgetView(item)
                        }
                    }
                }
            }
        }

        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp),
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Aether settings", tint = Color.White.copy(alpha = 0.55f))
        }
    }
}
