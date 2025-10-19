package io.github.sanlorng.warmtones.ui.screen.setup

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.github.sanlorng.warmtones.ui.components.CustomPreview
import io.github.sanlorng.warmtones.ui.components.PageScaffold
import io.github.sanlorng.warmtones.ui.theme.WarmTonesTheme

@Composable
fun SetupEnvironmentScreen(
    state: SetupEnvironmentState,
    onEvent: (SetupEnvironmentEvent) -> Unit
) {

    PageScaffold(title = "环境配置") { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "为了确保所有功能都能正常使用，请授予必要的权限并安装所需的语音包。",
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            CheckItem(
                isChecked = state.isPermissionsGranted,
                onCheckedChange = { onEvent(SetupEnvironmentEvent.OnPermissionRequested) },
                shape = MaterialTheme.shapes.large.copy(bottomStart = CornerSize(0), bottomEnd = CornerSize(0))
            ) {
                Text("授予联系人和电话权限")
            }
            Spacer(modifier = Modifier.height(2.dp))
            CheckItem(
                isChecked = state.isTtsAvailable,
                onCheckedChange = { onEvent(SetupEnvironmentEvent.OnTtsInstallRequested) },
                shape = MaterialTheme.shapes.large.copy(topStart = CornerSize(0), topEnd = CornerSize(0))
            ) {
                Text("安装中文语音包")
            }

            Spacer(modifier = Modifier.height(64.dp))

        }
    }
}

@Composable
fun CheckItem(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    shape: Shape,
    content: @Composable () -> Unit
) {
    ListItem(
        leadingContent = {
            Checkbox(
                checked = isChecked,
                enabled = !isChecked,
                onCheckedChange = null
            )
        },
        headlineContent = content,
        modifier = Modifier.clip(shape).clickable(
            enabled = !isChecked,
            onClick = { onCheckedChange(!isChecked) }
        ),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    )
}

@VisibleForTesting
private class SetupEnvironmentScreenPreviewProvider : PreviewParameterProvider<SetupEnvironmentState> {
    override val values: Sequence<SetupEnvironmentState>
        get() = sequenceOf(
            SetupEnvironmentState(
                false, false
            ),
            SetupEnvironmentState(
                true, false
            ),
            SetupEnvironmentState(
                false, true
            )
        )
}


@CustomPreview
@Composable
private fun SetupEnvironmentScreenPreview(
    @PreviewParameter(SetupEnvironmentScreenPreviewProvider::class) state: SetupEnvironmentState
) {
    WarmTonesTheme {
        SetupEnvironmentScreen(
            state = state,
            onEvent = {}
        )
    }
}

