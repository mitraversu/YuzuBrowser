package org.daidai.browser.actions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tab
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.annotation.StringRes
import org.daidai.browser.R

/**
 * The catalog every customization UI lists actions from —
 * Yuzu's `ActionNameArray` / `ActionIconMap` reborn.
 */
data class ActionSpec(
    val action: BrowserAction,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
)

object ActionCatalog {

    val all: List<ActionSpec> = listOf(
        ActionSpec(BrowserAction.GoBack, R.string.action_go_back, Icons.AutoMirrored.Filled.ArrowBack),
        ActionSpec(BrowserAction.GoForward, R.string.action_go_forward, Icons.AutoMirrored.Filled.ArrowForward),
        ActionSpec(BrowserAction.Reload, R.string.action_reload, Icons.Filled.Refresh),
        ActionSpec(BrowserAction.StopLoading, R.string.action_stop, Icons.Filled.Close),
        ActionSpec(BrowserAction.NewTab, R.string.action_new_tab, Icons.Filled.Add),
        ActionSpec(BrowserAction.CloseTab, R.string.action_close_tab, Icons.Filled.RemoveCircle),
        ActionSpec(BrowserAction.ToggleTabs, R.string.action_tabs, Icons.Filled.Tab),
        ActionSpec(BrowserAction.SharePage, R.string.action_share_page, Icons.Filled.Share),
        ActionSpec(BrowserAction.BookmarkPage, R.string.action_bookmark_page, Icons.Filled.Bookmark),
        ActionSpec(BrowserAction.OpenBookmarks, R.string.action_open_bookmarks, Icons.AutoMirrored.Filled.List),
        ActionSpec(BrowserAction.FindInPage, R.string.action_find_in_page, Icons.Filled.Search),
        ActionSpec(BrowserAction.ToggleDesktopSite, R.string.action_desktop_site, Icons.Filled.DesktopWindows),
        ActionSpec(BrowserAction.OpenSettings, R.string.action_settings, Icons.Filled.Settings),
        ActionSpec(BrowserAction.OpenMenu, R.string.action_menu, Icons.Filled.MoreVert),
    )

    fun byId(id: String): ActionSpec? = all.firstOrNull { it.action.id == id }
}
