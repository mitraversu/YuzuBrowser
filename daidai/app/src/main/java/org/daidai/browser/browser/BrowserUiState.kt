package org.daidai.browser.browser

import org.daidai.browser.data.Settings
import org.daidai.browser.data.Bookmark
import org.daidai.browser.toolbar.ToolbarSpec

data class BrowserUiState(
    val tabs: List<TabSnapshot> = emptyList(),
    val currentTabId: String? = null,
    val toolbarSpec: ToolbarSpec = ToolbarSpec.DEFAULT,
    val settings: Settings = Settings(),
    val bookmarks: List<Bookmark> = emptyList(),
    val showTabSheet: Boolean = false,
    val showToolbarEditor: Boolean = false,
    val showFindBar: Boolean = false,
    val findQuery: String = "",
    val findActiveIndex: Int = 0,
    val findCount: Int = 0,
    val blocklistHostCount: Int = 0,
    val blocklistUpdating: Boolean = false,
    val blocklistMessage: String? = null,
) {
    val currentTab: TabSnapshot?
        get() = tabs.firstOrNull { it.id == currentTabId }
}
