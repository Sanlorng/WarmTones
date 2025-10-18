package io.github.sanlorng.warmtones.ui.screen

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.github.sanlorng.warmtones.Route
import io.github.sanlorng.warmtones.data.SettingsRepository
import io.github.sanlorng.warmtones.ui.screen.contacts.ContactsScreen
import io.github.sanlorng.warmtones.ui.screen.contacts.ContactsViewModel
import io.github.sanlorng.warmtones.ui.screen.pager.ContactsPagerScreen
import io.github.sanlorng.warmtones.ui.screen.pager.ContactsPagerViewModel
import io.github.sanlorng.warmtones.ui.screen.settings.SettingsScreen
import io.github.sanlorng.warmtones.ui.screen.settings.SettingsViewModel
import io.github.sanlorng.warmtones.ui.theme.WarmTonesTheme
import kotlinx.coroutines.flow.first

@Composable
fun WarmTonesApp(application: Application) {
    var startDestination by remember { mutableStateOf<Route?>(null) }

    LaunchedEffect(Unit) {
        val settingsRepository = SettingsRepository(application)
        val isPagerMode = settingsRepository.isPagerModeEnabled.first()
        startDestination = if (isPagerMode) Route.Pager else Route.Contacts
        settingsRepository.isPagerModeEnabled.collect {
            startDestination = if (it) Route.Pager else Route.Contacts
        }
    }
    val currentStartDestination = startDestination
    if (currentStartDestination != null) {
        WarmTonesTheme {
            Surface {
                val backStack = rememberNavBackStack(currentStartDestination)
                LaunchedEffect(currentStartDestination) {
                    backStack[0] = currentStartDestination
                }
                val currentContext = LocalContext.current
                val applicationContext = currentContext.applicationContext as Application
                val settingsRepository = remember { SettingsRepository(applicationContext) }



                NavDisplay(
                    backStack = backStack,
                    entryProvider = entryProvider {
                        entry<Route.Contacts> {
                            val contactsViewModel: ContactsViewModel = viewModel {
                                ContactsViewModel(applicationContext, settingsRepository)
                            }
                            val state by contactsViewModel.state.collectAsState()
                            ContactsScreen(
                                state = state,
                                onEvent = contactsViewModel::onEvent,
                                sideEffects = contactsViewModel.sideEffects,
                                onNavigateToSettings = { backStack.add(Route.Settings) }
                            )
                        }
                        entry<Route.Settings> {
                            val settingsViewModel: SettingsViewModel = viewModel {
                                SettingsViewModel(settingsRepository)
                            }
                            val state by settingsViewModel.state.collectAsState()
                            SettingsScreen(
                                state = state,
                                onEvent = settingsViewModel::onEvent,
                                onBack = { backStack.removeLastOrNull() }
                            )
                        }
                        entry<Route.Pager> {
                            val contactsPagerViewModel: ContactsPagerViewModel = viewModel {
                                ContactsPagerViewModel(applicationContext, settingsRepository)
                            }
                            val state by contactsPagerViewModel.pagerState.collectAsState()
                            ContactsPagerScreen(
                                state = state,
                                onEvent = contactsPagerViewModel::onPagerEvent,
                                onNavigateToSettings = { backStack.add(Route.Settings) },
                                sideEffects = contactsPagerViewModel.sideEffects
                            )
                        }
                    }
                )
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize())
    }
}