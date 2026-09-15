package com.aether.launcher.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aether.launcher.data.model.AppInfo
import com.aether.launcher.data.model.HomeItem
import com.aether.launcher.ui.dock.Dock
import java.text.SimpleDateFormat
import java.util.*

private const val PAGE_COUNT = 5

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    suggestedAppKeys: List<String>,
    onAppClick: (AppInfo) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel,
    activity: android.app.Activity,
) {
    val pagerState = rememberPagerState(initialPage = 0) { PAGE_COUNT }
    val density = uiState.settings.gridDensity

    Column(modifier = Modifier.fillMaxSize()) {
        AtAGlanceWidget(modifier = Modifier.padding(top = 48.dp, start = 24.dp, end = 24.dp))

        Spacer(Modifier.height(8.dp))

        SmartSuggestionRow(
            uiState = uiState,
            suggestedAppKeys = suggestedAppKeys,
            onAppClick = onAppClick,
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            val itemsOnPage = remember(uiState.homeItems, page) {
                uiState.homeItems.filter { it.page == page }
            }
            HomeGrid(
                columns = density.columns,
                rows = density.rows,
                items = itemsOnPage,
                appFor = uiState::appByKey,
                badgedPackages = uiState.badgedPackages,
                showLabels = uiState.settings.showLabels,
                onAppClick = onAppClick,
                onAppLongPressRemove = viewModel::removeHomeItem,
                onMove = { item, col, row -> viewModel.moveHomeItem(item, page, col, row) },
                onOpenSettings = onOpenSettings,
            )
        }

        PageIndicator(pagerState.pageCount, pagerState.currentPage, Modifier.align(Alignment.CenterHorizontally))

        Dock(
            dockItems = uiState.dockItems,
            appFor = uiState::appByKey,
            badgedPackages = uiState.badgedPackages,
            onAppClick = onAppClick,
            onUnpin = viewModel::unpinFromDock,
            onOpenDrawer = onOpenDrawer,
            oneHandedMode = uiState.settings.oneHandedMode,
        )
    }
}

/** iOS-style "At a Glance": live clock + date, tap-through to calendar. Genuinely live, updates itself every minute. */
@Composable
private fun AtAGlanceWidget(modifier: Modifier = Modifier) {
    val now = remember { androidx.compose.runtime.mutableStateOf(Date()) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            now.value = Date()
            kotlinx.coroutines.delay(30_000)
        }
    }
    val timeFormat = remember { SimpleDateFormat("h:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()) }

    Column(modifier = modifier) {
        Text(
            text = timeFormat.format(now.value),
            color = Color.White,
            fontSize = 56.sp,
            fontWeight = FontWeight.Light,
        )
        Text(
            text = dateFormat.format(now.value),
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 16.sp,
        )
    }
}

@Composable
private fun PageIndicator(count: Int, current: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier.padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(count) { index ->
            val active = index == current
            Box(
                modifier = Modifier
                    .size(if (active) 7.dp else 5.dp)
                    .background(
                        color = if (active) Color.White else Color.White.copy(alpha = 0.4f),
                        shape = androidx.compose.foundation.shape.CircleShape,
                    )
            )
        }
    }
}
