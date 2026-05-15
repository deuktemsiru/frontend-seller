package com.example.deuktemsiru_seller.ui.order

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.R
import com.example.deuktemsiru_seller.data.SessionManager
import com.example.deuktemsiru_seller.databinding.FragmentOrderBinding
import com.example.deuktemsiru_seller.databinding.ItemOrderCompletedBinding
import com.example.deuktemsiru_seller.databinding.ItemOrderNewBinding
import com.example.deuktemsiru_seller.databinding.ItemOrderPreparingBinding
import com.example.deuktemsiru_seller.network.OrderApiResponse
import com.example.deuktemsiru_seller.network.RetrofitClient
import com.example.deuktemsiru_seller.network.UpdateOrderStatusRequest
import com.example.deuktemsiru_seller.ui.auth.LoginActivity
import com.example.deuktemsiru_seller.util.LocalNotificationHelper
import com.example.deuktemsiru_seller.ui.settings.NotificationSettingsActivity
import kotlinx.coroutines.launch
import retrofit2.HttpException

class OrderFragment : Fragment() {

    private var _binding: FragmentOrderBinding? = null
    private val binding get() = _binding!!

    private var currentTab = 0
    private var allOrders: List<OrderApiResponse> = emptyList()
    private lateinit var session: SessionManager

    private companion object {
        const val TAG = "OrderFragment"
        const val STATUS_NEW = "PENDING"
        const val STATUS_CONFIRMED = "CONFIRMED"
        const val STATUS_PICKED_UP = "PICKED_UP"
        const val STATUS_CANCELLED = "CANCELLED"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())
        setupTabs()
        val mainActivity = activity as? com.example.deuktemsiru_seller.MainActivity
        val pendingTab = mainActivity?.pendingOrderTab
        when {
            pendingTab != null -> {
                mainActivity.pendingOrderTab = null
                loadOrders(selectTabAfterLoad = pendingTab)
            }
            mainActivity?.orderCompletedRequested == true -> {
                mainActivity.orderCompletedRequested = false
                loadOrders(selectTabAfterLoad = 3)
            }
            else -> loadOrders()
        }
        binding.btnPickupVerify.setOnClickListener {
            startActivity(Intent(requireContext(), PickupVerifyActivity::class.java))
        }
        childFragmentManager.setFragmentResultListener("order_updated", viewLifecycleOwner) { _, bundle ->
            loadOrders(selectTabAfterLoad = bundle.getInt("next_tab", currentTab))
        }
    }

    private fun loadOrders(selectTabAfterLoad: Int = currentTab) {
        if (!session.isLoggedIn()) return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                allOrders = RetrofitClient.api.getOrders().data ?: emptyList()
                updateTabCounts()
                selectTab(selectTabAfterLoad)
            } catch (e: Exception) {
                if (handleAuthFailure(e)) return@launch
                Toast.makeText(requireContext(), "주문 목록을 불러오지 못했어요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupTabs() {
        // 픽업대기 탭은 스펙상 별도 상태 없음 → 숨김
        binding.tabPickup.visibility = View.GONE
        binding.tabNew.setOnClickListener { selectTab(0) }
        binding.tabPreparing.setOnClickListener { selectTab(1) }
        binding.tabDone.setOnClickListener { selectTab(3) }
        updateTabCounts()
    }

    private fun selectTab(index: Int) {
        currentTab = index
        val tabs = listOf(binding.tabNew, binding.tabPreparing, binding.tabPickup, binding.tabDone)
        tabs.forEachIndexed { i, tab ->
            if (i == index) {
                tab.setTextColor(requireContext().getColor(R.color.primary))
                tab.setBackgroundResource(R.drawable.bg_tab_active)
                tab.setPadding(0, 0, 0, 0)
            } else {
                tab.setTextColor(requireContext().getColor(R.color.text_muted))
                tab.background = null
            }
        }
        showTab(index)
    }

    private fun showTab(index: Int) {
        when (index) {
            0 -> showNewOrders(ordersByStatus(STATUS_NEW))
            1 -> showConfirmedOrders(ordersByStatus(STATUS_CONFIRMED))
            3 -> showCompletedOrders(ordersByStatuses(STATUS_PICKED_UP, STATUS_CANCELLED))
            else -> showNewOrders(ordersByStatus(STATUS_NEW))
        }
    }

    private fun ordersByStatus(status: String): List<OrderApiResponse> =
        allOrders.filter { it.status.equals(status, ignoreCase = true) }

    private fun ordersByStatuses(vararg statuses: String): List<OrderApiResponse> =
        allOrders.filter { order -> statuses.any { order.status.equals(it, ignoreCase = true) } }

    private fun updateTabCounts() {
        binding.tabNew.text = getString(R.string.tab_new_order_count, ordersByStatus(STATUS_NEW).size)
        binding.tabPreparing.text = getString(R.string.tab_preparing_count, ordersByStatus(STATUS_CONFIRMED).size)
        binding.tabDone.text = getString(R.string.tab_completed_count, ordersByStatuses(STATUS_PICKED_UP, STATUS_CANCELLED).size)
    }

    private fun showNewOrders(orders: List<OrderApiResponse>) {
        renderOrders(orders, "새로운 주문이 없어요") { order ->
            val itemBinding = ItemOrderNewBinding.inflate(layoutInflater, binding.orderListContainer, false)
            itemBinding.tvOrderNumber.text = order.orderNumber ?: "#${order.id}"
            itemBinding.tvOrderTime.text = getString(R.string.just_arrived)
            itemBinding.tvPickupTime.text = order.pickupTime ?: ""
            itemBinding.tvMenuSummary.text = formatMenuSummary(order)
            itemBinding.tvTotalAmount.text = formatPrice(order.totalAmount)
            itemBinding.root.setOnClickListener { showDetail(order) }
            itemBinding.btnAccept.setOnClickListener {
                setButtonsEnabled(listOf(itemBinding.btnAccept, itemBinding.btnReject), false)
                updateStatus(order.id, STATUS_CONFIRMED, onSuccess = { updatedOrder ->
                    replaceOrder(updatedOrder)
                    if (NotificationSettingsActivity.isEnabled(requireContext(), "new_order")) {
                        LocalNotificationHelper.show(requireContext(), "🛍️ 주문 수락됨", order.orderNumber ?: "")
                    }
                    Toast.makeText(requireContext(), "${order.orderNumber ?: "주문"} 수락 완료", Toast.LENGTH_SHORT).show()
                    loadOrders(selectTabAfterLoad = 1)
                }, onFailure = {
                    setButtonsEnabled(listOf(itemBinding.btnAccept, itemBinding.btnReject), true)
                })
            }
            itemBinding.btnReject.setOnClickListener {
                setButtonsEnabled(listOf(itemBinding.btnAccept, itemBinding.btnReject), false)
                updateStatus(order.id, STATUS_CANCELLED, onSuccess = { updatedOrder ->
                    replaceOrder(updatedOrder)
                    Toast.makeText(requireContext(), "${order.orderNumber ?: "주문"} 거절됨", Toast.LENGTH_SHORT).show()
                    loadOrders()
                }, onFailure = {
                    setButtonsEnabled(listOf(itemBinding.btnAccept, itemBinding.btnReject), true)
                })
            }
            itemBinding.root
        }
    }

    private fun showConfirmedOrders(orders: List<OrderApiResponse>) {
        renderOrders(orders, "준비중인 주문이 없어요") { order ->
            val itemBinding = ItemOrderPreparingBinding.inflate(layoutInflater, binding.orderListContainer, false)
            itemBinding.tvOrderNumber.text = order.orderNumber ?: "#${order.id}"
            itemBinding.tvElapsedTime.text = elapsedLabel(order.createdAt)
            itemBinding.tvPickupTime.text = order.pickupTime ?: ""
            itemBinding.tvMenuSummary.text = formatMenuSummary(order)
            itemBinding.tvTotalAmount.text = formatPrice(order.totalAmount)
            itemBinding.root.setOnClickListener { showDetail(order) }
            // READY 상태 없음 → 픽업 준비 완료 버튼 숨김
            itemBinding.btnReady.visibility = View.GONE
            itemBinding.root
        }
    }

    private fun showCompletedOrders(orders: List<OrderApiResponse>) {
        renderOrders(orders, "완료된 주문이 없어요") { order ->
            val itemBinding = ItemOrderCompletedBinding.inflate(layoutInflater, binding.orderListContainer, false)
            itemBinding.tvOrderNumber.text = order.orderNumber ?: "#${order.id}"
            itemBinding.tvPickupTime.text = order.pickupTime ?: ""
            itemBinding.tvMenuSummary.text = formatMenuSummary(order)
            itemBinding.tvTotalAmount.text = formatPrice(order.totalAmount)
            itemBinding.root.setOnClickListener { showDetail(order) }
            itemBinding.root
        }
    }

    private fun renderOrders(
        orders: List<OrderApiResponse>,
        emptyMessage: String,
        createItemView: (OrderApiResponse) -> View,
    ) {
        binding.orderListContainer.removeAllViews()
        if (orders.isEmpty()) {
            binding.orderListContainer.addView(createEmptyView(emptyMessage))
            return
        }
        orders.map(createItemView).forEach { binding.orderListContainer.addView(it) }
    }

    private fun updateStatus(
        orderId: Long,
        status: String,
        onSuccess: (OrderApiResponse) -> Unit,
        onFailure: () -> Unit,
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val updatedOrder = RetrofitClient.api.updateOrderStatus(
                    orderId = orderId,
                    req = UpdateOrderStatusRequest(status),
                ).data ?: return@launch
                onSuccess(updatedOrder)
            } catch (e: Exception) {
                if (handleAuthFailure(e)) return@launch
                onFailure()
                Log.e(TAG, "Failed to update order status. orderId=$orderId status=$status", e)
                Toast.makeText(requireContext(), statusUpdateErrorMessage(e), Toast.LENGTH_SHORT).show()
                // Intentionally reload the full order list on failure to restore consistent UI state.
                loadOrders()
            }
        }
    }

    private fun showDetail(order: OrderApiResponse) {
        OrderDetailBottomSheet.newInstance(order).show(childFragmentManager, "order_detail")
    }

    private fun replaceOrder(updatedOrder: OrderApiResponse) {
        allOrders = allOrders.map { if (it.id == updatedOrder.id) updatedOrder else it }
    }

    private fun setButtonsEnabled(buttons: List<Button>, enabled: Boolean) {
        buttons.forEach { it.isEnabled = enabled }
    }

    private fun formatMenuSummary(order: OrderApiResponse): String =
        order.items.joinToString(", ") { item ->
            val emojiPart = item.emoji?.let { "$it " } ?: ""
            "$emojiPart${item.name} × ${item.quantity}"
        }

    private fun elapsedLabel(createdAt: String): String {
        return try {
            val created = runCatching { java.time.OffsetDateTime.parse(createdAt) }
                .getOrElse { java.time.LocalDateTime.parse(createdAt).atOffset(java.time.OffsetDateTime.now().offset) }
            val minutes = java.time.Duration.between(created, java.time.OffsetDateTime.now()).toMinutes()
            when {
                minutes < 1 -> "방금 전"
                minutes < 60 -> "${minutes}분 경과"
                else -> "${minutes / 60}시간 ${minutes % 60}분 경과"
            }
        } catch (_: Exception) { "" }
    }

    private fun formatPrice(amount: Int): String = "%,d원".format(amount)

    private fun statusUpdateErrorMessage(error: Exception): String {
        if (error !is HttpException) return "상태 업데이트에 실패했어요."
        val detail = error.response()?.errorBody()?.string()
            ?.let { Regex(""""message"\s*:\s*"([^"]+)"""").find(it)?.groupValues?.getOrNull(1) ?: "오류가 발생했습니다" }
        return if (detail.isNullOrBlank()) "상태 업데이트에 실패했어요. (${error.code()})"
        else "상태 업데이트에 실패했어요. $detail"
    }

    private fun handleAuthFailure(error: Exception): Boolean {
        if (error !is HttpException || error.code() !in listOf(401, 403)) return false
        session.clear()
        Toast.makeText(requireContext(), "다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
        startActivity(
            Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        return true
    }

    private fun createEmptyView(message: String): View {
        val tv = android.widget.TextView(requireContext())
        tv.text = message
        tv.textSize = 14f
        tv.setTextColor(requireContext().getColor(R.color.text_sub))
        tv.gravity = android.view.Gravity.CENTER
        val params = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.topMargin = (48 * resources.displayMetrics.density).toInt()
        tv.layoutParams = params
        return tv
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
