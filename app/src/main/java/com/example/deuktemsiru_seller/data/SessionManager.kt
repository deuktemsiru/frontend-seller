package com.example.deuktemsiru_seller.data

import android.content.Context
import com.example.deuktemsiru_seller.network.RetrofitClient

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("seller_session", Context.MODE_PRIVATE)

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
        set(value) { prefs.edit().putString("refreshToken", value).apply() }

    fun isLoggedIn() = memberId > 0L && accessToken.isNotBlank()

    fun restoreToken() {
        RetrofitClient.accessToken = accessToken.takeIf { it.isNotBlank() }
    }

    fun clear() {
        prefs.edit().clear().apply()
        RetrofitClient.accessToken = null
    }
}
