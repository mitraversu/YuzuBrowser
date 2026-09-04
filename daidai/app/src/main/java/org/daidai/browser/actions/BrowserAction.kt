package org.daidai.browser.actions

/**
 * The reincarnation of Yuzu Browser's action system.
 *
 * In Yuzu, *everything* a button could do was an [BrowserAction] — and any
 * toolbar slot, soft button or gesture could be bound to any action. Daidai
 * keeps that DNA: the toolbar is just an ordered list of action ids, and this
 * sealed hierarchy is the vocabulary.
 *
 * When we grow, actions gain parameters (Yuzu had `StartIntentAction`,
 * `CustomSingleAction`, script actions…). Keep every action a small
 * immutable object.
 */
sealed interface BrowserAction {
    val id: String

    data object GoBack : BrowserAction { override val id = "go_back" }
    data object GoForward : BrowserAction { override val id = "go_forward" }
    data object Reload : BrowserAction { override val id = "reload" }
    data object StopLoading : BrowserAction { override val id = "stop" }
    data object NewTab : BrowserAction { override val id = "new_tab" }
    data object CloseTab : BrowserAction { override val id = "close_tab" }
    data object ToggleTabs : BrowserAction { override val id = "tabs" }
    data object SharePage : BrowserAction { override val id = "share_page" }
    data object BookmarkPage : BrowserAction { override val id = "bookmark_page" }
    data object OpenBookmarks : BrowserAction { override val id = "open_bookmarks" }
    data object FindInPage : BrowserAction { override val id = "find_in_page" }
    data object ToggleDesktopSite : BrowserAction { override val id = "desktop_site" }
    data object OpenSettings : BrowserAction { override val id = "settings" }
    data object OpenMenu : BrowserAction { override val id = "menu" }

    companion object {
        val all: List<BrowserAction> = listOf(
            GoBack, GoForward, Reload, StopLoading,
            NewTab, CloseTab, ToggleTabs,
            SharePage, BookmarkPage, OpenBookmarks,
            FindInPage, ToggleDesktopSite,
            OpenSettings, OpenMenu,
        )

        fun byId(id: String): BrowserAction? = all.firstOrNull { it.id == id }
    }
}
