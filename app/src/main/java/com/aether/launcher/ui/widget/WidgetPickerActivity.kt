package com.aether.launcher.ui.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.aether.launcher.data.model.HomeItem
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Aether's own widget picker: lists every AppWidgetProviderInfo on the device, then drives the
 * standard allocate -> bind (with permission prompt if needed) -> optional configure -> place
 * pipeline that every Android launcher implements for third-party widgets.
 */
class WidgetPickerActivity : FragmentActivity() {

    private lateinit var appWidgetManager: AppWidgetManager
    private var pendingAppWidgetId: Int = -1
    private var pendingProviderInfo: AppWidgetProviderInfo? = null

    private val bindLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            proceedAfterBind()
        } else {
            pendingAppWidgetId.takeIf { it >= 0 }?.let { AetherWidgetHost.deleteId(this, it) }
            finish()
        }
    }

    private val configureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            finishWithWidget()
        } else {
            pendingAppWidgetId.takeIf { it >= 0 }?.let { AetherWidgetHost.deleteId(this, it) }
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appWidgetManager = AppWidgetManager.getInstance(this)
        val providers = appWidgetManager.installedProviders

        setContent {
            MaterialTheme {
                Surface {
                    WidgetPickerList(providers) { provider -> startBindFlow(provider) }
                }
            }
        }
    }

    private fun startBindFlow(provider: AppWidgetProviderInfo) {
        val appWidgetId = AetherWidgetHost.allocateId(this)
        pendingAppWidgetId = appWidgetId
        pendingProviderInfo = provider

        val bound = appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, provider.provider)
        if (bound) {
            proceedAfterBind()
        } else {
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider.provider)
            }
            bindLauncher.launch(intent)
        }
    }

    private fun proceedAfterBind() {
        val provider = pendingProviderInfo ?: return finish()
        if (provider.configure != null) {
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                component = provider.configure
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pendingAppWidgetId)
            }
            runCatching { configureLauncher.launch(intent) }.onFailure { finishWithWidget() }
        } else {
            finishWithWidget()
        }
    }

    private fun finishWithWidget() {
        val provider = pendingProviderInfo
        val appWidgetId = pendingAppWidgetId
        if (provider != null && appWidgetId >= 0) {
            lifecycleScope.launch {
                val app = application as com.aether.launcher.AetherApplication
                val minSpanX = (provider.minWidth / 110).coerceIn(2, 5)
                val minSpanY = (provider.minHeight / 110).coerceIn(1, 4)
                app.layoutRepository.saveHomeItem(
                    HomeItem.Widget(UUID.randomUUID().toString(), page = 0, col = 0, row = 0, spanX = minSpanX, spanY = minSpanY, appWidgetId = appWidgetId)
                )
                finish()
            }
        } else {
            finish()
        }
    }
}

@Composable
private fun WidgetPickerList(providers: List<AppWidgetProviderInfo>, onPick: (AppWidgetProviderInfo) -> Unit) {
    val pm = androidx.compose.ui.platform.LocalContext.current.packageManager
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(providers.size) { index ->
            val provider = providers[index]
            val label = provider.loadLabel(pm)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .clickable { onPick(provider) },
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                val icon = runCatching { provider.loadPreviewImage(androidx.compose.ui.platform.LocalContext.current, 0) }
                    .getOrNull() ?: runCatching { provider.loadIcon(androidx.compose.ui.platform.LocalContext.current, 0) }.getOrNull()
                if (icon != null) {
                    Image(
                        bitmap = icon.toBitmap().asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(Modifier.width(16.dp))
                }
                Text(label)
            }
        }
    }
}
