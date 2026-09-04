package org.daidai.browser.browser

/** Callbacks a tab pushes up to the browser layer. */
interface TabEvents {
    fun onSnapshot(snapshot: TabSnapshot)
    fun onExternalUrl(url: String)
    fun onDownload(
        url: String,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?,
    )
    fun onFindResult(activeMatch: Int, numberOfMatches: Int, isDoneCounting: Boolean)
}
