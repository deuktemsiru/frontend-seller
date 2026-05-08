package com.example.deuktemsiru_seller

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.data.SessionManager
import com.example.deuktemsiru_seller.databinding.ActivityMainBinding
import com.example.deuktemsiru_seller.network.LoginRequest
import com.example.deuktemsiru_seller.network.RetrofitClient
import com.example.deuktemsiru_seller.network.SampleData
import android.content.Intent
import com.example.deuktemsiru_seller.ui.auth.RegisterActivity
import com.example.deuktemsiru_seller.ui.home.HomeFragment
import com.example.deuktemsiru_seller.ui.notification.NotificationFragment
import com.example.deuktemsiru_seller.ui.order.OrderFragment
import com.example.deuktemsiru_seller.ui.order.PickupVerifyActivity
import com.example.deuktemsiru_seller.ui.mypage.MyPageFragment
import com.example.deuktemsiru_seller.ui.product.ProductFragment
import com.example.deuktemsiru_seller.ui.sales.SalesFragment
import com.example.deuktemsiru_seller.ui.store.StoreFragment
import com.example.deuktemsiru_seller.util.LocalNotificationHelper
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var session: SessionManager
    private var orderBadgeCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            binding.bottomNav.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        session = SessionManager(this)
        RetrofitClient.authToken = session.token
        LocalNotificationHelper.createChannel(this)

        if (session.isSampleAccount && session.isLoggedIn()) {
            RetrofitClient.enableSampleMode(session.sellerId)
        }

        setupBottomNav()

        if (!session.isLoggedIn()) {
            showLogin()
        } else {
            showApp()
            if (savedInstanceState == null) {
                loadFragment(HomeFragment())
            }
            refreshOrderBadge()
        }

        binding.btnLogin.setOnClickListener { login() }
        binding.btnGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun login() {
        val email = binding.etLoginEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etLoginPassword.text?.toString().orEmpty()
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "로그인 중..."

        val sampleAccount = SampleData.findByCredentials(email, password)
        if (sampleAccount != null) {
            session.sellerId = sampleAccount.sellerId
            session.storeName = sampleAccount.storeName
            session.token = sampleAccount.token
            session.email = email
            session.isSampleAccount = true
            RetrofitClient.enableSampleMode(sampleAccount.sellerId)
            showApp()
            loadFragment(HomeFragment())
            refreshOrderBadge()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.login(LoginRequest(email, password))
                if (response.role != "SELLER") {
                    Toast.makeText(this@MainActivity, "판매자 계정으로 로그인해주세요.", Toast.LENGTH_SHORT).show()
                    resetLoginButton()
                    return@launch
                }
                session.sellerId = response.userId
                session.storeName = response.nickname
                session.token = response.token
                session.email = email
                session.isSampleAccount = false
                RetrofitClient.authToken = response.token
                runCatching {
                    session.storeName = RetrofitClient.api.getMyStore(response.userId).name
                }
                showApp()
                loadFragment(HomeFragment())
                refreshOrderBadge()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "로그인 실패: 입력값과 서버 상태를 확인해주세요.", Toast.LENGTH_LONG).show()
                resetLoginButton()
            }
        }
    }

    private fun showLogin() {
        binding.loginContainer.visibility = View.VISIBLE
        binding.fragmentContainer.visibility = View.GONE
        binding.bottomNav.visibility = View.GONE
    }

    private fun showApp() {
        binding.loginContainer.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE
        binding.bottomNav.visibility = View.VISIBLE
    }

    private fun resetLoginButton() {
        binding.btnLogin.isEnabled = true
        binding.btnLogin.text = "로그인하기"
    }

    private fun refreshOrderBadge() {
        if (!session.isLoggedIn()) return
        lifecycleScope.launch {
            try {
                val orders = RetrofitClient.api.getOrders(session.sellerId)
                orderBadgeCount = orders.count { it.status == "NEW" }
                updateOrderBadge()
            } catch (e: Exception) {
                // 배지 업데이트 실패 시 무시
            }
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_order -> {
                    orderBadgeCount = 0
                    updateOrderBadge()
                    OrderFragment()
                }
                R.id.nav_sales -> SalesFragment()
                R.id.nav_product -> ProductFragment()
                R.id.nav_my -> MyPageFragment()
                else -> HomeFragment()
            }
            loadFragment(fragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun updateOrderBadge() {
        val badge = binding.bottomNav.getOrCreateBadge(R.id.nav_order)
        if (orderBadgeCount > 0) {
            badge.isVisible = true
            badge.number = orderBadgeCount
            badge.backgroundColor = getColor(R.color.danger)
            badge.badgeTextColor = getColor(R.color.white)
        } else {
            badge.isVisible = false
        }
    }

    fun navigateToOrder() {
        binding.bottomNav.selectedItemId = R.id.nav_order
    }

    fun navigateToNotification() {
        loadFragment(NotificationFragment())
    }

    fun launchPickupVerify() {
        startActivity(Intent(this, PickupVerifyActivity::class.java))
    }

    fun navigateToStore() {
        loadFragment(StoreFragment())
    }
}
