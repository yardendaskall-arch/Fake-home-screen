package com.aether.launcher.ui.widget

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context

private const val HOST_ID = 771117

/** Aether's single AppWidgetHost instance — shared across the process, started/stopped with MainActivity's lifecycle. */
object AetherWidgetHost {
    private var host: AppWidgetHost? = null

    fun get(context: Context): AppWidgetHost =
        host ?: AppWidgetHost(context.applicationContext, HOST_ID).also { host = it }

    fun allocateId(context: Context): Int = get(context).allocateAppWidgetId()

    fun deleteId(context: Context, appWidgetId: Int) {
        runCatching { get(context).deleteAppWidgetId(appWidgetId) }
    }

    fun createView(context: Context, appWidgetId: Int, providerInfo: AppWidgetProviderInfo): AppWidgetHostView =
        get(context).createView(context, appWidgetId, providerInfo)

    fun startListening(context: Context) = runCatching { get(context).startListening() }
    fun stopListening(context: Context) = runCatching { get(context).stopListening() }
}
