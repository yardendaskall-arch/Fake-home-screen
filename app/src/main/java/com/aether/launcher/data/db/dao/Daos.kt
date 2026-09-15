package com.aether.launcher.data.db.dao

import androidx.room.*
import com.aether.launcher.data.db.entities.AppLaunchEventEntity
import com.aether.launcher.data.db.entities.DockItemEntity
import com.aether.launcher.data.db.entities.HiddenAppEntity
import com.aether.launcher.data.db.entities.HomeItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeItemDao {
    @Query("SELECT * FROM home_items ORDER BY page, row, col")
    fun observeAll(): Flow<List<HomeItemEntity>>

    @Upsert
    suspend fun upsert(item: HomeItemEntity)

    @Upsert
    suspend fun upsertAll(items: List<HomeItemEntity>)

    @Query("DELETE FROM home_items WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM home_items")
    suspend fun clear()

    @Query("SELECT * FROM home_items")
    suspend fun getAllOnce(): List<HomeItemEntity>
}

@Dao
interface DockItemDao {
    @Query("SELECT * FROM dock_items ORDER BY slot")
    fun observeAll(): Flow<List<DockItemEntity>>

    @Upsert
    suspend fun upsert(item: DockItemEntity)

    @Query("DELETE FROM dock_items WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM dock_items")
    suspend fun getAllOnce(): List<DockItemEntity>
}

@Dao
interface HiddenAppDao {
    @Query("SELECT * FROM hidden_apps")
    fun observeAll(): Flow<List<HiddenAppEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun hide(entity: HiddenAppEntity)

    @Query("DELETE FROM hidden_apps WHERE appKey = :appKey")
    suspend fun unhide(appKey: String)
}

@Dao
interface AppLaunchEventDao {
    @Insert
    suspend fun record(event: AppLaunchEventEntity)

    /** Ranks apps for the current hour-of-day / day-of-week bucket, most-used first. */
    @Query(
        """
        SELECT appKey FROM app_launch_events
        WHERE hourOfDay BETWEEN :hourStart AND :hourEnd
        GROUP BY appKey
        ORDER BY COUNT(*) DESC
        LIMIT :limit
        """
    )
    suspend fun topAppsForHourRange(hourStart: Int, hourEnd: Int, limit: Int): List<String>

    @Query("SELECT appKey FROM app_launch_events GROUP BY appKey ORDER BY COUNT(*) DESC LIMIT :limit")
    suspend fun topAppsOverall(limit: Int): List<String>

    @Query("DELETE FROM app_launch_events WHERE timestampMillis < :olderThan")
    suspend fun pruneOlderThan(olderThan: Long)
}
