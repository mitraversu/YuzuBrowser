package org.daidai.browser.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.daidai.browser.adblock.HostsBlocker
import org.daidai.browser.toolbar.ToolbarSpec

private val Context.dataStore by preferencesDataStore(name = "daidai_settings")

/**
 * Single small DataStore holding everything v0.1 persists: settings,
 * the toolbar spec, and bookmarks. Split into Room/whatnot when it hurts.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val SEARCH_ENGINE = stringPreferencesKey("search_engine")
        val BLOCKLIST_ENABLED = booleanPreferencesKey("blocklist_enabled")
        val BLOCKLIST_URL = stringPreferencesKey("blocklist_url")
        val TOOLBAR = stringPreferencesKey("toolbar_json")
        val BOOKMARKS = stringPreferencesKey("bookmarks_json")
    }

    private val json = Json { ignoreUnknownKeys = true }

    val settings: Flow<Settings> = context.dataStore.data.map { prefs ->
        Settings(
            searchEngineId = prefs[Keys.SEARCH_ENGINE] ?: Settings().searchEngineId,
            blocklistEnabled = prefs[Keys.BLOCKLIST_ENABLED] ?: true,
            blocklistUrl = prefs[Keys.BLOCKLIST_URL] ?: HostsBlocker.DEFAULT_URL,
        )
    }

    val toolbarSpec: Flow<ToolbarSpec> = context.dataStore.data.map { prefs ->
        ToolbarSpec.fromJson(prefs[Keys.TOOLBAR])
    }

    val bookmarks: Flow<List<Bookmark>> = context.dataStore.data.map { prefs ->
        runCatching {
            json.decodeFromString<List<Bookmark>>(prefs[Keys.BOOKMARKS] ?: "[]")
        }.getOrDefault(emptyList())
    }

    suspend fun setSearchEngine(id: String) {
        context.dataStore.edit { it[Keys.SEARCH_ENGINE] = id }
    }

    suspend fun setBlocklistEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BLOCKLIST_ENABLED] = enabled }
    }

    suspend fun setBlocklistUrl(url: String) {
        context.dataStore.edit { it[Keys.BLOCKLIST_URL] = url }
    }

    suspend fun setToolbarSpec(spec: ToolbarSpec) {
        context.dataStore.edit { it[Keys.TOOLBAR] = spec.toJson() }
    }

    suspend fun addBookmark(bookmark: Bookmark) {
        context.dataStore.edit { prefs ->
            val current = runCatching {
                json.decodeFromString<List<Bookmark>>(prefs[Keys.BOOKMARKS] ?: "[]")
            }.getOrDefault(emptyList())
            val next = (current.filterNot { it.url == bookmark.url } + bookmark)
                .sortedByDescending { it.addedAt }
            prefs[Keys.BOOKMARKS] = json.encodeToString(next)
        }
    }

    suspend fun removeBookmark(url: String) {
        context.dataStore.edit { prefs ->
            val current = runCatching {
                json.decodeFromString<List<Bookmark>>(prefs[Keys.BOOKMARKS] ?: "[]")
            }.getOrDefault(emptyList())
            prefs[Keys.BOOKMARKS] = json.encodeToString(current.filterNot { it.url == url })
        }
    }
}
