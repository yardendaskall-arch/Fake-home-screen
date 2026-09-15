package com.aether.launcher.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Process
import android.os.UserHandle
import com.aether.launcher.data.model.AppCategory
import com.aether.launcher.data.model.AppInfo
import com.aether.launcher.iconpack.IconPackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Single source of truth for "what apps exist on this device". Uses LauncherApps so it also
 * sees work-profile apps, and re-queries whenever the system tells us the app set changed —
 * or whenever [refresh] is called after the active icon pack / icon shape changes.
 */
class AppRepository(
    private val context: Context,
    private val iconPackRepository: IconPackRepository,
) {
    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    private val packageManager: PackageManager = context.packageManager
    private val manualRefresh = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Call after changing the active icon pack or icon shape so every icon is re-resolved. */
    fun refresh() { manualRefresh.tryEmit(Unit) }

    /** Emits the full app list once at collection start, then again after every install/uninstall/update/refresh. */
    fun observeApps(): Flow<List<AppInfo>> = callbackFlow {
        val callback = object : LauncherApps.Callback() {
            override fun onPackageAdded(packageName: String?, user: UserHandle?) { trySend(runCatchingLoad()) }
            override fun onPackageRemoved(packageName: String?, user: UserHandle?) { trySend(runCatchingLoad()) }
            override fun onPackageChanged(packageName: String?, user: UserHandle?) { trySend(runCatchingLoad()) }
            override fun onPackagesAvailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) { trySend(runCatchingLoad()) }
            override fun onPackagesUnavailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) { trySend(runCatchingLoad()) }
        }
        launcherApps.registerCallback(callback)
        val job = launch {
            manualRefresh.collect { trySend(runCatchingLoad()) }
        }
        awaitClose { launcherApps.unregisterCallback(callback); job.cancel() }
    }.onStart { emit(loadAllApps()) }.distinctUntilChanged()

    private fun runCatchingLoad(): List<AppInfo> = runCatching { loadAllApps() }.getOrDefault(emptyList())

    suspend fun loadAllApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val profiles = launcherApps.profiles.ifEmpty { listOf(Process.myUserHandle()) }
        profiles.flatMap { user ->
            launcherApps.getActivityList(null, user).map { activity ->
                val appInfo = activity.applicationInfo
                val platformCategory = if (android.os.Build.VERSION.SDK_INT >= 26) appInfo.category else -1
                AppInfo(
                    packageName = activity.applicationInfo.packageName,
                    activityName = activity.componentName.className,
                    label = activity.label.toString(),
                    icon = iconPackRepository.resolveIcon(activity, context),
                    userHandle = user,
                    isWorkProfile = user != Process.myUserHandle(),
                    category = AppCategory.fromPlatformCategory(platformCategory),
                    installTimeMillis = runCatching {
                        packageManager.getPackageInfo(appInfo.packageName, 0).firstInstallTime
                    }.getOrDefault(0L),
                )
            }
        }
    }

    fun launchApp(appInfo: AppInfo, sourceBoundsProvider: () -> android.graphics.Rect?) {
        val component = android.content.ComponentName(appInfo.packageName, appInfo.activityName)
        runCatching {
            launcherApps.startMainActivity(component, appInfo.userHandle, sourceBoundsProvider(), null)
        }.onFailure {
            // Fallback for edge cases LauncherApps can't resolve (rare OEM launcher-restricted activities).
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = android.content.ComponentName(appInfo.packageName, appInfo.activityName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { context.startActivity(intent) }
        }
    }

    fun openAppInfo(appInfo: AppInfo) {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", appInfo.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun requestUninstall(appInfo: AppInfo) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = android.net.Uri.fromParts("package", appInfo.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
