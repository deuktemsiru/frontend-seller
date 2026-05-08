package com.example.deuktemsiru_seller.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.MainActivity
import com.example.deuktemsiru_seller.data.SessionManager
import com.example.deuktemsiru_seller.databinding.ActivityRegisterBinding
import com.example.deuktemsiru_seller.network.RegisterRequest
import com.example.deuktemsiru_seller.network.RetrofitClient
import com.example.deuktemsiru_seller.network.SampleData
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var businessVerified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.btnVerify.setOnClickListener { verifyBusiness() }
        binding.btnRegister.setOnClickListener { register() }
        binding.btnGoLogin.setOnClickListener { finish() }
    }

    private fun verifyBusiness() {
        val number = binding.etBusinessNumber.text?.toString()?.trim().orEmpty()
        if (number.length != 10) {
            binding.etBusinessNumber.error = "사업자번호 10자리를 입력해주세요"
            return
        }

        binding.btnVerify.isEnabled = false
        lifecycleScope.launch {
            try {
                val result = RetrofitClient.api.verifyBusiness(number)
                if (result.verified) {
                    businessVerified = true
                    binding.tvVerifyResult.text = "✓ ${result.businessName ?: "인증 완료"}"
                    binding.tvVerifyResult.setTextColor(getColor(android.R.color.holo_green_dark))
                    binding.tvVerifyResult.visibility = android.view.View.VISIBLE
                    binding.btnRegister.isEnabled = true
                } else {
                    binding.tvVerifyResult.text = "유효하지 않은 사업자번호예요"
                    binding.tvVerifyResult.setTextColor(getColor(android.R.color.holo_red_dark))
                    binding.tvVerifyResult.visibility = android.view.View.VISIBLE
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "인증 서버에 연결할 수 없어요", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnVerify.isEnabled = true
            }
        }
    }

    private fun register() {
        val storeName = binding.etStoreName.text?.toString()?.trim().orEmpty()
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()
        val businessNumber = binding.etBusinessNumber.text?.toString()?.trim().orEmpty()

        if (storeName.isBlank()) { binding.etStoreName.error = "상호명을 입력해주세요"; return }
        if (email.isBlank()) { binding.etEmail.error = "이메일을 입력해주세요"; return }
        if (password.length < 6) { binding.etPassword.error = "비밀번호 6자리 이상"; return }
        if (!businessVerified) { Toast.makeText(this, "사업자번호 인증을 완료해주세요", Toast.LENGTH_SHORT).show(); return }

        if (SampleData.findByCredentials(email, "dummy") != null) {
            Toast.makeText(this, "이미 등록된 이메일이에요", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "가입 중..."

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.register(
                    RegisterRequest(email, password, storeName, businessNumber)
                )
                val session = SessionManager(this@RegisterActivity)
                session.sellerId = response.userId
                session.storeName = response.storeName
                session.token = response.token
                session.isSampleAccount = false
                RetrofitClient.authToken = response.token
                startActivity(Intent(this@RegisterActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "가입에 실패했어요. 서버 상태를 확인해주세요.", Toast.LENGTH_LONG).show()
                binding.btnRegister.isEnabled = true
                binding.btnRegister.text = "가입하기"
            }
        }
    }
}
