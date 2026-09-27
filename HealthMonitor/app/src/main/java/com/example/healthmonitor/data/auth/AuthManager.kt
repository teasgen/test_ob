package com.example.healthmonitor.data.auth

import android.content.Context
import android.content.SharedPreferences

class AuthManager private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString("auth_token", null)
        set(value) = prefs.edit().putString("auth_token", value).apply()

    var patientId: Int?
        get() = prefs.getInt("patient_id", -1).takeIf { it != -1 }
        set(value) = prefs.edit().putInt("patient_id", value ?: -1).apply()

    var inviteCode: String?
        get() = prefs.getString("invite_code", null)
        set(value) = prefs.edit().putString("invite_code", value).apply()

    val isLoggedIn: Boolean
        get() = !token.isNullOrEmpty()

    fun saveSession(token: String, patientId: Int, inviteCode: String?) {
        this.token = token
        this.patientId = patientId
        this.inviteCode = inviteCode
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    fun getAuthHeader(): String? = token?.let { "Token $it" }

    companion object {
        @Volatile private var instance: AuthManager? = null

        fun init(context: Context) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = AuthManager(context)
                    }
                }
            }
        }

        fun getInstance(): AuthManager {
            return instance ?: throw IllegalStateException(
                "AuthManager not initialized. Call AuthManager.init() in Application.onCreate()"
            )
        }
    }
}