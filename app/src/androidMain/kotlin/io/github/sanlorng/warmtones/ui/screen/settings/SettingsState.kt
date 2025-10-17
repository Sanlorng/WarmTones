package io.github.sanlorng.warmtones.ui.screen.settings

data class SettingsState(
    val isDialConfirmationEnabled: Boolean = true,
    val isPagerModeEnabled: Boolean = false,
    val isLeftHandedModeEnabled: Boolean = false
)