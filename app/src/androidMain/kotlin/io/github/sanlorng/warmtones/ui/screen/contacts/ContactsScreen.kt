package io.github.sanlorng.warmtones.ui.screen.contacts

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import io.github.sanlorng.warmtones.R
import io.github.sanlorng.warmtones.ui.components.CustomPreview
import io.github.sanlorng.warmtones.ui.components.FilledIconButton
import io.github.sanlorng.warmtones.ui.components.FilledTonalIconButton
import io.github.sanlorng.warmtones.ui.components.IconButton
import io.github.sanlorng.warmtones.ui.components.PageScaffold
import io.github.sanlorng.warmtones.ui.theme.WarmTonesTheme
import kotlinx.coroutines.flow.MutableSharedFlow

@Composable
fun ContactsScreen(
    state: ContactsState,
    onEvent: (ContactsEvent) -> Unit,
    sideEffects: kotlinx.coroutines.flow.SharedFlow<ContactsViewModel.SideEffect>,
    onNavigateToSettings: () -> Unit
) {

    collectSideEffect(sideEffects)

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
        collapsedAppbar = true,
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
                val hapticFeedback = LocalHapticFeedback.current
                val dialButton = @Composable {
                    DialButton(
                        enabled = state.selectedIndex != -1,
                        confirmPending = state.isDialConfirmationPending,
                        oncClick = { onEvent(ContactsEvent.DialContact) }
                    )
                }

                if (!state.isLeftHandedModeEnabled) {
                    dialButton()
                    Spacer(modifier = Modifier.size(0.dp))
                }

                FilledTonalIconButton(
                    enabled = state.contacts.isNotEmpty(),
                    onClick = {
                        onEvent(ContactsEvent.SelectPreviousContact)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                    },
                    modifier = Modifier.size(IconButtonDefaults.mediumContainerSize())
                ) {
                    Icon(
                        painter = painterResource(R.drawable.page_indicator_24px),
                        contentDescription = "上一个"
                    )
                }

                FilledTonalIconButton(
                    enabled = state.contacts.isNotEmpty(),
                    onClick = {
                        onEvent(ContactsEvent.SelectNextContact)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                    },
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

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            state.contacts.getOrNull(state.selectedIndex)?.let { selected ->

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    ContactAvatar(
                        contact = selected,
                        modifier = Modifier
                            .size(108.dp)
                    )
                }
            }
            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .weight(1f),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(4.dp)
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
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.large)
                            .clickable { onEvent(ContactsEvent.SelectContact(contact)) },
                        colors = ListItemDefaults.colors(containerColor = containerColor),
                    )
                }
            }
        }

    }
}

@Composable
internal fun DialButton(
    enabled: Boolean,
    confirmPending: Boolean,
    oncClick: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    FilledIconButton(
        onClick = {
            oncClick()
            hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
        },
        enabled = enabled,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = if (confirmPending) {
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
            contentDescription = "拨打电话"
        )
    }
}

@Composable
fun ContactAvatar(contact: Contact, modifier: Modifier = Modifier, textStyle: TextStyle = MaterialTheme.typography.displayLarge) {
    Box(modifier.clip(CircleShape)) {
        val painter = rememberAsyncImagePainter(model = contact.photoUri)
        Image(painter, contentDescription = contact.name, modifier = Modifier.fillMaxSize())
        if (painter.state.collectAsState().value !is AsyncImagePainter.State.Success) {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.fillMaxSize().aspectRatio(1f)
            ) {
                Text(
                    text = contact.name.take(1).uppercase(),
                    style = textStyle,
                    modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)
                )
            }
        }
    }
}

@Composable
internal fun collectSideEffect(sideEffects: kotlinx.coroutines.flow.SharedFlow<ContactsViewModel.SideEffect>) {
    val context = LocalContext.current
    LaunchedEffect(context) {
        sideEffects.collect {
            when (it) {
                is ContactsViewModel.SideEffect.Dial -> {
                    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${it.phoneNumber}"))
                    context.startActivity(intent)
                }
            }
        }
    }
}

/**
 * This PreviewParameterProvider provides different states for the ContactsScreen preview.
 * This allows us to test various UI scenarios, such as:
 * - Right-handed mode (default)
 * - Left-handed mode
 * - Dial confirmation pending
 * - No contacts available
 * - Permissions not granted
 */
private class ContactsScreenPreviewProvider : PreviewParameterProvider<ContactsState> {
    private val sampleContacts = listOf(
        Contact(1, "John Doe", "1234567890", null),
        Contact(2, "Jane Smith", "0987654321", "https://i.pravatar.cc/150?img=1"),
        Contact(3, "张三", "13800138000", null)
    )

    override val values = sequenceOf(
        // Default state, right-handed
        ContactsState(
            contacts = sampleContacts,
            selectedIndex = 0,
            isLeftHandedModeEnabled = false,
            isDialConfirmationPending = false
        ),
        // Left-handed mode
        ContactsState(
            contacts = sampleContacts,
            selectedIndex = 1,
            isLeftHandedModeEnabled = true,
            isDialConfirmationPending = false
        ),
        // No contacts
        ContactsState(
            contacts = emptyList(),
            selectedIndex = -1
        ),
        // Dial confirmation pending
        ContactsState(
            contacts = sampleContacts,
            selectedIndex = 0,
            isDialConfirmationPending = true
        )
    )
}

@CustomPreview
@Composable
fun ContactsScreenPreview(
    @PreviewParameter(ContactsScreenPreviewProvider::class) state: ContactsState
) {
    WarmTonesTheme {
        ContactsScreen(
            state = state,
            onEvent = {},
            sideEffects = MutableSharedFlow(),
            onNavigateToSettings = {}
        )
    }
}
