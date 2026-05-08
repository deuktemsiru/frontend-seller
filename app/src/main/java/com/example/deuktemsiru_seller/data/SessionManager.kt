package com.example.deuktemsiru_seller.data

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("seller_session", Context.MODE_PRIVATE)

    var sellerId: Long
        get() = prefs.getLong("sellerId", -1L)
        set(value) { prefs.edit().putLong("sellerId", value).apply() }

    var storeName: String
        get() = prefs.getString("storeName", "") ?: ""
        set(value) { prefs.edit().putString("storeName", value).apply() }

    var token: String
        get() = prefs.getString("token", "") ?: ""
        set(value) { prefs.edit().putString("token", value).apply() }

    var email: String
        get() = prefs.getString("email", "") ?: ""
        set(value) { prefs.edit().putString("email", value).apply() }

    var isSampleAccount: Boolean
        get() = prefs.getBoolean("isSampleAccount", false)
        set(value) { prefs.edit().putBoolean("isSampleAccount", value).apply() }

    fun isLoggedIn() = sellerId > 0L && token.isNotBlank()

    fun clear() {
        prefs.edit().clear().apply()
    }
}
