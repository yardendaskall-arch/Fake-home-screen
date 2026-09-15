package com.aether.launcher.ui.drawer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aether.launcher.data.model.AppInfo

/** Long-press context menu for an app — the primary way apps get added to Home/Dock, hidden, or removed. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppActionSheet(
    app: AppInfo,
    isHidden: Boolean,
    onDismiss: () -> Unit,
    onAddToHome: () -> Unit,
    onAddToDock: () -> Unit,
    onHide: () -> Unit,
    onUnhide: () -> Unit,
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = app.label,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
            ActionRow(Icons.Default.Home, "Add to Home screen", onAddToHome)
            ActionRow(Icons.Default.Star, "Pin to dock", onAddToDock)
            if (isHidden) {
                ActionRow(Icons.Default.Visibility, "Unhide app", onUnhide)
            } else {
                ActionRow(Icons.Default.VisibilityOff, "Hide app", onHide)
            }
            ActionRow(Icons.Default.Info, "App info", onAppInfo)
            ActionRow(Icons.Default.Delete, "Uninstall", onUninstall)
        }
    }
}

@Composable
private fun ActionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(20.dp))
        Text(label)
    }
}
