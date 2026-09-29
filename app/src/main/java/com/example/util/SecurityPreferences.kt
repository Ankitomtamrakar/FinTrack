package com.example.util

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager

class SecurityPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("fin_track_security_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_APP_LOCK_ENABLED = "key_app_lock_enabled"
        const val KEY_LOCK_TYPE = "key_lock_type" // "biometric" or "pin"
        const val KEY_USER_PIN = "key_user_pin"
        
        const val LOCK_TYPE_BIOMETRIC = "biometric"
        const val LOCK_TYPE_PIN = "pin"

        @Volatile
        private var INSTANCE: SecurityPreferences? = null

        fun getInstance(context: Context): SecurityPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SecurityPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    var isAppLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_APP_LOCK_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, value).apply()

    var lockType: String
        get() = prefs.getString(KEY_LOCK_TYPE, LOCK_TYPE_BIOMETRIC) ?: LOCK_TYPE_BIOMETRIC
        set(value) = prefs.edit().putString(KEY_LOCK_TYPE, value).apply()

    var userPin: String?
        get() = prefs.getString(KEY_USER_PIN, null)
        set(value) = prefs.edit().putString(KEY_USER_PIN, value).apply()

    fun hasPin(): Boolean = !userPin.isNullOrBlank()

    fun verifyPin(input: String): Boolean {
        val saved = userPin ?: return false
        return saved == input
    }

    fun isBiometricOrDeviceCredentialAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or 
                             BiometricManager.Authenticators.DEVICE_CREDENTIAL
        val canAuth = biometricManager.canAuthenticate(authenticators)
        return canAuth == BiometricManager.BIOMETRIC_SUCCESS
    }
}
