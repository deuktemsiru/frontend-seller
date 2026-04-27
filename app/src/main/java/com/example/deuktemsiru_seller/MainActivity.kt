package com.example.deuktemsiru_seller

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.deuktemsiru_seller.databinding.ActivityMainBinding
import com.example.deuktemsiru_seller.ui.home.HomeFragment
import com.example.deuktemsiru_seller.ui.notification.NotificationFragment
import com.example.deuktemsiru_seller.ui.order.OrderFragment
import com.example.deuktemsiru_seller.ui.sales.SalesFragment
import com.example.deuktemsiru_seller.ui.store.StoreFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var orderBadgeCount = 3

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

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        setupBottomNav()
        updateOrderBadge()
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_order -> OrderFragment()
                R.id.nav_sales -> SalesFragment()
                R.id.nav_notification -> NotificationFragment()
                R.id.nav_store -> StoreFragment()
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
        binding.bottomNav.selectedItemId = R.id.nav_notification
    }
}
