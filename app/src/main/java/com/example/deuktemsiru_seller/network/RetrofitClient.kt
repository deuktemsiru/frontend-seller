package com.example.deuktemsiru_seller.network

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.Authenticator
import com.example.deuktemsiru_seller.BuildConfig
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.net.URL

object RetrofitClient {

    private const val EMULATOR_BASE_URL = "http://10.0.2.2:8080/"
    val BASE_URL: String = BuildConfig.BASE_URL.ifBlank { EMULATOR_BASE_URL }

    /** SessionManager에서 accessToken을 설정하면 모든 요청에 자동으로 첨부됩니다. */
    var accessToken: String? = null
    var refreshToken: String? = null
    var onTokenRefreshed: ((String) -> Unit)? = null

    /** 목 로그인 세션 여부 — true이면 토큰 갱신 시도를 건너뜁니다. */
    var isMockSession: Boolean = false

    init {
        if (!BuildConfig.DEBUG && BASE_URL.contains("10.0.2.2")) {
            error("릴리스 빌드에서 에뮬레이터 URL을 사용할 수 없습니다. local.properties에 BACKEND_BASE_URL을 설정하세요.")
        }
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                accessToken?.takeIf { it.isNotBlank() }?.let {
                    requestBuilder.addHeader("Authorization", "Bearer $it")
                }
                chain.proceed(requestBuilder.build())
            }
            .authenticator(TokenRefreshAuthenticator())
            .build()
    }

    private val realApi: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val api: ApiService
        get() = if (isMockSession) MockApiService else realApi

    private class TokenRefreshAuthenticator : Authenticator {
        override fun authenticate(route: Route?, response: Response): Request? {
            if (response.request.header("Authorization").isNullOrBlank()) return null
            if (responseCount(response) >= 2) return null

            if (isMockSession) {
                Log.w("RetrofitClient", "Skipping token refresh for mock session")
                return null
            }
            val savedRefreshToken = refreshToken?.takeIf { it.isNotBlank() } ?: return null
            val newAccessToken = synchronized(this) {
                val currentRequestToken = response.request.header("Authorization")?.removePrefix("Bearer ")
                if (!accessToken.isNullOrBlank() && accessToken != currentRequestToken) {
                    accessToken
                } else {
                    refreshTokenSync(savedRefreshToken)?.also {
                        accessToken = it
                        onTokenRefreshed?.invoke(it)
                    }
                }
            } ?: return null

            return response.request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        }

        private fun refreshTokenSync(refreshToken: String): String? {
            return try {
                val url = URL("${BASE_URL}api/v1/auth/refresh")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 10_000
                    readTimeout = 10_000
                }
                val body = Gson().toJson(mapOf("refreshToken" to refreshToken))
                conn.outputStream.use { it.write(body.toByteArray()) }
                if (conn.responseCode != HttpURLConnection.HTTP_OK) return null
                val json = conn.inputStream.bufferedReader().readText()
                Gson().fromJson(json, Map::class.java)["data"]
                    ?.let { (it as? Map<*, *>)?.get("accessToken") as? String }
            } catch (_: Exception) { null }
        }

        private fun responseCount(response: Response): Int {
            var count = 1
            var prior = response.priorResponse
            while (prior != null) {
                count++
                prior = prior.priorResponse
            }
            return count
        }
    }
}
