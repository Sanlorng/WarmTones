package io.github.sanlorng.warmtones.ui.screen.pager

import io.github.sanlorng.warmtones.ui.screen.contacts.Contact

data class ContactsPagerState(
    val contacts: List<Contact> = emptyList(),
    val isLeftHandedModeEnabled: Boolean = false
)