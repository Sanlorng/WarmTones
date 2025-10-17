package io.github.sanlorng.warmtones.ui.screen.pager

import io.github.sanlorng.warmtones.ui.screen.contacts.Contact

sealed class ContactsPagerEvent {
    data class SpeakContact(val contact: Contact) : ContactsPagerEvent()
    data class DialContact(val contact: Contact) : ContactsPagerEvent()
}