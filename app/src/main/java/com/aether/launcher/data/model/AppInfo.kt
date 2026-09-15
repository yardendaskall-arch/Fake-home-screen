package com.aether.launcher.data.model

import android.graphics.drawable.Drawable
import android.os.Process
import android.os.UserHandle

/** A single installed, launchable app as the launcher sees it. */
data class AppInfo(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Drawable,
    val userHandle: UserHandle = Process.myUserHandle(),
    val isHidden: Boolean = false,
    val isWorkProfile: Boolean = false,
    val category: AppCategory = AppCategory.OTHER,
    val installTimeMillis: Long = 0L,
) {
    /** Stable key for a launchable component, used everywhere apps are referenced by id. */
    val key: String get() = "$packageName/$activityName"
}

enum class AppCategory(val label: String) {
    GAME("Games"),
    SOCIAL("Social"),
    PRODUCTIVITY("Productivity"),
    MEDIA("Media"),
    SYSTEM("System"),
    OTHER("Other");

    companion object {
        /** Maps Android's ApplicationInfo.category (API 26+) onto our simplified buckets. */
        fun fromPlatformCategory(platformCategory: Int): AppCategory = when (platformCategory) {
            0 -> GAME               // ApplicationInfo.CATEGORY_GAME
            1 -> MEDIA               // CATEGORY_AUDIO
            2 -> MEDIA               // CATEGORY_VIDEO
            3 -> MEDIA               // CATEGORY_IMAGE
            4 -> SOCIAL              // CATEGORY_SOCIAL
            5 -> SOCIAL              // CATEGORY_NEWS
            6 -> PRODUCTIVITY        // CATEGORY_MAPS
            7 -> PRODUCTIVITY        // CATEGORY_PRODUCTIVITY
            else -> OTHER
        }
    }
}
