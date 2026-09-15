package com.aether.launcher.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tracks which package names currently have an active notification, so home/dock icons can show
 * a dot. We only ever look at `packageName` — never notification content — and nothing is stored
 * or transmitted beyond this in-memory set for the current session.
 */
class NotificationBadgeService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        pushSnapshot()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        pushSnapshot()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        pushSnapshot()
    }

    private fun pushSnapshot() {
        val packages = runCatching { activeNotifications.map { it.packageName }.toSet() }.getOrDefault(emptySet())
        badgedPackages.value = packages
    }

    companion object {
        private val badgedPackages = MutableStateFlow<Set<String>>(emptySet())

        /** Observable from anywhere in the app; empty until the user grants notification access and this service binds. */
        val badgedPackagesFlow: StateFlow<Set<String>> = badgedPackages.asStateFlow()
    }
}
