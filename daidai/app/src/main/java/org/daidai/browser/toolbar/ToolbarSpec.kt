package org.daidai.browser.toolbar

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.daidai.browser.actions.BrowserAction

/**
 * A toolbar is just an ordered list of action ids — user-assembled,
 * persisted as JSON, rendered by the UI.
 *
 * This is the direct descendant of Yuzu's `ActionFile` layouts. In later
 * milestones a spec will describe multiple bars, slot sizes, gestures
 * (long-press/double-tap bindings) and free-position buttons.
 */
@Serializable
data class ToolbarSpec(
    val slots: List<String> = DEFAULT_SLOTS,
) {

    fun actions(): List<BrowserAction> = slots.mapNotNull { BrowserAction.byId(it) }

    fun toJson(): String = Json.encodeToString(this)

    companion object {
        val DEFAULT_SLOTS = listOf(
            BrowserAction.GoBack.id,
            BrowserAction.GoForward.id,
            BrowserAction.ToggleTabs.id,
            BrowserAction.NewTab.id,
            BrowserAction.OpenMenu.id,
        )

        val DEFAULT = ToolbarSpec(DEFAULT_SLOTS)

        private val json = Json { ignoreUnknownKeys = true }

        fun fromJson(raw: String?): ToolbarSpec =
            if (raw.isNullOrBlank()) DEFAULT
            else runCatching { json.decodeFromString<ToolbarSpec>(raw) }.getOrDefault(DEFAULT)
    }
}
