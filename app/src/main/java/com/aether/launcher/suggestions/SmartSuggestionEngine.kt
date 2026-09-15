package com.aether.launcher.suggestions

import com.aether.launcher.data.db.dao.AppLaunchEventDao
import com.aether.launcher.data.db.entities.AppLaunchEventEntity
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Entirely on-device "AI" dock: ranks apps by how often *you* open them around *this* time of
 * day, using a simple frequency model over a rolling 30-day window of local launch events.
 * Nothing here is sent anywhere — it's just SELECT ... GROUP BY ... ORDER BY.
 */
class SmartSuggestionEngine(private val dao: AppLaunchEventDao) {

    suspend fun recordLaunch(appKey: String) {
        val cal = Calendar.getInstance()
        dao.record(
            AppLaunchEventEntity(
                appKey = appKey,
                timestampMillis = cal.timeInMillis,
                hourOfDay = cal.get(Calendar.HOUR_OF_DAY),
                dayOfWeek = cal.get(Calendar.DAY_OF_WEEK),
            )
        )
        dao.pruneOlderThan(cal.timeInMillis - TimeUnit.DAYS.toMillis(30))
    }

    /** Top apps for "right now", falling back to all-time favorites for new installs with no history. */
    suspend fun suggestedApps(limit: Int = 5): List<String> {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val windowed = dao.topAppsForHourRange(hourStart = (hour - 1).coerceAtLeast(0), hourEnd = (hour + 1).coerceAtMost(23), limit = limit)
        if (windowed.size >= limit) return windowed
        val fallback = dao.topAppsOverall(limit)
        return (windowed + fallback).distinct().take(limit)
    }
}
