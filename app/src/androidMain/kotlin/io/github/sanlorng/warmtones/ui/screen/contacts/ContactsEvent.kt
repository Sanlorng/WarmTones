package io.github.sanlorng.warmtones.ui.screen.contacts

sealed class ContactsEvent {
    data class PermissionResult(val isGranted: Boolean) : ContactsEvent()
    object LoadContacts : ContactsEvent()
    data class SpeakContact(val contact: Contact) : ContactsEvent()
    object SelectNextContact : ContactsEvent()
    object SelectPreviousContact : ContactsEvent()
    data class SelectContact(val contact: Contact) : ContactsEvent()
    object DialContact : ContactsEvent()
    object DismissInstallTtsDataDialog : ContactsEvent()
}