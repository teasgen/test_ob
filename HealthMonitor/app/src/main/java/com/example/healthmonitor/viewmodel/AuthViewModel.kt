package com.example.healthmonitor.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmonitor.data.auth.AuthManager
import com.example.healthmonitor.data.network.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authManager = AuthManager.getInstance()
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String?, phone: String?, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = NetworkModule.api.login(
                    AuthRequest(
                        patient_email = email,
                        patient_phone_number = phone,
                        patient_password = password
                    )
                )
                if (response.isSuccessful && response.body()?.status == "success") {
                    val body = response.body()!!
                    authManager.saveSession(
                        token = body.token,
                        patientId = body.patient_id,
                        inviteCode = body.invite_code
                    )
                    _authState.value = AuthState.Success
                } else {
                    val error = response.errorBody()?.string() ?: "Ошибка входа"
                    _authState.value = AuthState.Error(error)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Ошибка сети: ${e.message}")
            }
        }
    }

    fun register(
        firstName: String,
        secondName: String,
        thirdName: String,
        email: String,
        phone: String,
        password: String,
        dateOfBirth: String
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val requestData = mapOf(
                    "patient_first_name" to firstName,
                    "patient_second_name" to secondName,
                    "patient_third_name" to thirdName,
                    "patient_email" to email,
                    "patient_phone_number" to phone,
                    "patient_password" to password,
                    "patient_date_of_birth" to dateOfBirth
                )

                val response = NetworkModule.api.registerPatient(requestData)

                if (response.isSuccessful && response.body()?.status == "success") {
                    val body = response.body()!!
                    authManager.saveSession(
                        token = body.token,
                        patientId = body.patient_id,
                        inviteCode = body.invite_code
                    )
                    _authState.value = AuthState.Success
                } else {
                    val error = response.errorBody()?.string() ?: "Ошибка регистрации"
                    _authState.value = AuthState.Error(error)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Ошибка сети: ${e.message}")
            }
        }
    }

    fun logout() {
        val mainVm = MainViewModel(getApplication())
        mainVm.clearUserData()

        authManager.logout()
        _authState.value = AuthState.Idle
    }

    fun isLoggedIn(): Boolean = authManager.isLoggedIn
}