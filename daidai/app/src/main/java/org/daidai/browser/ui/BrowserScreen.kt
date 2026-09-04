package org.daidai.browser.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.daidai.browser.BrowserViewModel
import org.daidai.browser.actions.BrowserAction
import org.daidai.browser.browser.BrowserUiState

@Composable
fun BrowserScreen(
    vm: BrowserViewModel,
    ui: BrowserUiState,
    onAction: (BrowserAction) -> Unit,
) {
    val currentController = ui.currentTabId?.let { vm.controllerOf(it) }

    // Web history back takes priority over app navigation.
    BackHandler(enabled = currentController?.canGoBack() == true) {
        currentController?.goBack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column(
                Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding(),
            ) {
                ui.currentTab?.let { tab -> UrlBar(tab = tab, onSubmit = vm::loadInput) }
                ActionToolbar(
                    spec = ui.toolbarSpec,
                    onAction = onAction,
                    onEdit = vm::openToolbarEditor,
                )
            }
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
        ) {
            // Pause the previous tab's timers when switching away, resume the new one.
            DisposableEffect(ui.currentTabId) {
                currentController?.resume()
                onDispose { currentController?.pause() }
            }

            key(ui.currentTabId) {
                currentController?.webView?.let { view ->
                    AndroidView(
                        factory = { view },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            if (ui.showFindBar) {
                FindBar(
                    ui = ui,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .imePadding(),
                    onQuery = vm::findInPage,
                    onNext = { vm.findNext(true) },
                    onPrevious = { vm.findNext(false) },
                    onClose = vm::closeFindBar,
                )
            }
        }
    }

    if (ui.showTabSheet) {
        TabSheet(
            ui = ui,
            onSelect = vm::selectTab,
            onClose = vm::closeTab,
            onNewTab = { vm.newTab() },
            onDismiss = vm::dismissTabSheet,
        )
    }

    if (ui.showToolbarEditor) {
        ToolbarEditorSheet(
            spec = ui.toolbarSpec,
            onChange = vm::updateToolbarSpec,
            onDismiss = vm::closeToolbarEditor,
        )
    }
}
