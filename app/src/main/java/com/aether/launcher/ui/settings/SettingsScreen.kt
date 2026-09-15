@file:OptIn(ExperimentalMaterial3Api::class)

package com.aether.launcher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aether.launcher.data.model.*
import com.aether.launcher.iconpack.IconPackRepository
import com.aether.launcher.ui.home.HomeUiState
import com.aether.launcher.ui.home.HomeViewModel

@Composable
fun SettingsScreen(
    uiState: HomeUiState,
    onDismiss: () -> Unit,
    onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit,
    onUnhideApp: (String) -> Unit,
    viewModel: HomeViewModel,
) {
    val settings = uiState.settings
    val context = LocalContext.current
    var showPinSetup by remember { mutableStateOf(false) }
    var showBackupSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF111114),
        topBar = {
            TopAppBar(
                title = { Text("Aether settings") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Close settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF111114)),
            )
        },
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            item { SectionHeader("Layout") }
            item {
                DropdownSetting(
                    label = "Grid density",
                    selected = settings.gridDensity.name,
                    options = GridDensity.entries.map { it.name },
                    onSelect = { value -> onUpdateSettings { it.copy(gridDensity = GridDensity.valueOf(value)) } },
                )
            }
            item {
                SwitchSetting("Show app labels", settings.showLabels) { checked ->
                    onUpdateSettings { it.copy(showLabels = checked) }
                }
            }
            item {
                SliderSetting("Icon size", settings.iconScale, 0.7f..1.4f) { value ->
                    onUpdateSettings { it.copy(iconScale = value) }
                }
            }

            item { SectionHeader("Appearance") }
            item {
                DropdownSetting(
                    label = "Icon shape",
                    selected = settings.iconShape.name,
                    options = IconShape.entries.map { it.name },
                    onSelect = { value -> onUpdateSettings { it.copy(iconShape = IconShape.valueOf(value)) } },
                )
            }
            item {
                SwitchSetting("Dynamic wallpaper color (Material You)", settings.useDynamicWallpaperColor) { checked ->
                    onUpdateSettings { it.copy(useDynamicWallpaperColor = checked) }
                }
            }
            item {
                SwitchSetting("Frosted glass / blur effects", settings.blurEnabled) { checked ->
                    onUpdateSettings { it.copy(blurEnabled = checked) }
                }
            }
            item { IconPackPicker(settings, viewModel) }

            item { SectionHeader("Gestures") }
            items(GestureSlot.entries.toList()) { slot ->
                GestureRow(
                    slot = slot,
                    current = settings.gestureMap[slot] ?: GestureAction.NONE,
                    onSelect = { action ->
                        onUpdateSettings { it.copy(gestureMap = it.gestureMap + (slot to action)) }
                    },
                )
            }

            item { SectionHeader("Smart features") }
            item {
                SwitchSetting("On-device Smart Suggestions", settings.smartSuggestionsEnabled) { checked ->
                    onUpdateSettings { it.copy(smartSuggestionsEnabled = checked) }
                }
            }
            item {
                SwitchSetting("Notification dots", settings.notificationDotsEnabled) { checked ->
                    onUpdateSettings { it.copy(notificationDotsEnabled = checked) }
                    if (checked) openNotificationAccessSettings(context)
                }
            }
            item {
                SwitchSetting("One-handed reachability mode", settings.oneHandedMode) { checked ->
                    onUpdateSettings { it.copy(oneHandedMode = checked) }
                }
            }
            item {
                SwitchSetting("Focus mode (minimal, grayscale-ready)", settings.focusModeEnabled) { checked ->
                    onUpdateSettings { it.copy(focusModeEnabled = checked) }
                }
            }

            item { SectionHeader("Privacy") }
            item {
                SwitchSetting("App lock for hidden apps", settings.appLockEnabled) { checked ->
                    if (checked && settings.pinHash == null) {
                        showPinSetup = true
                    } else {
                        onUpdateSettings { it.copy(appLockEnabled = checked) }
                    }
                }
            }
            item {
                HiddenAppsList(uiState = uiState, onUnhideApp = onUnhideApp)
            }

            item { SectionHeader("Backup") }
            item {
                Button(
                    onClick = { showBackupSheet = true },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                ) {
                    Text("Backup & restore layout")
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }

    if (showPinSetup) {
        PinSetupDialog(
            onSetPin = { hash ->
                onUpdateSettings { it.copy(pinHash = hash, appLockEnabled = true) }
                showPinSetup = false
            },
            onDismiss = { showPinSetup = false },
        )
    }

    if (showBackupSheet) {
        BackupRestoreSheet(
            viewModel = viewModel,
            onDismiss = { showBackupSheet = false },
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = Color(0xFF8B8BFF),
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 6.dp),
    )
}

@Composable
private fun SwitchSetting(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = Color.White, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SliderSetting(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
        Text(label, color = Color.White)
        Slider(value = value, onValueChange = onValueChange, valueRange = range)
    }
}

@Composable
private fun DropdownSetting(label: String, selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
        Text(label, color = Color.White)
        Box {
            OutlinedButton(onClick = { expanded = true }) { Text(selected) }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false })
                }
            }
        }
    }
}

@Composable
private fun GestureRow(slot: GestureSlot, current: GestureAction, onSelect: (GestureAction) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val slotLabel = when (slot) {
        GestureSlot.SWIPE_UP -> "Swipe up"
        GestureSlot.SWIPE_DOWN -> "Swipe down"
        GestureSlot.DOUBLE_TAP -> "Double tap"
        GestureSlot.PINCH_IN -> "Pinch in"
        GestureSlot.EDGE_SWIPE_LEFT -> "Swipe from left edge"
        GestureSlot.EDGE_SWIPE_RIGHT -> "Swipe from right edge"
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(slotLabel, color = Color.White, modifier = Modifier.weight(1f))
        Box {
            OutlinedButton(onClick = { expanded = true }) { Text(current.label) }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                GestureAction.entries.forEach { action ->
                    DropdownMenuItem(text = { Text(action.label) }, onClick = { onSelect(action); expanded = false })
                }
            }
        }
    }
}

@Composable
private fun IconPackPicker(settings: LauncherSettings, viewModel: HomeViewModel) {
    val context = LocalContext.current
    val repo = remember { IconPackRepository(context) }
    val packs = remember { repo.findInstalledIconPacks() }
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = packs.firstOrNull { it.packageName == settings.selectedIconPackPackage }?.label ?: "System default"

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
        Text("Icon pack", color = Color.White)
        Box {
            OutlinedButton(onClick = { expanded = true }) { Text(selectedLabel) }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text("System default") }, onClick = {
                    viewModel.updateSettings { it.copy(selectedIconPackPackage = null) }
                    expanded = false
                })
                packs.forEach { pack ->
                    DropdownMenuItem(text = { Text(pack.label) }, onClick = {
                        viewModel.updateSettings { it.copy(selectedIconPackPackage = pack.packageName) }
                        expanded = false
                    })
                }
            }
        }
        if (packs.isEmpty()) {
            Text(
                "No icon packs installed. Icon packs exporting a NovaLauncher/ADW-style appfilter.xml are supported.",
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun HiddenAppsList(uiState: HomeUiState, onUnhideApp: (String) -> Unit) {
    val hiddenApps = uiState.allApps.filter { it.key in uiState.hiddenAppKeys }
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text("${hiddenApps.size} hidden app(s)", color = Color.White.copy(alpha = 0.7f))
        hiddenApps.forEach { app ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(app.label, color = Color.White)
                TextButton(onClick = { onUnhideApp(app.key) }) { Text("Unhide") }
            }
        }
    }
}

private fun openNotificationAccessSettings(context: android.content.Context) {
    runCatching {
        context.startActivity(
            android.content.Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
