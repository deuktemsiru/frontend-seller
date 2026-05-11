package com.example.deuktemsiru_seller.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.MainActivity
import com.example.deuktemsiru_seller.data.SessionManager
import com.example.deuktemsiru_seller.databinding.ActivityLoginBinding
import com.example.deuktemsiru_seller.network.KakaoLoginRequest
import com.example.deuktemsiru_seller.network.RetrofitClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        if (session.isLoggedIn()) {
            session.restoreToken()
            navigateToMain()
            return
        }

        binding.btnKakaoLogin.setOnClickListener { startKakaoLogin() }
    }

    private fun startKakaoLogin() {
        setLoading(true)

        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            when {
                error != null -> handleKakaoError(error)
                token != null -> loginToBackend(token.accessToken)
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    if (error.isUserCancelled()) setLoading(false)
                    else UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                } else if (token != null) {
                    loginToBackend(token.accessToken)
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    private fun handleKakaoError(error: Throwable) {
        if (!error.isUserCancelled()) {
            Toast.makeText(this, "카카오 로그인 실패: ${error.message}", Toast.LENGTH_SHORT).show()
        }
        setLoading(false)
    }

    private fun loginToBackend(kakaoAccessToken: String) {
        lifecycleScope.launch {
            runCatching {
                RetrofitClient.api.kakaoLogin(
                    KakaoLoginRequest(kakaoAccessToken = kakaoAccessToken, role = "SELLER")
                )
            }.onSuccess { response ->
                val loginData = response.data
                if (loginData != null) {
                    session.memberId = loginData.member.memberId
                    session.nickname = loginData.member.nickname
                    session.accessToken = loginData.accessToken
                    session.refreshToken = loginData.refreshToken
                    navigateToMain()
                } else {
                    showError("로그인 응답이 올바르지 않습니다.")
                }
            }.onFailure { e ->
                showError("서버 연결에 실패했습니다: ${e.message}")
            }
            setLoading(false)
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun Throwable.isUserCancelled() =
        this is ClientError && reason == ClientErrorCause.Cancelled

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnKakaoLogin.isEnabled = !loading
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
