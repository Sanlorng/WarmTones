package io.github.sanlorng.warmtones.ui.screen.setup

sealed class SetupEnvironmentEvent {

    data object OnPermissionRequested : SetupEnvironmentEvent()

    data object OnTtsInstallRequested: SetupEnvironmentEvent()
}