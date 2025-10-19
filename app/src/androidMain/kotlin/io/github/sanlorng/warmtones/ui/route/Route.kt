package io.github.sanlorng.warmtones.ui.route

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {

    @Serializable
    object Contacts : Route

    @Serializable
    object Settings : Route

    @Serializable
    object Pager : Route

    @Serializable
    object Splash : Route

    @Serializable
    object SetupEnvironment : Route
}
