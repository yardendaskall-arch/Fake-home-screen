package com.aether.launcher.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * AppRepository already re-queries via LauncherApps.Callback while the process is alive; this
 * receiver's job is just to wake the app up / signal a refresh even from a cold start context,
 * e.g. right after the user installs something while the launcher's UI isn't in the foreground.
 */
class AppChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        events.tryEmit(Unit)
    }

    companion object {
        private val events = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 4)
        val packageChanged: SharedFlow<Unit> = events.asSharedFlow()
    }
}
