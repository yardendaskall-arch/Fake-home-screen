package com.aether.launcher.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aether.launcher.data.model.AppInfo
import com.aether.launcher.ui.components.AppIconView

/**
 * The "AI dock": a horizontal strip of apps predicted for *right now*, ranked by an on-device
 * frequency model over what you personally open at this time of day (see SmartSuggestionEngine).
 * No network calls, no telemetry — the "AI" is a GROUP BY over a local SQLite table.
 */
@Composable
fun SmartSuggestionRow(
    uiState: HomeUiState,
    suggestedAppKeys: List<String>,
    onAppClick: (AppInfo) -> Unit,
) {
    if (!uiState.settings.smartSuggestionsEnabled || suggestedAppKeys.isEmpty()) return

    val apps = suggestedAppKeys.mapNotNull { uiState.appByKey(it) }
    if (apps.isEmpty()) return

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(apps, key = { it.key }) { app ->
            AppIconView(
                app = app,
                iconSizeDp = 48.dp,
                showLabel = true,
                showBadge = app.packageName in uiState.badgedPackages,
                onClick = { onAppClick(app) },
            )
        }
    }
}
