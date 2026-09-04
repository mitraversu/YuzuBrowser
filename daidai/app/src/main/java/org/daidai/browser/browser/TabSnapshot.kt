package org.daidai.browser.browser

/** Immutable, observable state of one tab — what the UI renders. */
data class TabSnapshot(
    val id: String,
    val title: String,
    val url: String,
    val isSecure: Boolean,
    val progress: Int,
    val loading: Boolean,
    val isDesktopMode: Boolean = false,
)
