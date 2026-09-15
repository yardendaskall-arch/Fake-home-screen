package com.aether.launcher

import android.app.Application
import com.aether.launcher.data.db.AppDatabase
import com.aether.launcher.data.repository.AppRepository
import com.aether.launcher.data.repository.LayoutRepository
import com.aether.launcher.data.repository.SettingsRepository
import com.aether.launcher.iconpack.IconPackRepository
import com.aether.launcher.suggestions.SmartSuggestionEngine

/** Minimal hand-rolled service locator — this app is small enough that Hilt would be pure ceremony. */
class AetherApplication : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var iconPackRepository: IconPackRepository
        private set
    lateinit var appRepository: AppRepository
        private set
    lateinit var layoutRepository: LayoutRepository
        private set
    lateinit var smartSuggestionEngine: SmartSuggestionEngine
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.get(this)
        settingsRepository = SettingsRepository(this)
        iconPackRepository = IconPackRepository(this)
        appRepository = AppRepository(this, iconPackRepository)
        layoutRepository = LayoutRepository(database.homeItemDao(), database.dockItemDao(), database.hiddenAppDao())
        smartSuggestionEngine = SmartSuggestionEngine(database.appLaunchEventDao())
    }
}
