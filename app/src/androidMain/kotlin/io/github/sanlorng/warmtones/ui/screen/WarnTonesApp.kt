package io.github.sanlorng.warmtones.ui.screen

import android.app.Application
import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SceneInfo
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import io.github.sanlorng.warmtones.R
import io.github.sanlorng.warmtones.data.PermissionsRepository
import io.github.sanlorng.warmtones.data.SettingsRepository
import io.github.sanlorng.warmtones.data.TtsRepository
import io.github.sanlorng.warmtones.ui.route.Route
import io.github.sanlorng.warmtones.ui.screen.contacts.ContactsScreen
import io.github.sanlorng.warmtones.ui.screen.contacts.ContactsViewModel
import io.github.sanlorng.warmtones.ui.screen.pager.ContactsPagerScreen
import io.github.sanlorng.warmtones.ui.screen.pager.ContactsPagerViewModel
import io.github.sanlorng.warmtones.ui.screen.settings.SettingsScreen
import io.github.sanlorng.warmtones.ui.screen.settings.SettingsViewModel
import io.github.sanlorng.warmtones.ui.screen.setup.SetupEnvironmentEvent
import io.github.sanlorng.warmtones.ui.screen.setup.SetupEnvironmentScreen
import io.github.sanlorng.warmtones.ui.screen.setup.SetupEnvironmentViewModel
import io.github.sanlorng.warmtones.ui.theme.WarmTonesTheme
import kotlinx.coroutines.flow.combine

@Composable
fun WarmTonesApp() {

    WarmTonesTheme {

        val currentContext = LocalContext.current
        val applicationContext = currentContext.applicationContext as Application
        val settingsRepository =
            remember(applicationContext) { SettingsRepository(applicationContext) }
        val permissionsRepository =
            remember(applicationContext) { PermissionsRepository(applicationContext) }
        val ttsRepository = remember(applicationContext, currentContext) {
            TtsRepository(applicationContext)
        }
        val isResume = remember { mutableStateOf(false) }
        LifecycleResumeEffect(permissionsRepository, ttsRepository) {
            permissionsRepository.updatePermissionsStatus()
            onPauseOrDispose {}
        }

        LaunchedEffect(isResume.value) {
            if (isResume.value) {
                ttsRepository.checkTtsAvailability()
            }
        }

        val mutex = remember { MutatorMutex() }

        Surface {
            val backStack = rememberNavBackStack(Route.Splash)
            LaunchedEffect(settingsRepository) {
                settingsRepository.isPagerModeEnabled.collect { isPageModeEnabled ->
                    mutex.mutate {
                        backStack[0] = if (isPageModeEnabled) {
                            Route.Pager
                        } else {
                            Route.Contacts
                        }
                    }
                }
            }
            LaunchedEffect(permissionsRepository, ttsRepository) {
                combine(
                    permissionsRepository.permissionsGranted,
                    ttsRepository.isTtsAvailable
                ) { permissions, tts ->
                    permissions and tts
                }.collect { pass ->
                    if (!pass) {
                        if (!backStack.contains(Route.SetupEnvironment)) {
                            backStack.add(Route.SetupEnvironment)
                        }
                    } else {
                        backStack.remove(Route.SetupEnvironment)
                    }
                }
            }
            NavDisplay(
                backStack = backStack,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                    rememberPredictiveBackNavEntryDecorator()
                ),
                entryProvider = entryProvider {
                    entry<Route.Contacts> {
                        val contactsViewModel: ContactsViewModel = viewModel {
                            ContactsViewModel(
                                applicationContext,
                                settingsRepository,
                                permissionsRepository,
                                ttsRepository
                            )
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
                            ContactsPagerViewModel(applicationContext, settingsRepository, permissionsRepository, ttsRepository)
                        }
                        val state by contactsPagerViewModel.pagerState.collectAsState()
                        ContactsPagerScreen(
                            state = state,
                            onEvent = contactsPagerViewModel::onPagerEvent,
                            onNavigateToSettings = { backStack.add(Route.Settings) },
                            sideEffects = contactsPagerViewModel.sideEffects
                        )
                    }

                    entry<Route.Splash> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.headlineLarge,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                    entry<Route.SetupEnvironment> {
                        val viewModel: SetupEnvironmentViewModel = viewModel {
                            SetupEnvironmentViewModel(permissionsRepository, ttsRepository)
                        }
                        val state by viewModel.state.collectAsState()
                        val launcher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.RequestMultiplePermissions(),
                            onResult = { permissionsRepository.updatePermissionsStatus() }
                        )
                        NavigationBackHandler(state = rememberNavigationEventState(NavigationEventInfo.None)) {}
                        SetupEnvironmentScreen(
                            state = state,
                            onEvent = {
                                when (it) {
                                    SetupEnvironmentEvent.OnTtsInstallRequested -> {
                                        val installIntent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
                                        currentContext.startActivity(installIntent)
                                    }
                                    SetupEnvironmentEvent.OnPermissionRequested -> {
                                        val permissions = permissionsRepository.getRequiredPermissions()
                                        launcher.launch(permissions)
                                    }
                                }
                            }
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun rememberPredictiveBackNavEntryDecorator(): NavEntryDecorator<Any> {
    val navDispatcher = LocalNavigationEventDispatcherOwner.current?.navigationEventDispatcher

    val transitionState = navDispatcher?.transitionState?.collectAsState()
    return remember(navDispatcher) {
        NavEntryDecorator { entry ->
            val isClip by remember(entry, transitionState) {
                derivedStateOf {
                    if (navDispatcher == null) return@derivedStateOf false
                    if (transitionState?.value !is NavigationEventTransitionState.InProgress) {
                        return@derivedStateOf false
                    }
                    val currentValue = navDispatcher.history.value.let { it.mergedHistory.getOrNull(it.currentIndex) } as? SceneInfo<*>

                    currentValue?.scene?.entries?.any { it.contentKey == entry.contentKey } ?: false
                }
            }
            val targetCornerSize = if (isClip) 28.dp else 0.dp
            val currentScrim = remember(entry, navDispatcher, transitionState) {
                derivedStateOf {
                    val currentTransitionState = transitionState?.value
                    when {
                        navDispatcher == null -> 0f
                        currentTransitionState !is NavigationEventTransitionState.InProgress -> 0f
                        else -> {
                            val previousScene = navDispatcher.history.value.let { it.mergedHistory.getOrNull(it.currentIndex - 1) as? SceneInfo<*> }
                            if (previousScene?.scene?.entries?.any { it.contentKey == entry.contentKey } == true) {
                                currentTransitionState.latestEvent.progress
                            } else {
                                0f
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier.clip(
                    RoundedCornerShape(
                        size = animateDpAsState(targetCornerSize).value
                    )
                ),
                propagateMinConstraints = true
            ) {
                entry.Content()
                if (currentScrim.value != 0f) {
                    Box(modifier = Modifier.fillMaxSize().background(
                        MaterialTheme.colorScheme.scrim.let { it.copy(it.alpha * (1- currentScrim.value)) }
                    ))
                }
            }
        }
    }
}