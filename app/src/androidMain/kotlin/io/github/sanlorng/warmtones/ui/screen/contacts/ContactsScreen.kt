package io.github.sanlorng.warmtones.ui.screen.contacts

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import io.github.sanlorng.warmtones.R
import io.github.sanlorng.warmtones.ui.components.CustomPreview
import io.github.sanlorng.warmtones.ui.components.FilledIconButton
import io.github.sanlorng.warmtones.ui.components.FilledTonalIconButton
import io.github.sanlorng.warmtones.ui.components.IconButton
import io.github.sanlorng.warmtones.ui.components.PageScaffold
import io.github.sanlorng.warmtones.ui.theme.WarmTonesTheme
import io.github.sanlorng.warmtones.ui.util.toColor
import kotlinx.coroutines.flow.MutableSharedFlow

@Composable
fun ContactsScreen(
    state: ContactsState,
    onEvent: (ContactsEvent) -> Unit,
    sideEffects: kotlinx.coroutines.flow.SharedFlow<ContactsViewModel.SideEffect>,
    onNavigateToSettings: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val allGranted = permissions.all { it.value }
            onEvent(ContactsEvent.PermissionResult(allGranted))
        }
    )

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        sideEffects.collect {
            when (it) {
                is ContactsViewModel.SideEffect.Dial -> {
                    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${it.phoneNumber}"))
                    context.startActivity(intent)
                }
            }
        }
    }

    if (state.showInstallTtsDataDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(ContactsEvent.DismissInstallTtsDataDialog) },
            title = { Text("缺少语音包") },
            text = { Text("您的手机缺少中文语音包，导致无法播报。是否现在前往系统设置进行下载？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val installIntent = Intent()
                        installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                        context.startActivity(installIntent)
                        onEvent(ContactsEvent.DismissInstallTtsDataDialog)
                    }
                ) {
                    Text("下载")
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(ContactsEvent.DismissInstallTtsDataDialog) }) {
                    Text("取消")
                }
            }
        )
    }

    PageScaffold(
        title = "联系人",
        actions = {
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    painter = painterResource(R.drawable.settings_24px),
                    contentDescription = "设置"
                )
            }
        },
        floatingActionButton = {},
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
            ) {
                val dialButton = @Composable {
                    FilledIconButton(
                        onClick = { onEvent(ContactsEvent.DialContact) },
                        enabled = state.selectedIndex != -1,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (state.isDialConfirmationPending) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                Color.Unspecified
                            },
                            contentColor = Color.Unspecified
                        ),
                        modifier = Modifier.size(IconButtonDefaults.largeContainerSize())
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.call_24px),
                            contentDescription = "拨号"
                        )
                    }
                }
                val spacer = @Composable {
                    Spacer(Modifier.size(IconButtonDefaults.largeContainerSize()))
                }

                if (!state.isLeftHandedModeEnabled) {
                    Spacer(modifier = Modifier.size(0.dp))
                    dialButton()
                }

                    FilledTonalIconButton(
                        onClick = { onEvent(ContactsEvent.SelectPreviousContact) },
                        modifier = Modifier.size(IconButtonDefaults.mediumContainerSize())
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.page_indicator_24px),
                            contentDescription = "上一个"
                        )
                    }

                    FilledTonalIconButton(
                        onClick = { onEvent(ContactsEvent.SelectNextContact) },
                        modifier = Modifier.size(IconButtonDefaults.mediumContainerSize())
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.page_indicator_24px),
                            contentDescription = "下一个",
                            modifier = Modifier.rotate(180f)
                        )
                    }

                if (state.isLeftHandedModeEnabled) {
                    Spacer(modifier = Modifier.size(0.dp))
                    dialButton()
                }
            }
        }
    ) { padding ->
        if (state.permissionGranted) {
            val listState = rememberLazyListState()
            val density = LocalDensity.current

            LaunchedEffect(state.selectedIndex) {
                if (state.selectedIndex != -1) {
                    val offset = if (state.selectedIndex > 0) {
                        with(density) { -(56.dp.toPx() / 3).toInt() }
                    } else {
                        0
                    }
                    listState.animateScrollToItem(
                        index = state.selectedIndex,
                        scrollOffset = offset
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                state = listState,
                verticalArrangement = Arrangement.Top
            ) {
                itemsIndexed(state.contacts, key = { _, contact -> contact.id }) { index, contact ->
                    val isSelected = index == state.selectedIndex
                    val containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        Color.Transparent
                    }
                    val headlineStyle = if (isSelected) {
                        MaterialTheme.typography.headlineLarge
                    } else {
                        MaterialTheme.typography.bodyLarge
                    }

                    ListItem(
                        headlineContent = { Text(contact.name, style = headlineStyle) },
                        leadingContent = {
                            if (contact.photoUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(contact.photoUri),
                                    contentDescription = contact.name,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        },
                        modifier = Modifier.clickable { onEvent(ContactsEvent.SelectContact(contact)) },
                        colors = ListItemDefaults.colors(containerColor = containerColor)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = { launcher.launch(arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE)) }) {
                    Text("请求权限")
                }
            }
        }
    }
}

@CustomPreview
@Composable
fun ContactsScreenPreview() {
    val state = ContactsState(
        permissionGranted = true,
        contacts = listOf(
            Contact(1, "John Doe", "1234567890", null),
            Contact(2, "Jane Smith", "0987654321", null)

        ),
        selectedIndex = 0,
        isLeftHandedModeEnabled = true
    )
    WarmTonesTheme {
        ContactsScreen(
            state = state,
            onEvent = {},
            sideEffects = MutableSharedFlow(),
            onNavigateToSettings = {}
        )
    }
}
