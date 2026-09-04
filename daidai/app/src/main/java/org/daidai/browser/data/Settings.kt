package org.daidai.browser.data

import kotlinx.serialization.Serializable

@Serializable
data class Bookmark(
    val url: String,
    val title: String,
    val addedAt: Long,
)

data class Settings(
    val searchEngineId: String = SearchEngines.DEFAULT.id,
    val blocklistEnabled: Boolean = true,
    val blocklistUrl: String = "",
) {
    val searchEngine: SearchEngine get() = SearchEngines.byId(searchEngineId)
}
