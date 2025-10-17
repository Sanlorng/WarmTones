package io.github.sanlorng.warmtones.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.sanlorng.warmtones.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    val state: StateFlow<SettingsState> = combine(
        settingsRepository.isDialConfirmationEnabled,
        settingsRepository.isPagerModeEnabled,
        settingsRepository.isLeftHandedModeEnabled,
    ) { isDialConfirmationEnabled, isPagerModeEnabled, isLeftHandedModeEnabled ->
        SettingsState(isDialConfirmationEnabled, isPagerModeEnabled, isLeftHandedModeEnabled)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsState()
    )

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.ToggleDialConfirmation -> {
                viewModelScope.launch {
                    settingsRepository.setDialConfirmation(event.isEnabled)
                }
            }
            is SettingsEvent.TogglePagerMode -> {
                viewModelScope.launch {
                    settingsRepository.setPagerMode(event.isEnabled)
                }
            }
            is SettingsEvent.ToggleLeftHandedMode -> {
                viewModelScope.launch {
                    settingsRepository.setLeftHandedMode(event.isEnabled)
                }
            }
        }
    }
}