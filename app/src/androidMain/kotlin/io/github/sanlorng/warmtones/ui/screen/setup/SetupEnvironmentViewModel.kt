package io.github.sanlorng.warmtones.ui.screen.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.sanlorng.warmtones.data.PermissionsRepository
import io.github.sanlorng.warmtones.data.TtsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class SetupEnvironmentViewModel(
    permissionsRepository: PermissionsRepository,
    ttsRepository: TtsRepository
): ViewModel() {
    val state = permissionsRepository.permissionsGranted
        .combine(ttsRepository.isTtsAvailable) { permissionsGranted, isTtsInstalled ->
            SetupEnvironmentState(permissionsGranted, isTtsInstalled)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(),  SetupEnvironmentState(permissionsRepository.permissionsGranted.value, ttsRepository.isTtsAvailable.value))

}