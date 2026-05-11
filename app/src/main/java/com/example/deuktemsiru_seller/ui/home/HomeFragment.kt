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
import com.example.deuktemsiru_seller.network.SaleItemApiResponse
import com.example.deuktemsiru_seller.network.RetrofitClient
import com.example.deuktemsiru_seller.ui.product.ProductListingActivity
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
        binding.tvStoreName.text = session.nickname.ifBlank { "내 가게" }

        binding.cardSales.setOnClickListener { (activity as? MainActivity)?.navigateToOrder() }
        binding.cardNewOrder.setOnClickListener { (activity as? MainActivity)?.navigateToOrder() }
        binding.btnRegisterMenu.setOnClickListener {
            startActivity(Intent(requireContext(), ProductListingActivity::class.java))
        }
        binding.btnSendNotification.setOnClickListener {
            (activity as? MainActivity)?.navigateToNotification()
        }

        if (session.isLoggedIn()) {
            loadStore()
            loadStats()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::session.isInitialized && session.isLoggedIn()) {
            loadStore()
            loadStats()
        }
    }

    private fun loadStore() {
        lifecycleScope.launch {
            runCatching {
                val store = RetrofitClient.api.getMyStore().data
                if (store != null) {
                    binding.tvStoreName.text = store.name
                    binding.tvClosingTime.text = "마감 ${store.closingTime}"
                }
            }
            // 활성 판매 상품 목록 로드
            runCatching {
                val items = RetrofitClient.api.getSaleItems().data ?: emptyList()
                renderActiveSaleItems(items)
            }.onFailure {
                renderActiveSaleItems(emptyList())
            }
        }
    }

    private fun renderActiveSaleItems(items: List<SaleItemApiResponse>) {
        binding.activeMenuContainer.removeAllViews()
        val activeItems = items.filter { it.status == "AVAILABLE" && it.remainingItems > 0 }

        if (activeItems.isEmpty()) {
            binding.activeMenuContainer.addView(createEmptyMenuView())
            return
        }

        activeItems.forEach { item ->
            val itemBinding = ItemActiveMenuBinding.inflate(layoutInflater, binding.activeMenuContainer, false)
            itemBinding.tvMenuEmoji.text = item.emoji
            itemBinding.tvMenuName.text = item.name
            itemBinding.tvRemaining.text = getString(R.string.remaining_items, item.remainingItems)
            itemBinding.tvPrice.text = "%,d원".format(item.discountedPrice)
            itemBinding.tvTimeBadge.text = item.pickupTimeSlot
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

    private fun loadStats() {
        lifecycleScope.launch {
            runCatching {
                val sales = RetrofitClient.api.getSales().data
                if (sales != null) {
                    binding.tvTodaySales.text = "%,d원".format(sales.todaySales)
                    binding.tvTodayOrders.text = "${sales.todayOrderCount}건"
                }
            }
        }
        lifecycleScope.launch {
            runCatching {
                val orders = RetrofitClient.api.getOrders().data ?: emptyList()
                val newCount = orders.count { it.status.equals("PENDING", ignoreCase = true) }
                val preparingCount = orders.count { it.status.equals("PREPARING", ignoreCase = true) }
                val pickupCount = orders.count { it.status.equals("READY", ignoreCase = true) }
                binding.tvNewOrderAlert.text = if (newCount > 0) {
                    getString(R.string.new_order_alert_count, newCount)
                } else {
                    getString(R.string.new_order_alert_empty)
                }
                binding.tvReservationNew.text = newCount.toString()
                binding.tvReservationPreparing.text = preparingCount.toString()
                binding.tvReservationPickup.text = pickupCount.toString()
            }.onFailure {
                binding.tvNewOrderAlert.text = getString(R.string.new_order_alert_empty)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
