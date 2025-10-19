package io.github.sanlorng.warmtones.ui.screen.contacts

data class ContactsState(
    val contacts: List<Contact> = emptyList(),
    val selectedIndex: Int = -1,
    val isDialConfirmationPending: Boolean = false,
    val isLeftHandedModeEnabled: Boolean = false
)

data class Contact(
    val id: Long,
    val name: String,
    val phoneNumber: String,
    val photoUri: String?
)