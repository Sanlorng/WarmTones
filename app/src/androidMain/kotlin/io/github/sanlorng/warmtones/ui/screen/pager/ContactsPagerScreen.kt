package io.github.sanlorng.warmtones.ui.screen.pager

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.sanlorng.warmtones.R
import io.github.sanlorng.warmtones.ui.components.CustomPreview
import io.github.sanlorng.warmtones.ui.components.IconButton
import io.github.sanlorng.warmtones.ui.components.PageScaffold
import io.github.sanlorng.warmtones.ui.screen.contacts.Contact
import io.github.sanlorng.warmtones.ui.screen.contacts.ContactAvatar
import io.github.sanlorng.warmtones.ui.screen.contacts.ContactsViewModel
import io.github.sanlorng.warmtones.ui.screen.contacts.DialButton
import io.github.sanlorng.warmtones.ui.screen.contacts.collectSideEffect
import io.github.sanlorng.warmtones.ui.theme.WarmTonesTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactsPagerScreen(
    state: ContactsPagerState,
    sideEffects: kotlinx.coroutines.flow.SharedFlow<ContactsViewModel.SideEffect>,
    onNavigateToSettings: () -> Unit,
    onEvent: (ContactsPagerEvent) -> Unit
) {
    PageScaffold(
        title = "联系人",
        actions = {
            IconButton(
                content = {
                    Icon(
                        painter = painterResource(R.drawable.settings_24px),
                        contentDescription = "设置"
                    )
                },
                onClick = onNavigateToSettings
            )
        },
        collapsedAppbar = true
    ) { padding ->
        if (state.contacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("沒有联系人")
            }
            return@PageScaffold
        }

        val pagerState = rememberPagerState { state.contacts.size }
        val hapticFeedback = LocalHapticFeedback.current
        collectSideEffect(sideEffects)

        LaunchedEffect(pagerState, hapticFeedback) {
            snapshotFlow { pagerState.settledPage }.collect {
                val contact = state.contacts[it]
                onEvent(ContactsPagerEvent.SpeakContact(contact))
                hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            VerticalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
            ) { page ->
                val contact = state.contacts[page]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onEvent(ContactsPagerEvent.SpeakContact(contact)) }
                        .padding(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            ContactAvatar(
                                contact = contact,
                                modifier = Modifier.size(128.dp)
                            )
                            Text(
                                text = contact.name,
                                style = MaterialTheme.typography.displayLarge,
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        horizontalArrangement = if (state.isLeftHandedModeEnabled) Arrangement.End else Arrangement.Start,
                    ) {
                        DialButton(
                            enabled = true,
                            confirmPending = state.isDialConfirmationPending,
                            oncClick = { onEvent(ContactsPagerEvent.DialContact(contact)) }
                        )
                    }
                }
            }
        }
    }
}

@CustomPreview
@Composable
fun ContactsPagerScreenPreview() {
    WarmTonesTheme {
        val sampleContacts = listOf(
            Contact(1L, "张三", "13800138000", null),
            Contact(2L, "李四", "13900139000", null),
            Contact(3L, "Wang Wu", "13700137000", null)
        )
        val sampleState = ContactsPagerState(
            contacts = sampleContacts,
            isLeftHandedModeEnabled = true
        )
        ContactsPagerScreen(
            state = sampleState,
            onEvent = {},
            onNavigateToSettings = {},
            sideEffects = kotlinx.coroutines.flow.MutableSharedFlow()
        )
    }
}
