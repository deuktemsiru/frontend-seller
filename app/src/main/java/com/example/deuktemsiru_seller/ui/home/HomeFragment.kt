package com.example.deuktemsiru_seller.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.R
import com.example.deuktemsiru_seller.MainActivity
import com.example.deuktemsiru_seller.data.SessionManager
import com.example.deuktemsiru_seller.databinding.FragmentHomeBinding
import com.example.deuktemsiru_seller.databinding.ItemActiveMenuBinding
import com.example.deuktemsiru_seller.network.MenuItemApiResponse
import com.example.deuktemsiru_seller.network.RetrofitClient
import com.example.deuktemsiru_seller.ui.registration.MenuRegistrationActivity
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var session: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        session = SessionManager(requireContext())
        RetrofitClient.authToken = session.token
        binding.tvStoreName.text = session.storeName.ifBlank { "내 가게" }

        binding.cardSales.setOnClickListener { (activity as? MainActivity)?.navigateToOrder() }
        binding.cardNewOrder.setOnClickListener { (activity as? MainActivity)?.navigateToOrder() }
        binding.btnRegisterMenu.setOnClickListener {
            startActivity(Intent(requireContext(), MenuRegistrationActivity::class.java))
        }
        binding.btnSendNotification.setOnClickListener {
            (activity as? MainActivity)?.navigateToNotification()
        }

        if (session.isLoggedIn()) {
            loadStore(session)
            loadStats(session.sellerId)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::session.isInitialized && session.isLoggedIn()) {
            loadStore(session)
            loadStats(session.sellerId)
        }
    }

    private fun loadStore(session: SessionManager) {
        lifecycleScope.launch {
            try {
                val store = RetrofitClient.api.getMyStore(session.sellerId)
                session.storeName = store.name
                binding.tvStoreName.text = store.name
                binding.tvClosingTime.text = "마감 ${store.closingTime}"
                renderActiveMenus(store.menus)
            } catch (e: Exception) {
                binding.tvClosingTime.text = ""
                renderActiveMenus(emptyList())
            }
        }
    }

    private fun renderActiveMenus(menus: List<MenuItemApiResponse>) {
        binding.activeMenuContainer.removeAllViews()
        val activeMenus = menus.filter { !it.isSoldOut && it.remainingItems > 0 }

        if (activeMenus.isEmpty()) {
            binding.activeMenuContainer.addView(createEmptyMenuView())
            return
        }

        activeMenus.forEach { menu ->
            val itemBinding = ItemActiveMenuBinding.inflate(layoutInflater, binding.activeMenuContainer, false)
            itemBinding.tvMenuEmoji.text = menu.emoji
            itemBinding.tvMenuName.text = menu.name
            itemBinding.tvRemaining.text = getString(R.string.remaining_items, menu.remainingItems)
            itemBinding.tvPrice.text = "%,d원".format(menu.discountedPrice)
            itemBinding.tvTimeBadge.text = menu.pickupTimeSlot
            itemBinding.tvTimeBadge.setTextColor(requireContext().getColor(R.color.warning))
            itemBinding.tvTimeBadge.setBackgroundResource(R.drawable.bg_rounded_warning)
            binding.activeMenuContainer.addView(itemBinding.root)
        }
    }

    private fun createEmptyMenuView(): View {
        return TextView(requireContext()).apply {
            text = getString(R.string.empty_menus)
            textSize = 13f
            setTextColor(requireContext().getColor(R.color.text_sub))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 24, 0, 24)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }
    }

    private fun loadStats(sellerId: Long) {
        lifecycleScope.launch {
            try {
                val sales = RetrofitClient.api.getSales(sellerId)
                binding.tvTodaySales.text = "%,d원".format(sales.todaySales)
                binding.tvTodayOrders.text = "${sales.todayOrderCount}건"
            } catch (e: Exception) {
                // 통계 로드 실패 시 기본값 유지
            }
        }
        lifecycleScope.launch {
            try {
                val newOrderCount = RetrofitClient.api.getOrders(sellerId)
                    .count { it.status.equals("NEW", ignoreCase = true) }
                binding.tvNewOrderAlert.text = if (newOrderCount > 0) {
                    getString(R.string.new_order_alert_count, newOrderCount)
                } else {
                    getString(R.string.new_order_alert_empty)
                }
            } catch (e: Exception) {
                binding.tvNewOrderAlert.text = getString(R.string.new_order_alert_empty)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
