@file:OptIn(ExperimentalMaterial3Api::class)

package com.aether.launcher.ui.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.aether.launcher.data.model.AppCategory
import com.aether.launcher.data.model.AppInfo
import com.aether.launcher.ui.components.AppIconView
import com.aether.launcher.ui.components.FrostedSurface
import com.aether.launcher.ui.home.HomeUiState

private enum class DrawerTab { ALL, GAMES, SOCIAL, PRODUCTIVITY, MEDIA, HIDDEN }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerScreen(
    uiState: HomeUiState,
    onAppClick: (AppInfo) -> Unit,
    onDismiss: () -> Unit,
    onHideApp: (AppInfo) -> Unit,
    onUnhideApp: (String) -> Unit,
    onAddToHome: (AppInfo) -> Unit,
    onAddToDock: (AppInfo) -> Unit,
    onAppInfo: (AppInfo) -> Unit,
    onUninstall: (AppInfo) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var tab by remember { mutableStateOf(DrawerTab.ALL) }
    var actionTarget by remember { mutableStateOf<AppInfo?>(null) }

    val sourceList = if (tab == DrawerTab.HIDDEN) {
        uiState.allApps.filter { it.key in uiState.hiddenAppKeys }
    } else {
        uiState.visibleApps
    }

    val filtered = remember(sourceList, query, tab) {
        sourceList
            .filter { app ->
                val matchesQuery = query.isBlank() || app.label.contains(query, ignoreCase = true)
                val matchesTab = when (tab) {
                    DrawerTab.ALL, DrawerTab.HIDDEN -> true
                    DrawerTab.GAMES -> app.category == AppCategory.GAME
                    DrawerTab.SOCIAL -> app.category == AppCategory.SOCIAL
                    DrawerTab.PRODUCTIVITY -> app.category == AppCategory.PRODUCTIVITY
                    DrawerTab.MEDIA -> app.category == AppCategory.MEDIA
                }
                matchesQuery && matchesTab
            }
            .sortedBy { it.label.lowercase() }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f))) {
        Column(modifier = Modifier.fillMaxSize().padding(top = 40.dp)) {
            DrawerDismissHandle(onDismiss)
            SearchField(query = query, onQueryChange = { query = it })

            DrawerTabs(selected = tab, onSelect = { tab = it })

            Spacer(Modifier.height(4.dp))

            val calculatorResult = remember(query) { com.aether.launcher.ui.search.QuickCalculator.tryEvaluate(query) }
            if (calculatorResult != null) {
                CalculatorResultRow(query, calculatorResult)
            }

            Box(modifier = Modifier.weight(1f)) {
                if (filtered.isEmpty() && query.isNotBlank()) {
                    WebSearchFallback(query)
                } else {
                    AlphabetIndexedGrid(
                        apps = filtered,
                        badgedPackages = uiState.badgedPackages,
                        onAppClick = onAppClick,
                        onAppLongClick = { actionTarget = it },
                    )
                }
            }
        }
    }

    actionTarget?.let { app ->
        AppActionSheet(
            app = app,
            isHidden = app.key in uiState.hiddenAppKeys,
            onDismiss = { actionTarget = null },
            onAddToHome = { onAddToHome(app); actionTarget = null },
            onAddToDock = { onAddToDock(app); actionTarget = null },
            onHide = { onHideApp(app); actionTarget = null },
            onUnhide = { onUnhideApp(app.key); actionTarget = null },
            onAppInfo = { onAppInfo(app); actionTarget = null },
            onUninstall = { onUninstall(app); actionTarget = null },
        )
    }
}

@Composable
private fun DrawerDismissHandle(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 36.dp, height = 4.dp)
                .background(Color.White.copy(alpha = 0.5f), androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
        )
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    FrostedSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(52.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(26.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
            Spacer(Modifier.width(10.dp))
            BasicSearchInput(query, onQueryChange, Modifier.weight(1f))
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun BasicSearchInput(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 16.sp),
        cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text("Search apps", color = Color.White.copy(alpha = 0.6f))
            }
            inner()
        },
    )
}

@Composable
private fun CalculatorResultRow(query: String, result: Double) {
    val formatted = if (result == result.toLong().toDouble()) result.toLong().toString() else result.toString()
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("$query =", color = Color.White.copy(alpha = 0.7f))
        Text(formatted, color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
private fun WebSearchFallback(query: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("No apps match \"$query\"", color = Color.White.copy(alpha = 0.6f))
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = {
            val intent = android.content.Intent(android.content.Intent.ACTION_WEB_SEARCH).apply {
                putExtra(android.app.SearchManager.QUERY, query)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { context.startActivity(intent) }
        }) {
            Text("Search the web for \"$query\"")
        }
    }
}

@Composable
private fun DrawerTabs(selected: DrawerTab, onSelect: (DrawerTab) -> Unit) {
    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(DrawerTab.entries.size) { index ->
            val entry = DrawerTab.entries[index]
            val label = when (entry) {
                DrawerTab.ALL -> "All"
                DrawerTab.GAMES -> "Games"
                DrawerTab.SOCIAL -> "Social"
                DrawerTab.PRODUCTIVITY -> "Work"
                DrawerTab.MEDIA -> "Media"
                DrawerTab.HIDDEN -> "Hidden"
            }
            FilterChip(
                selected = selected == entry,
                onClick = { onSelect(entry) },
                label = { Text(label) },
            )
        }
    }
}

private val sectionHeaderCharSelector: (AppInfo) -> String = { it.label.take(1).uppercase() }

@Composable
private fun AlphabetIndexedGrid(
    apps: List<AppInfo>,
    badgedPackages: Set<String>,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
) {
    val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
    val letters = remember(apps) { apps.map(sectionHeaderCharSelector).distinct() }
    val scope = rememberCoroutineScope()

    Row(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            state = gridState,
            modifier = Modifier.weight(1f).fillMaxHeight(),
            contentPadding = PaddingValues(16.dp),
        ) {
            items(apps.size, key = { apps[it].key }) { index ->
                val app = apps[index]
                Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                    AppIconView(
                        app = app,
                        iconSizeDp = 48.dp,
                        showLabel = true,
                        showBadge = app.packageName in badgedPackages,
                        onClick = { onAppClick(app) },
                        onLongClick = { onAppLongClick(app) },
                    )
                }
            }
        }

        AlphabetIndex(
            letters = letters,
            onLetterSelected = { letter ->
                val targetIndex = apps.indexOfFirst { sectionHeaderCharSelector(it) == letter }
                if (targetIndex >= 0) {
                    scope.launch { gridState.animateScrollToItem(targetIndex) }
                }
            },
        )
    }
}
