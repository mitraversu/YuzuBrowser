package org.daidai.browser.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.daidai.browser.R
import org.daidai.browser.actions.ActionCatalog
import org.daidai.browser.toolbar.ToolbarSpec

/**
 * Assemble-your-own toolbar — the feature Yuzu was loved for.
 * v0.1: add / remove / reorder via buttons. Drag-and-drop lands in M2.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolbarEditorSheet(
    spec: ToolbarSpec,
    onChange: (ToolbarSpec) -> Unit,
    onDismiss: () -> Unit,
) {
    fun move(index: Int, delta: Int) {
        val target = index + delta
        if (target < 0 || target >= spec.slots.size) return
        val slots = spec.slots.toMutableList()
        val item = slots.removeAt(index)
        slots.add(target, item)
        onChange(ToolbarSpec(slots))
    }

    fun remove(index: Int) {
        val slots = spec.slots.toMutableList()
        slots.removeAt(index)
        onChange(ToolbarSpec(slots))
    }

    fun add(actionId: String) {
        val slots = spec.slots.toMutableList()
        slots.add(actionId)
        onChange(ToolbarSpec(slots))
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = stringResource(R.string.customize_toolbar),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Text(
                text = stringResource(R.string.toolbar_editor_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )

            spec.slots.forEachIndexed { index, actionId ->
                val entry = ActionCatalog.byId(actionId) ?: return@forEachIndexed
                ListItem(
                    headlineContent = { Text(stringResource(entry.labelRes)) },
                    leadingContent = { Icon(entry.icon, contentDescription = null) },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { move(index, -1) },
                                enabled = index > 0,
                            ) {
                                Icon(
                                    Icons.Filled.KeyboardArrowUp,
                                    contentDescription = stringResource(R.string.move_up),
                                )
                            }
                            IconButton(
                                onClick = { move(index, 1) },
                                enabled = index < spec.slots.lastIndex,
                            ) {
                                Icon(
                                    Icons.Filled.KeyboardArrowDown,
                                    contentDescription = stringResource(R.string.move_down),
                                )
                            }
                            IconButton(onClick = { remove(index) }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = stringResource(R.string.remove_action),
                                )
                            }
                        }
                    },
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            Text(
                text = stringResource(R.string.add_actions),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )

            ActionCatalog.all.forEach { entry ->
                ListItem(
                    headlineContent = { Text(stringResource(entry.labelRes)) },
                    leadingContent = { Icon(entry.icon, contentDescription = null) },
                    trailingContent = {
                        IconButton(onClick = { add(entry.action.id) }) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = stringResource(entry.labelRes),
                            )
                        }
                    },
                )
            }

            Spacer(Modifier.width(8.dp))
        }
    }
}
