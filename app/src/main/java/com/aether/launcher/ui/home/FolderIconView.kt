package com.aether.launcher.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.aether.launcher.data.model.AppInfo
import com.aether.launcher.data.model.HomeItem
import com.aether.launcher.ui.components.AppIconView

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun FolderIconView(
    folder: HomeItem.Folder,
    appFor: (String) -> AppInfo?,
    onAppClick: (AppInfo) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val apps = remember(folder) { folder.appKeys.mapNotNull(appFor) }

    Box(
        modifier = Modifier
            .size(52.dp)
            .background(Color.White.copy(alpha = 0.16f), RoundedCornerShape(16.dp))
            .clickable { expanded = true },
    ) {
        FolderPreviewGrid(apps)
    }

    if (expanded) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(onDismissRequest = { expanded = false }, sheetState = sheetState) {
            Text(
                text = folder.name,
                fontSize = 18.sp,
                modifier = Modifier.padding(16.dp),
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp),
            ) {
                items(apps.size) { index ->
                    val app = apps[index]
                    AppIconView(
                        app = app,
                        iconSizeDp = 48.dp,
                        showLabel = true,
                        showBadge = false,
                        onClick = { onAppClick(app); expanded = false },
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FolderPreviewGrid(apps: List<AppInfo>) {
    val preview = apps.take(4)
    androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize().padding(6.dp)) {
        for (row in 0..1) {
            Row(modifier = Modifier.weight(1f)) {
                for (col in 0..1) {
                    val index = row * 2 + col
                    Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        preview.getOrNull(index)?.let { app ->
                            val bitmap = remember(app.icon) { app.icon.toBitmap().asImageBitmap() }
                            androidx.compose.foundation.Image(
                                bitmap = bitmap,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(0.85f),
                            )
                        }
                    }
                }
            }
        }
    }
}
