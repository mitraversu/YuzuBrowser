package org.daidai.browser.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.daidai.browser.R
import org.daidai.browser.actions.ActionCatalog
import org.daidai.browser.actions.BrowserAction
import org.daidai.browser.toolbar.ToolbarSpec

/** Extra actions reachable from the menu slot. The menu itself becomes customizable in M2. */
private val MENU_ACTIONS = listOf(
    BrowserAction.Reload,
    BrowserAction.SharePage,
    BrowserAction.BookmarkPage,
    BrowserAction.OpenBookmarks,
    BrowserAction.FindInPage,
    BrowserAction.ToggleDesktopSite,
    BrowserAction.OpenSettings,
)

/**
 * Renders a user-assembled toolbar spec. Every slot is long-pressable → editor.
 * The spiritual successor of Yuzu's `Toolbar.kt` + `ActionExecutor`.
 */
@Composable
fun ActionToolbar(
    spec: ToolbarSpec,
    onAction: (BrowserAction) -> Unit,
    onEdit: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        spec.slots.forEach { actionId ->
            val entry = ActionCatalog.byId(actionId) ?: return@forEach
            if (entry.action is BrowserAction.OpenMenu) {
                MenuActionSlot(onAction = onAction, onEdit = onEdit)
            } else {
                ActionSlot(
                    icon = entry.icon,
                    label = stringResource(entry.labelRes),
                    onClick = { onAction(entry.action) },
                    onLongClick = onEdit,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ActionSlot(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.Button,
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun MenuActionSlot(
    onAction: (BrowserAction) -> Unit,
    onEdit: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        ActionSlot(
            icon = Icons.Filled.MoreVert,
            label = stringResource(R.string.action_menu),
            onClick = { expanded = true },
            onLongClick = onEdit,
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            MENU_ACTIONS.forEach { action ->
                val entry = ActionCatalog.byId(action.id) ?: return@forEach
                DropdownMenuItem(
                    text = { Text(stringResource(entry.labelRes)) },
                    leadingIcon = { Icon(entry.icon, contentDescription = null) },
                    onClick = {
                        expanded = false
                        onAction(entry.action)
                    },
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(stringResource(R.string.customize_toolbar)) },
                leadingIcon = { Icon(Icons.Filled.Build, contentDescription = null) },
                onClick = {
                    expanded = false
                    onEdit()
                },
            )
        }
    }
}
