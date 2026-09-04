package org.daidai.browser.browser

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebViewDatabase
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.daidai.browser.AppContainer
import org.daidai.browser.actions.BrowserAction
import org.daidai.browser.data.Bookmark
import org.daidai.browser.toolbar.ToolbarSpec
import java.util.UUID

/**
 * The conductor: owns tabs (and their WebViews), executes actions,
 * and exposes one immutable [BrowserUiState] for the Compose UI.
 */
class BrowserViewModel(
    application: Application,
    private val container: AppContainer,
) : AndroidViewModel(application) {

    private val _ui = MutableStateFlow(BrowserUiState())
    val ui = _ui.asStateFlow()

    private val tabs = LinkedHashMap<String, TabController>()

    private val events = object : TabEvents {
        override fun onSnapshot(snapshot: TabSnapshot) {
            _ui.update { state ->
                state.copy(tabs = tabs.values.map { if (it.id == snapshot.id) snapshot else it.snapshot() })
            }
        }

        override fun onExternalUrl(url: String) {
            runCatching {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                getApplication<Application>().startActivity(intent)
            }
        }

        override fun onDownload(
            url: String,
            userAgent: String?,
            contentDisposition: String?,
            mimeType: String?,
        ) {
            DownloadHelper.enqueue(getApplication(), url, userAgent, contentDisposition, mimeType)
        }

        override fun onFindResult(activeMatch: Int, numberOfMatches: Int, isDoneCounting: Boolean) {
            _ui.update {
                it.copy(findActiveIndex = activeMatch + 1, findCount = numberOfMatches)
            }
        }
    }

    init {
        viewModelScope.launch {
            container.settings.settings.collect { s ->
                container.blocker.enabled = s.blocklistEnabled
                _ui.update { it.copy(settings = s) }
            }
        }
        viewModelScope.launch {
            container.settings.toolbarSpec.collect { spec ->
                _ui.update { it.copy(toolbarSpec = spec) }
            }
        }
        viewModelScope.launch {
            container.settings.bookmarks.collect { bookmarks ->
                _ui.update { it.copy(bookmarks = bookmarks) }
            }
        }
        viewModelScope.launch {
            container.blocker.hostCount.collect { count ->
                _ui.update { it.copy(blocklistHostCount = count) }
            }
        }
        viewModelScope.launch {
            container.pendingUrl.collect { url -> newTab(url) }
        }
        container.blocker.initialize()

        newTab()
    }

    fun currentController(): TabController? = _ui.value.currentTabId?.let { tabs[it] }

    fun controllerOf(tabId: String): TabController? = tabs[tabId]

    fun newTab(url: String? = null) {
        val id = UUID.randomUUID().toString()
        val controller = TabController(getApplication(), id, events, container.blocker)
        tabs[id] = controller
        _ui.update { state ->
            state.copy(
                tabs = tabs.values.map { it.snapshot() },
                currentTabId = id,
                showTabSheet = false,
            )
        }
        controller.load(url ?: "about:blank")
    }

    fun selectTab(id: String) {
        if (id == _ui.value.currentTabId) {
            _ui.update { it.copy(showTabSheet = false) }
            return
        }
        _ui.update { it.copy(currentTabId = id, showTabSheet = false) }
    }

    fun closeTab(id: String) {
        val controller = tabs.remove(id) ?: return
        val wasCurrent = id == _ui.value.currentTabId
        controller.destroy()
        if (tabs.isEmpty()) {
            newTab()
            return
        }
        _ui.update { state ->
            val list = tabs.values.map { it.snapshot() }
            var next = state.currentTabId
            if (wasCurrent) {
                next = list.lastOrNull()?.id
            }
            state.copy(tabs = list, currentTabId = next)
        }
    }

    fun loadInput(rawInput: String) {
        val input = rawInput.trim()
        if (input.isEmpty()) return
        val address = AddressResolver.resolve(input, _ui.value.settings.searchEngine)
        currentController()?.load(address)
    }

    fun execute(action: BrowserAction) {
        val tab = currentController()
        when (action) {
            is BrowserAction.GoBack -> tab?.takeIf { it.canGoBack() }?.goBack()
            is BrowserAction.GoForward -> tab?.takeIf { it.canGoForward() }?.goForward()
            is BrowserAction.Reload -> tab?.reload()
            is BrowserAction.StopLoading -> tab?.stop()
            is BrowserAction.NewTab -> newTab()
            is BrowserAction.CloseTab -> _ui.value.currentTabId?.let(::closeTab)
            is BrowserAction.ToggleTabs -> _ui.update { it.copy(showTabSheet = !it.showTabSheet) }
            is BrowserAction.SharePage -> shareCurrentPage()
            is BrowserAction.BookmarkPage -> bookmarkCurrentPage()
            is BrowserAction.FindInPage -> _ui.update { it.copy(showFindBar = true) }
            is BrowserAction.ToggleDesktopSite -> tab?.toggleDesktopSite()
            // OpenBookmarks / OpenSettings / OpenMenu are navigation concerns —
            // the UI layer handles those before reaching the view model.
            is BrowserAction.OpenBookmarks,
            is BrowserAction.OpenSettings,
            is BrowserAction.OpenMenu -> Unit
        }
    }

    private fun shareCurrentPage() {
        val snap = _ui.value.currentTab ?: return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, snap.url)
            putExtra(Intent.EXTRA_TITLE, snap.title)
        }
        getApplication<Application>().startActivity(
            Intent.createChooser(intent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    private fun bookmarkCurrentPage() {
        val snap = _ui.value.currentTab ?: return
        if (snap.url == "about:blank") return
        viewModelScope.launch {
            container.settings.addBookmark(
                Bookmark(url = snap.url, title = snap.title.ifBlank { snap.url }, addedAt = System.currentTimeMillis()),
            )
        }
    }

    fun removeBookmark(url: String) {
        viewModelScope.launch { container.settings.removeBookmark(url) }
    }

    fun openInNewTab(url: String) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) return
        newTab(url)
    }

    // --- Find in page ---

    fun findInPage(query: String) {
        _ui.update { it.copy(findQuery = query) }
        currentController()?.findAll(query)
    }

    fun findNext(forward: Boolean) {
        currentController()?.findNext(forward)
    }

    fun closeFindBar() {
        currentController()?.clearFind()
        _ui.update { it.copy(showFindBar = false, findQuery = "", findCount = 0, findActiveIndex = 0) }
    }

    // --- Toolbar customization ---

    fun openToolbarEditor() = _ui.update { it.copy(showToolbarEditor = true) }

    fun closeToolbarEditor() = _ui.update { it.copy(showToolbarEditor = false) }

    fun updateToolbarSpec(spec: ToolbarSpec) {
        _ui.update { it.copy(toolbarSpec = spec) }
        viewModelScope.launch { container.settings.setToolbarSpec(spec) }
    }

    // --- Settings ---

    fun setSearchEngine(id: String) {
        viewModelScope.launch { container.settings.setSearchEngine(id) }
    }

    fun setBlocklistEnabled(enabled: Boolean) {
        viewModelScope.launch { container.settings.setBlocklistEnabled(enabled) }
    }

    fun setBlocklistUrl(url: String) {
        viewModelScope.launch { container.settings.setBlocklistUrl(url) }
    }

    fun updateBlocklist() {
        viewModelScope.launch {
            _ui.update { it.copy(blocklistUpdating = true, blocklistMessage = null) }
            val result = container.blocker.refresh(_ui.value.settings.blocklistUrl)
            _ui.update {
                it.copy(
                    blocklistUpdating = false,
                    blocklistMessage = if (result.isSuccess) {
                        "ok:${result.getOrDefault(0)}"
                    } else {
                        "error"
                    },
                )
            }
        }
    }

    fun clearBrowsingData() {
        val context = getApplication<Application>()
        CookieManager.getInstance().apply {
            removeAllCookies(null)
            removeSessionCookies(null)
            flush()
        }
        WebStorage.getInstance().deleteAllData()
        WebViewDatabase.getInstance(context).apply {
            clearFormData()
            clearHttpAuthUsernamePassword()
        }
    }

    // --- Sheets ---

    fun dismissTabSheet() = _ui.update { it.copy(showTabSheet = false) }

    override fun onCleared() {
        tabs.values.forEach { it.destroy() }
        tabs.clear()
        super.onCleared()
    }
}

/** Decides whether typed text is a URL or a search query. */
object AddressResolver {
    fun resolve(input: String, engine: org.daidai.browser.data.SearchEngine): String {
        if (input.startsWith("http://") || input.startsWith("https://")) return input
        if (input.startsWith("about:")) return input
        val looksLikeHost =
            !input.contains(' ') &&
                (input.contains('.') || input.equals("localhost", ignoreCase = true))
        if (looksLikeHost) return "https://$input"
        return engine.buildQuery(input)
    }
}
