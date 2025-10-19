package io.github.sanlorng.warmtones.ui.screen.setup

data class SetupEnvironmentState(
    val isPermissionsGranted: Boolean = false,
    val isTtsAvailable: Boolean = false
)