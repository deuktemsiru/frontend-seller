package com.example.deuktemsiru_seller.ui.mypage

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.data.SessionManager
import com.example.deuktemsiru_seller.databinding.ActivityAccountInfoBinding
import com.example.deuktemsiru_seller.network.RetrofitClient
import kotlinx.coroutines.launch

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

        binding.tvEmail.text = "불러오는 중…"
        binding.tvSellerId.text = "#${session.memberId}"
        binding.tvStoreName.text = session.nickname.ifBlank { "—" }
        binding.tvBusinessNumber.text = "정보 없음"

        lifecycleScope.launch {
            runCatching {
                val me = RetrofitClient.api.getMyInfo().data
                binding.tvEmail.text = me?.email?.takeIf { it.isNotBlank() } ?: "카카오 소셜 계정"
                binding.tvStoreName.text = (me?.nickname ?: session.nickname).ifBlank { "—" }
            }.onFailure {
                binding.tvEmail.text = "카카오 소셜 계정"
                Toast.makeText(this@AccountInfoActivity, "계정 정보를 불러올 수 없어요.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnInstagram.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("SNS 연동")
                .setMessage("인스타그램 계정은 현재 매장 홍보 채널로 수동 관리됩니다.\n\n프로필 또는 게시물 링크를 고객 알림 문구에 포함하면 구매자에게 바로 노출할 수 있어요. 정식 OAuth 연동은 운영 서버 도입 후 연결하는 흐름으로 분리해두는 것이 안전합니다.")
                .setPositiveButton("확인", null)
                .show()
        }
    }
}
