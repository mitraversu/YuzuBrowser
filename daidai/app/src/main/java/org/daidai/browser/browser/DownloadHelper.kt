package org.daidai.browser.browser

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.URLUtil
import java.io.File

/** Hands downloads to the system DownloadManager — no storage permission needed. */
object DownloadHelper {

    fun enqueue(
        context: Context,
        url: String,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?,
    ) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) return
        val name = fileName(url, contentDisposition)
        val request = android.app.DownloadManager.Request(Uri.parse(url)).apply {
            setMimeType(mimeType)
            setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setTitle(name)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
            userAgent?.let { addRequestHeader("User-Agent", it) }
            CookieManager.getInstance().getCookie(url)?.let { addRequestHeader("Cookie", it) }
        }
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
        dm.enqueue(request)
    }

    private fun fileName(url: String, contentDisposition: String?): String {
        val fromDisposition = Regex("filename=\"?([^\";]+)\"?")
            .find(contentDisposition.orEmpty())
            ?.groupValues
            ?.getOrNull(1)
        fromDisposition?.let { return sanitize(it) }
        URLUtil.guessFileName(url, contentDisposition, null).let { return sanitize(it) }
    }

    private fun sanitize(name: String): String =
        name.replace(Regex("[^A-Za-z0-9._ ()\\-\\[\\]]"), "_").take(120).ifBlank { "download" }
}
