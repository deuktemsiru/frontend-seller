package com.example.deuktemsiru_seller.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.deuktemsiru_seller.network.RetrofitClient

class SessionManager(context: Context) {
    private val prefs = securePrefs(context, "seller_session")

    init {
        restoreToken()
        RetrofitClient.onTokenRefreshed = { newAccessToken ->
            prefs.edit().putString("accessToken", newAccessToken).apply()
            RetrofitClient.accessToken = newAccessToken.takeIf { it.isNotBlank() }
        }
    }

    var isMockSession: Boolean
        get() = prefs.getBoolean("isMockSession", false)
        set(value) {
            prefs.edit().putBoolean("isMockSession", value).apply()
            RetrofitClient.isMockSession = value
        }

    var memberId: Long
        get() = prefs.getLong("memberId", -1L)
        set(value) { prefs.edit().putLong("memberId", value).apply() }

    var nickname: String
        get() = prefs.getString("nickname", "") ?: ""
        set(value) { prefs.edit().putString("nickname", value).apply() }

    var accessToken: String
        get() = prefs.getString("accessToken", "") ?: ""
        set(value) {
            prefs.edit().putString("accessToken", value).apply()
            RetrofitClient.accessToken = value.takeIf { it.isNotBlank() }
        }

    var refreshToken: String
        get() = prefs.getString("refreshToken", "") ?: ""
        set(value) {
            prefs.edit().putString("refreshToken", value).apply()
            RetrofitClient.refreshToken = value.takeIf { it.isNotBlank() }
        }

    fun isLoggedIn() = memberId > 0L && accessToken.isNotBlank()

    fun restoreToken() {
        RetrofitClient.accessToken = accessToken.takeIf { it.isNotBlank() }
        RetrofitClient.refreshToken = refreshToken.takeIf { it.isNotBlank() }
        RetrofitClient.isMockSession = isMockSession
    }

    fun clear() {
        prefs.edit().clear().apply()
        RetrofitClient.accessToken = null
        RetrofitClient.refreshToken = null
        RetrofitClient.onTokenRefreshed = null
        RetrofitClient.isMockSession = false
    }
}

private fun securePrefs(context: Context, name: String): SharedPreferences {
    val appContext = context.applicationContext
    return runCatching {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            appContext,
            name,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }.getOrElse {
        appContext.getSharedPreferences(name, Context.MODE_PRIVATE)
    }
}
