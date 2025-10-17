package io.github.sanlorng.warmtones.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private val dialConfirmationKey = booleanPreferencesKey("dial_confirmation")
    private val pagerModeKey = booleanPreferencesKey("pager_mode")

    private val leftHandedModeKey = booleanPreferencesKey("left_handed_mode")

    val isDialConfirmationEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[dialConfirmationKey] ?: true
        }

    val isPagerModeEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[pagerModeKey] ?: false
        }

    val isLeftHandedModeEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[leftHandedModeKey] ?: false
        }

    suspend fun setDialConfirmation(isEnabled: Boolean) {
        context.dataStore.edit {
            it[dialConfirmationKey] = isEnabled
        }
    }

    suspend fun setPagerMode(isEnabled: Boolean) {
        context.dataStore.edit {
            it[pagerModeKey] = isEnabled
        }
    }

    suspend fun setLeftHandedMode(isEnabled: Boolean) {
        context.dataStore.edit {
            it[leftHandedModeKey] = isEnabled
        }
    }
}