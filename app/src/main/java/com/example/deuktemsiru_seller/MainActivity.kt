package com.example.deuktemsiru_seller

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.data.SessionManager
import com.example.deuktemsiru_seller.databinding.ActivityMainBinding
import com.example.deuktemsiru_seller.network.RetrofitClient
import com.example.deuktemsiru_seller.ui.auth.LoginActivity
import com.example.deuktemsiru_seller.ui.home.HomeFragment
import com.example.deuktemsiru_seller.ui.mypage.MyPageFragment
import com.example.deuktemsiru_seller.ui.notification.NotificationFragment
import com.example.deuktemsiru_seller.ui.order.OrderFragment
import com.example.deuktemsiru_seller.ui.order.PickupVerifyActivity
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
        LocalNotificationHelper.createChannel(this)

        // 로그인 상태 확인 — 미로그인 시 LoginActivity로 이동
        if (!session.isLoggedIn()) {
            navigateToLogin()
            return
        }

        // 저장된 토큰 복원
        session.restoreToken()

        setupBottomNav()

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
        refreshOrderBadge()
    }

    // ── 로그아웃 (MyPageFragment에서 호출) ──────────────────────
    fun logout() {
        lifecycleScope.launch {
            runCatching { RetrofitClient.api.logout() }
            session.clear()
            navigateToLogin()
        }
    }

    // ── 화면 이동 ─────────────────────────────────────────────
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

    // ── 내부 유틸 ─────────────────────────────────────────────
    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun refreshOrderBadge() {
        lifecycleScope.launch {
            runCatching {
                val response = RetrofitClient.api.getOrders()
                orderBadgeCount = response.data?.count { it.status == "PENDING" } ?: 0
                updateOrderBadge()
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
}
