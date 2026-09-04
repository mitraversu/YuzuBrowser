package org.daidai.browser.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.daidai.browser.BuildConfig
import org.daidai.browser.R
import org.daidai.browser.browser.BrowserUiState
import org.daidai.browser.data.SearchEngines

@Composable
fun SettingsScreen(
    ui: BrowserUiState,
    onBack: () -> Unit,
    onSetSearchEngine: (String) -> Unit,
    onSetBlocklistEnabled: (Boolean) -> Unit,
    onSetBlocklistUrl: (String) -> Unit,
    onUpdateBlocklist: () -> Unit,
    onClearBrowsingData: () -> Unit,
) {
    var cleared by remember { mutableStateOf(false) }
    val blocklistMessage = ui.blocklistMessage

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_go_back),
                )
            }
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        }

        SectionHeader(stringResource(R.string.search_engine))
        SearchEngines.ALL.forEach { engine ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSetSearchEngine(engine.id) },
            ) {
                RadioButton(
                    selected = ui.settings.searchEngineId == engine.id,
                    onClick = { onSetSearchEngine(engine.id) },
                )
                Text(text = engine.name, style = MaterialTheme.typography.bodyLarge)
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 16.dp))

        SectionHeader(stringResource(R.string.ad_block))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.ad_block),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = if (ui.blocklistUpdating) {
                        stringResource(R.string.blocklist_updating)
                    } else {
                        stringResource(R.string.blocklist_hosts, ui.blocklistHostCount)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Switch(
                checked = ui.settings.blocklistEnabled,
                onCheckedChange = onSetBlocklistEnabled,
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = ui.settings.blocklistUrl,
            onValueChange = onSetBlocklistUrl,
            singleLine = true,
            label = { Text(stringResource(R.string.blocklist_source)) },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onUpdateBlocklist,
            enabled = !ui.blocklistUpdating,
        ) {
            Text(
                text = if (ui.blocklistUpdating) {
                    stringResource(R.string.blocklist_updating)
                } else {
                    stringResource(R.string.update_blocklist)
                },
            )
        }
        blocklistMessage?.let { message ->
            Spacer(Modifier.height(4.dp))
            if (message == "error") {
                Text(
                    text = stringResource(R.string.blocklist_update_failed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
                val count = message.removePrefix("ok:").toIntOrNull() ?: 0
                Text(
                    text = stringResource(R.string.blocklist_update_ok, count),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 16.dp))

        OutlinedButton(
            onClick = {
                onClearBrowsingData()
                cleared = true
            },
        ) { Text(stringResource(R.string.clear_browsing_data)) }
        if (cleared) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.browsing_data_cleared),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        HorizontalDivider(Modifier.padding(vertical = 16.dp))

        SectionHeader(stringResource(R.string.about))
        Text(
            text = stringResource(R.string.tagline),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.about_lineage),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "v${BuildConfig.VERSION_NAME} · ${stringResource(R.string.license)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}
