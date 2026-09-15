package com.aether.launcher.ui.home

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.aether.launcher.data.model.AppInfo
import com.aether.launcher.data.model.GestureAction
import com.aether.launcher.gesture.aetherGestures
import com.aether.launcher.ui.drawer.AppDrawerScreen
import com.aether.launcher.ui.lock.AppLockGate
import com.aether.launcher.ui.settings.SettingsScreen
import com.aether.launcher.ui.widget.WidgetPickerActivity
import kotlinx.coroutines.flow.collectLatest

enum class AetherSurface { HOME, DRAWER, SETTINGS }

/** Root composable: owns which full-screen surface is showing and wires gestures to surface transitions. */
@Composable
fun AetherApp(
    uiState: HomeUiState,
    suggestedAppKeys: List<String>,
    viewModel: HomeViewModel,
    activity: android.app.Activity,
) {
    var surface by remember { mutableStateOf(AetherSurface.HOME) }
    var appPendingUnlock by remember { mutableStateOf<AppInfo?>(null) }

    LaunchedEffect(Unit) {
        viewModel.homePressedAgain.collectLatest {
            surface = AetherSurface.HOME
        }
    }

    fun handleGestureAction(action: GestureAction) {
        when (action) {
            GestureAction.OPEN_APP_DRAWER -> surface = AetherSurface.DRAWER
            GestureAction.OPEN_NOTIFICATIONS -> runCatching {
                @Suppress("DEPRECATION")
                (activity.getSystemService("statusbar"))?.let {
                    val method = it.javaClass.getMethod("expandNotificationsPanel")
                    method.invoke(it)
                }
            }
            GestureAction.OPEN_QUICK_SETTINGS -> runCatching {
                @Suppress("DEPRECATION")
                (activity.getSystemService("statusbar"))?.let {
                    val method = it.javaClass.getMethod("expandSettingsPanel")
                    method.invoke(it)
                }
            }
            GestureAction.LOCK_SCREEN -> runCatching {
                val dpm = activity.getSystemService(android.app.admin.DevicePolicyManager::class.java)
                dpm?.lockNow()
            }
            GestureAction.OPEN_WIDGET_STACK -> activity.startActivity(
                Intent(activity, WidgetPickerActivity::class.java)
            )
            GestureAction.OPEN_SEARCH -> surface = AetherSurface.DRAWER
            GestureAction.PREVIOUS_APP -> { /* No public "last app" API without accessibility service; intentionally a no-op. */ }
            GestureAction.NONE -> Unit
        }
    }

    fun openApp(appInfo: AppInfo) {
        if (uiState.settings.appLockEnabled && appInfo.key in uiState.hiddenAppKeys) {
            appPendingUnlock = appInfo
        } else {
            viewModel.onAppLaunched(appInfo)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .aetherGestures(uiState.settings.gestureMap) { action ->
                if (surface == AetherSurface.HOME) handleGestureAction(action)
            }
    ) {
        HomeScreen(
            uiState = uiState,
            suggestedAppKeys = suggestedAppKeys,
            onAppClick = ::openApp,
            onOpenDrawer = { surface = AetherSurface.DRAWER },
            onOpenSettings = { surface = AetherSurface.SETTINGS },
            viewModel = viewModel,
            activity = activity,
        )

        AnimatedVisibility(
            visible = surface == AetherSurface.DRAWER,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        ) {
            AppDrawerScreen(
                uiState = uiState,
                onAppClick = { openApp(it); surface = AetherSurface.HOME },
                onDismiss = { surface = AetherSurface.HOME },
                onHideApp = viewModel::hideApp,
                onUnhideApp = { viewModel.unhideApp(it) },
                onAddToHome = { app -> viewModel.placeAppOnHome(app, 0, findFreeCol(uiState), 0) },
                onAddToDock = { app -> viewModel.pinToDock(app, uiState.dockItems.size) },
                onAppInfo = viewModel::openAppInfo,
                onUninstall = viewModel::requestUninstall,
            )
        }

        AnimatedVisibility(
            visible = surface == AetherSurface.SETTINGS,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            SettingsScreen(
                uiState = uiState,
                onDismiss = { surface = AetherSurface.HOME },
                onUpdateSettings = viewModel::updateSettings,
                onUnhideApp = viewModel::unhideApp,
                viewModel = viewModel,
            )
        }

        appPendingUnlock?.let { locked ->
            AppLockGate(
                storedPinHash = uiState.settings.pinHash,
                onUnlocked = {
                    viewModel.onAppLaunched(locked)
                    appPendingUnlock = null
                },
                onCancel = { appPendingUnlock = null },
            )
        }
    }
}

private fun findFreeCol(uiState: HomeUiState): Int {
    val usedCols = uiState.homeItems.filter { it.page == 0 && it.row == 0 }.map { it.col }.toSet()
    return (0..20).first { it !in usedCols }
}
