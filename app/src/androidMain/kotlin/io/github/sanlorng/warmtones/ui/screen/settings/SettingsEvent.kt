package io.github.sanlorng.warmtones.ui.screen.settings

sealed class SettingsEvent {
    data class ToggleDialConfirmation(val isEnabled: Boolean) : SettingsEvent()
    data class TogglePagerMode(val isEnabled: Boolean) : SettingsEvent()
    data class ToggleLeftHandedMode(val isEnabled: Boolean) : SettingsEvent()
}