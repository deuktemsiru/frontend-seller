package com.example.deuktemsiru_seller.ui.mypage

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deuktemsiru_seller.data.SessionManager
import com.example.deuktemsiru_seller.databinding.ActivityAccountInfoBinding

class AccountInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val session = SessionManager(this)

        binding.btnBack.setOnClickListener { finish() }

        binding.tvEmail.text = "카카오 소셜 계정"
        binding.tvSellerId.text = "#${session.memberId}"
        binding.tvStoreName.text = session.nickname.ifBlank { "—" }
        binding.tvBusinessNumber.text = "***-**-*****"

        binding.btnInstagram.setOnClickListener {
            Toast.makeText(this, "SNS 연동은 준비 중이에요", Toast.LENGTH_SHORT).show()
        }
    }
}
