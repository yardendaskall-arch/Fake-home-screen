package com.aether.launcher.ui.widget

import android.appwidget.AppWidgetManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.aether.launcher.data.model.HomeItem

/** Renders one already-bound app widget on the home grid via the classic AppWidgetHostView, embedded through AndroidView. */
@Composable
fun HostedWidgetView(item: HomeItem.Widget) {
    val context = LocalContext.current
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val providerInfo = appWidgetManager.getAppWidgetInfo(item.appWidgetId) ?: return

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            AetherWidgetHost.createView(ctx, item.appWidgetId, providerInfo).apply {
                setAppWidget(item.appWidgetId, providerInfo)
            }
        },
    )
}
