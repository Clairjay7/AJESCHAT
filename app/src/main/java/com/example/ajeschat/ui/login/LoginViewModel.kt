package com.example.ajeschat.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ajeschat.AjesChatApp
import com.example.ajeschat.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository: AuthRepository = (application as AjesChatApp).authRepository

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateUsername(s: String) {
        _uiState.value = _uiState.value.copy(username = s, error = null)
    }

    fun updatePassword(s: String) {
        _uiState.value = _uiState.value.copy(password = s, error = null)
    }

    fun togglePasswordVisible() {
        _uiState.value = _uiState.value.copy(passwordVisible = !_uiState.value.passwordVisible)
    }

    fun login(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            when (val result = authRepository.login(_uiState.value.username, _uiState.value.password)) {
                is AuthRepository.LoginResult.Success -> {
                    _uiState.value = _uiState.value.copy(loading = false, error = null)
                    onSuccess()
                }
                is AuthRepository.LoginResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = result.message
                    )
                }
            }
        }
    }
}
