package com.aether.launcher

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.aether.launcher.ui.home.AetherApp
import com.aether.launcher.ui.home.HomeViewModel
import com.aether.launcher.ui.theme.AetherTheme

/**
 * Aether's single Activity. Declared android:launchMode="singleTask" with HOME/DEFAULT/LAUNCHER
 * intent filters in the manifest, so the system treats it as an installable Home app; the Back
 * key here intentionally does nothing destructive (real launchers never navigate "back" out).
 * Extends FragmentActivity (not plain ComponentActivity) because BiometricPrompt requires it.
 */
class MainActivity : FragmentActivity() {

    private val viewModel: HomeViewModel by viewModels { HomeViewModel.factory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        onBackPressedDispatcher.addCallback(this) {
            // A Home screen has nowhere to "go back" to; treat Back like pressing Home again.
            viewModel.onHomePressedAgain()
        }

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val palette by viewModel.wallpaperPalette.collectAsState()
            val suggested by viewModel.suggestedAppKeys.collectAsState()

            AetherTheme(wallpaperPalette = palette, useDynamicColor = uiState.settings.useDynamicWallpaperColor) {
                AetherApp(
                    uiState = uiState,
                    suggestedAppKeys = suggested,
                    viewModel = viewModel,
                    activity = this,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.onHomePressedAgain()
    }

    override fun onStart() {
        super.onStart()
        com.aether.launcher.ui.widget.AetherWidgetHost.startListening(this)
    }

    override fun onStop() {
        com.aether.launcher.ui.widget.AetherWidgetHost.stopListening(this)
        super.onStop()
    }

    companion object {
        const val REQUEST_PICK_WIDGET = 9003
    }
}
