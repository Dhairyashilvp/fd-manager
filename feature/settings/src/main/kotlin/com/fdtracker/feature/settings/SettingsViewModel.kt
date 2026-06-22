package com.fdtracker.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fdtracker.core.domain.model.UserProfile
import com.fdtracker.core.domain.repository.SmtpConfig
import com.fdtracker.core.domain.repository.SmtpRepository
import com.fdtracker.core.domain.repository.UserPrefsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val dateOfBirth: LocalDate? = null,
    val panNumber: String = "",
    val pushReminders: Boolean = true,
    val emailReminders: Boolean = false,
    val appLock: Boolean = false,
    val darkMode: Boolean? = null,
    val smtpHost: String = "",
    val smtpPort: String = "587",
    val smtpUsername: String = "",
    val smtpPassword: String = "",
    val smtpFromAddress: String = "",
    val smtpToAddress: String = "",
    val smtpUseTls: Boolean = true,
    val showSmtpSection: Boolean = false,
    val savedMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPrefsRepository: UserPrefsRepository,
    private val smtpRepository: SmtpRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                userPrefsRepository.observeUserProfile(),
                userPrefsRepository.observePushRemindersEnabled(),
                userPrefsRepository.observeEmailRemindersEnabled(),
                userPrefsRepository.observeAppLockEnabled(),
                userPrefsRepository.observeDarkMode()
            ) { profile, push, email, appLock, dark ->
                SettingsUiState(
                    isLoading = false,
                    userName = profile?.fullName ?: "",
                    dateOfBirth = profile?.dateOfBirth,
                    panNumber = profile?.panNumber ?: "",
                    pushReminders = push,
                    emailReminders = email,
                    appLock = appLock,
                    darkMode = dark
                )
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }.collect { state ->
                _uiState.update { state }
            }
        }

        viewModelScope.launch {
            smtpRepository.observeConfig()
                .catch { }
                .collect { config ->
                    if (config != null) {
                        _uiState.update {
                            it.copy(
                                smtpHost = config.host,
                                smtpPort = config.port.toString(),
                                smtpUsername = config.username,
                                smtpPassword = config.password,
                                smtpFromAddress = config.fromAddress,
                                smtpToAddress = config.toAddress,
                                smtpUseTls = config.useTls
                            )
                        }
                    }
                }
        }
    }

    fun updateField(field: String, value: Any) {
        _uiState.update { state ->
            when (field) {
                "userName" -> state.copy(userName = value as String)
                "panNumber" -> state.copy(panNumber = value as String)
                "smtpHost" -> state.copy(smtpHost = value as String)
                "smtpPort" -> state.copy(smtpPort = value as String)
                "smtpUsername" -> state.copy(smtpUsername = value as String)
                "smtpPassword" -> state.copy(smtpPassword = value as String)
                "smtpFromAddress" -> state.copy(smtpFromAddress = value as String)
                "smtpToAddress" -> state.copy(smtpToAddress = value as String)
                else -> state
            }
        }
    }

    fun updateDateOfBirth(date: LocalDate) {
        _uiState.update { it.copy(dateOfBirth = date) }
    }

    fun togglePushReminders(enabled: Boolean) {
        viewModelScope.launch {
            userPrefsRepository.setPushRemindersEnabled(enabled)
            _uiState.update { it.copy(pushReminders = enabled) }
        }
    }

    fun toggleEmailReminders(enabled: Boolean) {
        viewModelScope.launch {
            userPrefsRepository.setEmailRemindersEnabled(enabled)
            _uiState.update { it.copy(emailReminders = enabled) }
        }
    }

    fun toggleAppLock(enabled: Boolean) {
        viewModelScope.launch {
            userPrefsRepository.setAppLockEnabled(enabled)
            _uiState.update { it.copy(appLock = enabled) }
        }
    }

    fun toggleDarkMode(enabled: Boolean?) {
        viewModelScope.launch {
            userPrefsRepository.setDarkMode(enabled)
            _uiState.update { it.copy(darkMode = enabled) }
        }
    }

    fun toggleSmtpTls(enabled: Boolean) {
        _uiState.update { it.copy(smtpUseTls = enabled) }
    }

    fun toggleSmtpSection() {
        _uiState.update { it.copy(showSmtpSection = !it.showSmtpSection) }
    }

    fun saveProfile() {
        viewModelScope.launch {
            val state = _uiState.value
            val dob = state.dateOfBirth ?: return@launch
            val profile = UserProfile(
                fullName = state.userName,
                dateOfBirth = dob,
                panNumber = state.panNumber.ifBlank { null }
            )
            userPrefsRepository.saveUserProfile(profile)
            _uiState.update { it.copy(savedMessage = "Profile saved") }
        }
    }

    fun saveSmtpConfig() {
        viewModelScope.launch {
            val state = _uiState.value
            try {
                val config = SmtpConfig(
                    host = state.smtpHost,
                    port = state.smtpPort.toIntOrNull() ?: 587,
                    username = state.smtpUsername,
                    password = state.smtpPassword,
                    fromAddress = state.smtpFromAddress,
                    toAddress = state.smtpToAddress,
                    useTls = state.smtpUseTls
                )
                smtpRepository.saveConfig(config)
                _uiState.update { it.copy(savedMessage = "SMTP config saved") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(savedMessage = null, error = null) }
    }
}
