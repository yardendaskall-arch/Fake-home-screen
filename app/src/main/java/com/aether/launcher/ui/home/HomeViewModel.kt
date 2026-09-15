package com.aether.launcher.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aether.launcher.AetherApplication
import com.aether.launcher.data.model.*
import com.aether.launcher.service.NotificationBadgeService
import com.aether.launcher.util.WallpaperPalette
import com.aether.launcher.util.extractWallpaperPalette
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class HomeUiState(
    val allApps: List<AppInfo> = emptyList(),
    val visibleApps: List<AppInfo> = emptyList(),
    val hiddenAppKeys: Set<String> = emptySet(),
    val homeItems: List<HomeItem> = emptyList(),
    val dockItems: List<DockItem> = emptyList(),
    val badgedPackages: Set<String> = emptySet(),
    val settings: LauncherSettings = LauncherSettings(),
    val isLoading: Boolean = true,
) {
    fun appByKey(key: String): AppInfo? = allApps.firstOrNull { it.key == key }
}

class HomeViewModel(private val app: AetherApplication) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        app.appRepository.observeApps(),
        app.layoutRepository.observeHomeItems(),
        app.layoutRepository.observeDockItems(),
        app.layoutRepository.observeHiddenAppKeys(),
        app.settingsRepository.settings,
        NotificationBadgeService.badgedPackagesFlow,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val apps = values[0] as List<AppInfo>
        @Suppress("UNCHECKED_CAST")
        val homeItems = values[1] as List<HomeItem>
        @Suppress("UNCHECKED_CAST")
        val dockItems = values[2] as List<DockItem>
        @Suppress("UNCHECKED_CAST")
        val hidden = values[3] as Set<String>
        val settings = values[4] as LauncherSettings
        @Suppress("UNCHECKED_CAST")
        val badged = values[5] as Set<String>

        HomeUiState(
            allApps = apps,
            visibleApps = apps.filter { it.key !in hidden },
            hiddenAppKeys = hidden,
            homeItems = homeItems,
            dockItems = dockItems,
            badgedPackages = badged,
            settings = settings,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    // Every field below must stay ABOVE the init{} block: Kotlin runs property initializers and
    // init{} blocks in textual declaration order, and init{} kicks off coroutines (via
    // viewModelScope.launch, which runs synchronously up to its first real suspension point on
    // Dispatchers.Main.immediate) that touch these fields — declaring init{} first previously
    // caused a NullPointerException on _palette before its initializer had run.
    private val _suggested = MutableStateFlow<List<String>>(emptyList())
    val suggestedAppKeys: StateFlow<List<String>> = _suggested

    private val _palette = MutableStateFlow<WallpaperPalette?>(null)
    val wallpaperPalette: StateFlow<WallpaperPalette?> = _palette

    private val _homePressedAgain = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val homePressedAgain: SharedFlow<Unit> = _homePressedAgain

    init {
        refreshWallpaperPalette()
        viewModelScope.launch {
            val suggestions = app.smartSuggestionEngine.suggestedApps()
            _suggested.value = suggestions
        }
        // Keep the icon-resolution pipeline (pack + shape mask) in sync with Settings, and force
        // every icon to be re-resolved whenever either changes.
        viewModelScope.launch {
            app.settingsRepository.settings
                .map { it.selectedIconPackPackage to it.iconShape }
                .distinctUntilChanged()
                .collect { (pack, shape) ->
                    app.iconPackRepository.setActiveIconPack(pack)
                    app.iconPackRepository.setIconShape(shape)
                    app.appRepository.refresh()
                }
        }
    }

    /** Fired when the user presses Home while already Home, or presses Back: closes drawer/settings and snaps to page 0. */
    fun onHomePressedAgain() {
        _homePressedAgain.tryEmit(Unit)
    }

    fun refreshWallpaperPalette() {
        viewModelScope.launch {
            _palette.value = extractWallpaperPalette(app)
        }
    }

    fun onAppLaunched(appInfo: AppInfo) {
        viewModelScope.launch { app.smartSuggestionEngine.recordLaunch(appInfo.key) }
        app.appRepository.launchApp(appInfo) { null }
    }

    fun placeAppOnHome(app0: AppInfo, page: Int, col: Int, row: Int) {
        viewModelScope.launch {
            app.layoutRepository.saveHomeItem(HomeItem.AppIcon(UUID.randomUUID().toString(), page, col, row, app0.key))
        }
    }

    fun moveHomeItem(item: HomeItem, page: Int, col: Int, row: Int) {
        viewModelScope.launch {
            val moved = when (item) {
                is HomeItem.AppIcon -> item.copy(page = page, col = col, row = row)
                is HomeItem.Folder -> item.copy(page = page, col = col, row = row)
                is HomeItem.Widget -> item.copy(page = page, col = col, row = row)
            }
            app.layoutRepository.saveHomeItem(moved)
        }
    }

    fun removeHomeItem(item: HomeItem) {
        viewModelScope.launch { app.layoutRepository.removeHomeItem(item.id) }
    }

    fun pinToDock(app0: AppInfo, slot: Int) {
        viewModelScope.launch {
            app.layoutRepository.pinToDock(DockItem(UUID.randomUUID().toString(), slot, app0.key))
        }
    }

    fun unpinFromDock(item: DockItem) {
        viewModelScope.launch { app.layoutRepository.unpinFromDock(item.id) }
    }

    fun hideApp(app0: AppInfo) = viewModelScope.launch { app.layoutRepository.hideApp(app0.key) }
    fun unhideApp(appKey: String) = viewModelScope.launch { app.layoutRepository.unhideApp(appKey) }

    fun openAppInfo(app0: AppInfo) = app.appRepository.openAppInfo(app0)
    fun requestUninstall(app0: AppInfo) = app.appRepository.requestUninstall(app0)

    suspend fun exportLayoutBackup(): String = app.layoutRepository.exportBackup()
    suspend fun importLayoutBackup(json: String) = app.layoutRepository.importBackup(json)

    fun createFolder(page: Int, col: Int, row: Int, appKeys: List<String>) {
        viewModelScope.launch {
            app.layoutRepository.saveHomeItem(
                HomeItem.Folder(UUID.randomUUID().toString(), page, col, row, "Folder", appKeys)
            )
        }
    }

    fun updateSettings(transform: (LauncherSettings) -> LauncherSettings) {
        viewModelScope.launch { app.settingsRepository.update(transform) }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(context.applicationContext as AetherApplication) as T
            }
        }
    }
}
