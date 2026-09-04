package org.daidai.browser.data

import android.net.Uri

data class SearchEngine(
    val id: String,
    val name: String,
    private val queryTemplate: String,
) {
    fun buildQuery(terms: String): String = queryTemplate.replace("%s", Uri.encode(terms))
}

object SearchEngines {

    val DUCK_DUCK_GO = SearchEngine("duckduckgo", "DuckDuckGo", "https://duckduckgo.com/?q=%s")
    val BRAVE = SearchEngine("brave", "Brave Search", "https://search.brave.com/search?q=%s")
    val STARTPAGE = SearchEngine("startpage", "Startpage", "https://www.startpage.com/sp/search?query=%s")
    val GOOGLE = SearchEngine("google", "Google", "https://www.google.com/search?q=%s")
    val BING = SearchEngine("bing", "Bing", "https://www.bing.com/search?q=%s")

    val ALL = listOf(DUCK_DUCK_GO, BRAVE, STARTPAGE, GOOGLE, BING)

    val DEFAULT = DUCK_DUCK_GO

    fun byId(id: String): SearchEngine = ALL.firstOrNull { it.id == id } ?: DEFAULT
}
