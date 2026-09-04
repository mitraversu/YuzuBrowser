package org.daidai.browser.adblock

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Hosts-file based ad & tracker blocker.
 *
 * Yuzu had a full filter-list engine (`module/adblock` with ABP syntax).
 * Daidai starts with the pragmatic 80/20: hosts-format lists intercepted in
 * [android.webkit.WebView.shouldInterceptRequest]. Filter syntax (EasyList
 * style) returns in M2/M3.
 */
class HostsBlocker(
    private val context: Context,
    private val scope: CoroutineScope,
) {

    @Volatile
    var enabled: Boolean = true

    @Volatile
    private var hosts: Set<String> = emptySet()

    private val _hostCount = MutableStateFlow(0)
    val hostCount: StateFlow<Int> = _hostCount.asStateFlow()

    private val blocklistFile: File
        get() = File(File(context.filesDir, "blocklist"), "hosts.txt")

    /** Called once at startup: load cached list; fetch if none yet. */
    fun initialize() {
        scope.launch(Dispatchers.IO) {
            loadFromDisk()
            if (hosts.isEmpty()) {
                refresh(DEFAULT_URL)
            }
        }
    }

    fun shouldBlock(uri: Uri?): Boolean {
        if (!enabled || uri == null) return false
        val scheme = uri.scheme ?: return false
        if (scheme != "http" && scheme != "https") return false
        val host = uri.host ?: return false
        return hosts.contains(host)
    }

    /** Downloads a hosts file and swaps it in atomically. Result = host count or error. */
    suspend fun refresh(url: String): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val text = download(url)
            val parsed = parse(text)
            blocklistFile.parentFile?.mkdirs()
            val tmp = File(blocklistFile.absolutePath + ".tmp")
            tmp.writeText(text)
            if (!tmp.renameTo(blocklistFile)) {
                blocklistFile.writeText(text)
                tmp.delete()
            }
            hosts = parsed
            _hostCount.value = parsed.size
            parsed.size
        }
    }

    private fun loadFromDisk() {
        val f = blocklistFile
        if (!f.exists()) return
        runCatching {
            hosts = parse(f.readText())
            _hostCount.value = hosts.size
        }
    }

    private fun download(url: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 15_000
        conn.readTimeout = 60_000
        conn.instanceFollowRedirects = true
        try {
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    private fun parse(text: String): Set<String> {
        val set = HashSet<String>(1 shl 18)
        for (rawLine in text.lineSequence()) {
            val line = rawLine.substringBefore('#').trim()
            if (line.isEmpty()) continue
            val host = line.split(' ', '\t').lastOrNull { it.isNotEmpty() } ?: continue
            if (host.contains('.')) set.add(host.lowercase())
        }
        return set
    }

    companion object {
        /**
         * StevenBlack hosts — a well maintained aggregated blocklist
         * (ads + trackers + malware). Users can point this at any hosts file.
         */
        const val DEFAULT_URL =
            "https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts"
    }
}
