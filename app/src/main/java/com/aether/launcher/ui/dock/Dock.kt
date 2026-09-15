package com.aether.launcher.ui.dock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aether.launcher.data.model.AppInfo
import com.aether.launcher.data.model.DockItem
import com.aether.launcher.ui.components.AppIconView
import com.aether.launcher.ui.components.FrostedSurface

/** The bottom dock: pinned apps plus the always-present button into the full app drawer. */
@Composable
fun Dock(
    dockItems: List<DockItem>,
    appFor: (String) -> AppInfo?,
    badgedPackages: Set<String>,
    onAppClick: (AppInfo) -> Unit,
    onUnpin: (DockItem) -> Unit,
    onOpenDrawer: () -> Unit,
    oneHandedMode: Boolean,
) {
    FrostedSurface(
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = if (oneHandedMode) 28.dp else 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            dockItems.sortedBy { it.slot }.forEach { dockItem ->
                val app = appFor(dockItem.appKey)
                if (app != null) {
                    AppIconView(
                        app = app,
                        iconSizeDp = 44.dp,
                        showLabel = false,
                        showBadge = app.packageName in badgedPackages,
                        onClick = { onAppClick(app) },
                        onLongClick = { onUnpin(dockItem) },
                    )
                }
            }

            IconButton(onClick = onOpenDrawer) {
                Icon(Icons.Default.Apps, contentDescription = "App drawer", tint = Color.White)
            }
        }
    }
}
