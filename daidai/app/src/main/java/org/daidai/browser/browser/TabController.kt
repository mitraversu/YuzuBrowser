package org.daidai.browser.browser

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Message
import android.view.View
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import org.daidai.browser.adblock.HostsBlocker
import java.io.ByteArrayInputStream

/**
 * Owns and configures one WebView — the system engine, wrapped.
 * The deliberate seam for M4: when GeckoView arrives, this is the class
 * that gets an interface extraction (Yuzu never got that far).
 */
@SuppressLint("SetJavaScriptEnabled")
class TabController(
    context: Context,
    val id: String,
    private val events: TabEvents,
    private val blocker: HostsBlocker,
) {

    val webView: WebView

    private var snapshot = TabSnapshot(
        id = id,
        title = "",
        url = "about:blank",
        isSecure = false,
        progress = 0,
        loading = false,
    )

    init {
        webView = WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = false
                allowContentAccess = false
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                builtInZoomControls = true
                displayZoomControls = false
                loadWithOverviewMode = true
                useWideViewPort = true
                setSupportZoom(true)
            }
            isVerticalScrollBarEnabled = true
            webViewClient = DaidaiWebViewClient()
            webChromeClient = DaidaiChromeClient()
            setFindListener { activeMatch, numberOfMatches, isDoneCounting ->
                events.onFindResult(activeMatch, numberOfMatches, isDoneCounting)
            }
            setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
                events.onDownload(url, userAgent, contentDisposition, mimeType)
            }
        }
    }

    fun snapshot(): TabSnapshot = snapshot

    private fun publish(mutate: TabSnapshot.() -> TabSnapshot) {
        snapshot = snapshot.mutate()
        events.onSnapshot(snapshot)
    }

    fun load(url: String) {
        if (url.isBlank()) return
        publish { copy(url = url, loading = true, progress = 5) }
        webView.loadUrl(url)
    }

    fun goBack() = webView.goBack()
    fun goForward() = webView.goForward()
    fun reload() = webView.reload()
    fun stop() = webView.stopLoading()
    fun canGoBack(): Boolean = webView.canGoBack()
    fun canGoForward(): Boolean = webView.canGoForward()

    fun resume() = webView.onResume()
    fun pause() = webView.onPause()

    fun toggleDesktopSite() {
        val desktop = !snapshot.isDesktopMode
        webView.settings.userAgentString =
            if (desktop) DESKTOP_USER_AGENT else null
        publish { copy(isDesktopMode = desktop) }
        reload()
    }

    fun findAll(query: String) {
        if (query.isEmpty()) {
            webView.clearMatches()
        } else {
            webView.findAllAsync(query)
        }
    }

    fun findNext(forward: Boolean) = webView.findNext(forward)

    fun clearFind() = webView.clearMatches()

    fun destroy() = webView.destroy()

    private inner class DaidaiWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?,
        ): Boolean {
            val uri = request?.url ?: return false
            val scheme = uri.scheme ?: return false
            return if (scheme == "http" || scheme == "https") {
                false // stay in the browser
            } else {
                events.onExternalUrl(uri.toString())
                true
            }
        }

        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?,
        ): WebResourceResponse? {
            if (request == null) return null
            return if (request.isForMainFrame.not() && blocker.shouldBlock(request.url)) {
                WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
            } else {
                null
            }
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            val u = url ?: return
            publish {
                copy(
                    url = u,
                    loading = true,
                    progress = 5,
                    isSecure = u.startsWith("https://"),
                )
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            val u = url ?: return
            publish {
                copy(
                    url = u,
                    loading = false,
                    progress = 100,
                    isSecure = u.startsWith("https://"),
                )
            }
        }

        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?,
        ) {
            // Security over convenience: never proceed past a broken certificate.
            handler?.cancel()
        }
    }

    private inner class DaidaiChromeClient : WebChromeClient() {

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            publish {
                copy(
                    progress = newProgress,
                    loading = newProgress < 100,
                    title = title.ifBlank { url },
                )
            }
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            if (title.isNullOrBlank()) return
            publish { copy(title = title) }
        }

        override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
            // favicons in the tab sheet from M2
        }

        override fun onCreateWindow(
            view: WebView?,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message?,
        ): Boolean {
            // target="_blank" opens are handled in M2 (popup/tab handling)
            return false
        }

        override fun onGeolocationPermissionsShowPrompt(
            origin: String?,
            callback: GeolocationPermissions.Callback?,
        ) {
            // location is not granted silently, ever
            callback?.invoke(origin, false, false)
        }

        override fun onPermissionRequest(request: PermissionRequest?) {
            // camera/mic via web pages is out of scope for v0.1
            request?.deny()
        }
    }

    companion object {
        private const val DESKTOP_USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/124.0.0.0 Safari/537.36"

        /** Called by BrowserViewModel when clearing browsing data. */
        fun cookieManager(): CookieManager = CookieManager.getInstance()
    }
}
