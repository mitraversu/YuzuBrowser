package org.daidai.browser.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import org.daidai.browser.DaidaiApplication
import org.daidai.browser.actions.BrowserAction
import org.daidai.browser.browser.BrowserViewModel

private const val ROUTE_BROWSER = "browser"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_BOOKMARKS = "bookmarks"

@Composable
fun DaidaiApp() {
    val context = LocalContext.current
    val container = (context.applicationContext as DaidaiApplication).container

    val vm: BrowserViewModel = viewModel(factory = viewModelFactory {
        initializer {
            BrowserViewModel(
                application = this[APPLICATION_KEY] as android.app.Application,
                container = container,
            )
        }
    })

    val ui by vm.ui.collectAsState()
    var route by rememberSaveable { mutableStateOf(ROUTE_BROWSER) }

    BackHandler(enabled = route != ROUTE_BROWSER) { route = ROUTE_BROWSER }

    // Actions that navigate screens are handled here; the rest go to the view model.
    val onAction: (BrowserAction) -> Unit = { action ->
        when (action) {
            BrowserAction.OpenSettings -> route = ROUTE_SETTINGS
            BrowserAction.OpenBookmarks -> route = ROUTE_BOOKMARKS
            else -> vm.execute(action)
        }
    }

    when (route) {
        ROUTE_SETTINGS -> SettingsScreen(
            ui = ui,
            onBack = { route = ROUTE_BROWSER },
            onSetSearchEngine = vm::setSearchEngine,
            onSetBlocklistEnabled = vm::setBlocklistEnabled,
            onSetBlocklistUrl = vm::setBlocklistUrl,
            onUpdateBlocklist = vm::updateBlocklist,
            onClearBrowsingData = vm::clearBrowsingData,
        )

        ROUTE_BOOKMARKS -> BookmarksScreen(
            ui = ui,
            onBack = { route = ROUTE_BROWSER },
            onOpen = { url ->
                vm.openInNewTab(url)
                route = ROUTE_BROWSER
            },
            onDelete = vm::removeBookmark,
        )

        else -> BrowserScreen(vm = vm, ui = ui, onAction = onAction)
    }
}
