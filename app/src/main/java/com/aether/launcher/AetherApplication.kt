package com.aether.launcher

import android.app.Application
import android.content.Intent
import android.util.Log
import com.aether.launcher.data.db.AppDatabase
import com.aether.launcher.data.repository.AppRepository
import com.aether.launcher.data.repository.LayoutRepository
import com.aether.launcher.data.repository.SettingsRepository
import com.aether.launcher.iconpack.IconPackRepository
import com.aether.launcher.suggestions.SmartSuggestionEngine
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

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
        installCrashHandler()
        database = AppDatabase.get(this)
        settingsRepository = SettingsRepository(this)
        iconPackRepository = IconPackRepository(this)
        appRepository = AppRepository(this, iconPackRepository)
        layoutRepository = LayoutRepository(database.homeItemDao(), database.dockItemDao(), database.hiddenAppDao())
        smartSuggestionEngine = SmartSuggestionEngine(database.appLaunchEventDao())
    }

    /**
     * Since Aether *is* the Home app, a silent crash means the user is dropped back onto
     * whatever launcher they had before with no way to see why — there's no ADB access assumed
     * here, so show the real stack trace on-device instead of just logging it.
     */
    private fun installCrashHandler() {
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                val writer = StringWriter()
                throwable.printStackTrace(PrintWriter(writer))
                Log.e("AetherCrash", writer.toString())
                val intent = Intent(this, CrashActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    putExtra(CrashActivity.EXTRA_STACK_TRACE, writer.toString())
                }
                startActivity(intent)
            }
            previousHandler?.uncaughtException(thread, throwable)
            exitProcess(1)
        }
    }
}
