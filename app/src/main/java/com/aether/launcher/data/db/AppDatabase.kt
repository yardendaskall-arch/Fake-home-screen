package com.aether.launcher.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aether.launcher.data.db.dao.AppLaunchEventDao
import com.aether.launcher.data.db.dao.DockItemDao
import com.aether.launcher.data.db.dao.HiddenAppDao
import com.aether.launcher.data.db.dao.HomeItemDao
import com.aether.launcher.data.db.entities.AppLaunchEventEntity
import com.aether.launcher.data.db.entities.DockItemEntity
import com.aether.launcher.data.db.entities.HiddenAppEntity
import com.aether.launcher.data.db.entities.HomeItemEntity

@Database(
    entities = [
        HomeItemEntity::class,
        DockItemEntity::class,
        HiddenAppEntity::class,
        AppLaunchEventEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun homeItemDao(): HomeItemDao
    abstract fun dockItemDao(): DockItemDao
    abstract fun hiddenAppDao(): HiddenAppDao
    abstract fun appLaunchEventDao(): AppLaunchEventDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "aether_launcher.db",
            ).build().also { instance = it }
        }
    }
}
