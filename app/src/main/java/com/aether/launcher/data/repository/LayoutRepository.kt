package com.aether.launcher.data.repository

import com.aether.launcher.data.db.dao.DockItemDao
import com.aether.launcher.data.db.dao.HiddenAppDao
import com.aether.launcher.data.db.dao.HomeItemDao
import com.aether.launcher.data.db.entities.DockItemEntity
import com.aether.launcher.data.db.entities.HiddenAppEntity
import com.aether.launcher.data.db.entities.HomeItemEntity
import com.aether.launcher.data.model.DockItem
import com.aether.launcher.data.model.HomeItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class LayoutRepository(
    private val homeItemDao: HomeItemDao,
    private val dockItemDao: DockItemDao,
    private val hiddenAppDao: HiddenAppDao,
) {
    fun observeHomeItems(): Flow<List<HomeItem>> = homeItemDao.observeAll().map { rows -> rows.map { it.toDomain() } }
    fun observeDockItems(): Flow<List<DockItem>> = dockItemDao.observeAll().map { rows -> rows.map { it.toDomain() } }
    fun observeHiddenAppKeys(): Flow<Set<String>> = hiddenAppDao.observeAll().map { rows -> rows.map { it.appKey }.toSet() }

    suspend fun saveHomeItem(item: HomeItem) = homeItemDao.upsert(item.toEntity())
    suspend fun removeHomeItem(id: String) = homeItemDao.delete(id)
    suspend fun savePage(items: List<HomeItem>) = homeItemDao.upsertAll(items.map { it.toEntity() })

    suspend fun pinToDock(item: DockItem) = dockItemDao.upsert(DockItemEntity(item.id, item.slot, item.appKey))
    suspend fun unpinFromDock(id: String) = dockItemDao.delete(id)

    suspend fun hideApp(appKey: String) = hiddenAppDao.hide(HiddenAppEntity(appKey))
    suspend fun unhideApp(appKey: String) = hiddenAppDao.unhide(appKey)

    /** Serializes the whole layout to pretty JSON for local export/share. */
    suspend fun exportBackup(): String {
        val json = JSONObject()
        json.put("version", 1)
        json.put("homeItems", JSONArray(homeItemDao.getAllOnce().map { it.toJson() }))
        json.put("dockItems", JSONArray(dockItemDao.getAllOnce().map { it.toJson() }))
        return json.toString(2)
    }

    /** Replaces the current layout with one previously produced by [exportBackup]. */
    suspend fun importBackup(json: String) {
        val root = JSONObject(json)
        homeItemDao.clear()
        val homeItems = root.optJSONArray("homeItems") ?: JSONArray()
        for (i in 0 until homeItems.length()) {
            homeItemDao.upsert(homeItems.getJSONObject(i).toHomeEntity())
        }
        val dockItems = root.optJSONArray("dockItems") ?: JSONArray()
        for (i in 0 until dockItems.length()) {
            val obj = dockItems.getJSONObject(i)
            dockItemDao.upsert(DockItemEntity(obj.getString("id"), obj.getInt("slot"), obj.getString("appKey")))
        }
    }
}

private fun HomeItemEntity.toDomain(): HomeItem = when (type) {
    "folder" -> HomeItem.Folder(id, page, col, row, folderName.orEmpty(), folderAppKeys?.split(",")?.filter { it.isNotBlank() } ?: emptyList())
    "widget" -> HomeItem.Widget(id, page, col, row, spanX, spanY, appWidgetId ?: -1)
    else -> HomeItem.AppIcon(id, page, col, row, appKey.orEmpty())
}

private fun DockItemEntity.toDomain() = DockItem(id, slot, appKey)

private fun HomeItem.toEntity(): HomeItemEntity = when (this) {
    is HomeItem.AppIcon -> HomeItemEntity(id, "app", page, col, row, spanX, spanY, appKey = appKey)
    is HomeItem.Folder -> HomeItemEntity(id, "folder", page, col, row, spanX, spanY, folderName = name, folderAppKeys = appKeys.joinToString(","))
    is HomeItem.Widget -> HomeItemEntity(id, "widget", page, col, row, spanX, spanY, appWidgetId = appWidgetId)
}

private fun HomeItemEntity.toJson(): JSONObject = JSONObject().apply {
    put("id", id); put("type", type); put("page", page); put("col", col); put("row", row)
    put("spanX", spanX); put("spanY", spanY)
    appKey?.let { put("appKey", it) }
    folderName?.let { put("folderName", it) }
    folderAppKeys?.let { put("folderAppKeys", it) }
    appWidgetId?.let { put("appWidgetId", it) }
}

private fun DockItemEntity.toJson(): JSONObject = JSONObject().apply {
    put("id", id); put("slot", slot); put("appKey", appKey)
}

private fun JSONObject.toHomeEntity(): HomeItemEntity = HomeItemEntity(
    id = getString("id"),
    type = getString("type"),
    page = getInt("page"),
    col = getInt("col"),
    row = getInt("row"),
    spanX = optInt("spanX", 1),
    spanY = optInt("spanY", 1),
    appKey = if (has("appKey")) getString("appKey") else null,
    folderName = if (has("folderName")) getString("folderName") else null,
    folderAppKeys = if (has("folderAppKeys")) getString("folderAppKeys") else null,
    appWidgetId = if (has("appWidgetId")) optInt("appWidgetId") else null,
)
