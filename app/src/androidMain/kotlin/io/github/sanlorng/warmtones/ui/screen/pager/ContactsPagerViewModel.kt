package io.github.sanlorng.warmtones.ui.screen.pager

import android.app.Application
import androidx.lifecycle.viewModelScope
import io.github.sanlorng.warmtones.data.SettingsRepository
import io.github.sanlorng.warmtones.ui.screen.contacts.ContactsViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactsPagerViewModel(
    application: Application,
    settingsRepository: SettingsRepository
) : ContactsViewModel(application, settingsRepository) {

    val pagerState = state.map { ContactsPagerState(it.contacts) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ContactsPagerState()
    )

    fun onPagerEvent(event: ContactsPagerEvent) {
        when (event) {
            is ContactsPagerEvent.SpeakContact -> {
                viewModelScope.launch {
                    ttsHelper.speak(event.contact.name)
                }
            }
            is ContactsPagerEvent.DialContact -> {
                handleDialContact(event.contact)
            }
        }
    }
}