package com.swetabiswas.gamesnack.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swetabiswas.gamesnack.data.local.datastore.AppPreferenceState
import com.swetabiswas.gamesnack.data.local.datastore.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {

    val prefs: StateFlow<AppPreferenceState> = appPreferences.appPreferenceState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppPreferenceState())

    fun toggleDarkMode() {
        viewModelScope.launch {
            appPreferences.setDarkMode(!prefs.value.isDarkMode)
        }
    }

    fun toggleSound() {
        viewModelScope.launch {
            appPreferences.setSoundEnabled(!prefs.value.isSoundEnabled)
        }
    }
}
