package io.github.sanlorng.warmtones.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.sanlorng.warmtones.ui.components.CustomPreview
import io.github.sanlorng.warmtones.ui.components.PageScaffold
import io.github.sanlorng.warmtones.ui.theme.WarmTonesTheme

@Composable
fun SettingsScreen(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    onBack: () -> Unit
) {
    PageScaffold(
        title = "设置",
        onBack = onBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ListItem(
                headlineContent = { Text("拨打电话前二次确认") },
                supportingContent = { Text(if (state.isDialConfirmationEnabled) "已开启" else "已关闭") },
                trailingContent = {
                    Switch(
                        checked = state.isDialConfirmationEnabled,
                        onCheckedChange = null
                    )
                },
                modifier = Modifier.clickable {
                    onEvent(SettingsEvent.ToggleDialConfirmation(!state.isDialConfirmationEnabled))
                }
            )
            ListItem(
                headlineContent = { Text("列表样式") },
                supportingContent = { Text(if (state.isPagerModeEnabled) "垂直翻页" else "普通列表") },
                trailingContent = {
                    Switch(
                        checked = state.isPagerModeEnabled,
                        onCheckedChange = null
                    )
                },
                modifier = Modifier.clickable {
                    onEvent(SettingsEvent.TogglePagerMode(!state.isPagerModeEnabled))
                }
            )
            ListItem(
                headlineContent = { Text("左手模式") },
                supportingContent = { Text(if (state.isLeftHandedModeEnabled) "已开启" else "已关闭") },
                trailingContent = {
                    Switch(
                        checked = state.isLeftHandedModeEnabled,
                        onCheckedChange = null
                    )
                },
                modifier = Modifier.clickable {
                    onEvent(SettingsEvent.ToggleLeftHandedMode(!state.isLeftHandedModeEnabled))
                }
            )
        }
    }
}

@CustomPreview
@Composable
fun SettingsScreenPreview() {
    WarmTonesTheme {
        SettingsScreen(
            state = SettingsState(
                isDialConfirmationEnabled = true,
                isPagerModeEnabled = false,
                isLeftHandedModeEnabled = true
            ),
            onEvent = {},
            onBack = {}
        )
    }
}
