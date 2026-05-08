package com.example.deuktemsiru_seller.ui.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.R
import com.example.deuktemsiru_seller.data.SessionManager
import com.example.deuktemsiru_seller.databinding.BottomSheetOrderDetailBinding
import com.example.deuktemsiru_seller.network.OrderApiResponse
import com.example.deuktemsiru_seller.network.RetrofitClient
import com.example.deuktemsiru_seller.network.UpdateOrderStatusRequest
import com.example.deuktemsiru_seller.util.LocalNotificationHelper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import kotlinx.coroutines.launch

class OrderDetailBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetOrderDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var order: OrderApiResponse
    private lateinit var session: SessionManager

    companion object {
        private const val STATUS_NEW = "NEW"
        private const val STATUS_PREPARING = "PREPARING"
        private const val STATUS_READY = "READY"
        private const val STATUS_COMPLETED = "COMPLETED"

        fun newInstance(order: OrderApiResponse) = OrderDetailBottomSheet().apply {
            arguments = Bundle().apply { putString("order", Gson().toJson(order)) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val json = arguments?.getString("order") ?: run { dismiss(); return }
        order = Gson().fromJson(json, OrderApiResponse::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())
        renderOrder()
    }

    private fun renderOrder() {
        binding.tvOrderNumber.text = order.orderNumber
        binding.tvCreatedAt.text = order.createdAt.take(16).replace("T", " ")
        binding.tvPickupTime.text = order.pickupTime
        binding.tvPickupCode.text = order.pickupCode.chunked(2).joinToString("-")
        binding.tvTotalAmount.text = "%,d원".format(order.totalAmount)

        val (statusText, statusBg, statusColor) = when (order.status.uppercase()) {
            STATUS_NEW -> Triple("신규 주문", R.drawable.bg_status_soldout, 0xFFE65100.toInt())
            STATUS_PREPARING -> Triple("준비 중", R.drawable.bg_status_available, 0xFF2E7D32.toInt())
            STATUS_READY -> Triple("픽업 대기", R.drawable.bg_rounded_primary, 0xFFFFFFFF.toInt())
            STATUS_COMPLETED -> Triple("완료", R.drawable.bg_status_expired, 0xFF757575.toInt())
            else -> Triple(order.status, R.drawable.bg_status_expired, 0xFF757575.toInt())
        }
        binding.tvStatusBadge.text = statusText
        binding.tvStatusBadge.setBackgroundResource(statusBg)
        binding.tvStatusBadge.setTextColor(statusColor)

        binding.itemsContainer.removeAllViews()
        order.items.forEach { item ->
            val row = TextView(requireContext()).apply {
                text = "${item.emoji} ${item.name}  ×${item.quantity}  %,d원".format(item.price * item.quantity)
                textSize = 14f
                setTextColor(requireContext().getColor(R.color.text))
                setPadding(0, 4, 0, 4)
            }
            binding.itemsContainer.addView(row)
        }

        val (actionText, nextStatus) = when (order.status.uppercase()) {
            STATUS_NEW -> "수락하기" to STATUS_PREPARING
            STATUS_PREPARING -> "준비 완료" to STATUS_READY
            STATUS_READY -> "픽업 완료 처리" to STATUS_COMPLETED
            else -> null to null
        }

        if (actionText != null && nextStatus != null) {
            binding.btnAction.text = actionText
            binding.btnAction.visibility = View.VISIBLE
            binding.btnAction.setOnClickListener { performAction(nextStatus) }
        }
    }

    private fun performAction(nextStatus: String) {
        binding.btnAction.isEnabled = false
        lifecycleScope.launch {
            try {
                RetrofitClient.api.updateOrderStatus(
                    order.id, session.sellerId, UpdateOrderStatusRequest(nextStatus)
                )
                val (title, body) = when (nextStatus) {
                    STATUS_PREPARING -> "🛍️ 주문 수락됨" to order.orderNumber
                    STATUS_READY -> "✅ 픽업 준비 완료" to "${order.orderNumber} · 코드 ${order.pickupCode}"
                    STATUS_COMPLETED -> "🎉 픽업 완료" to "+%,d원".format(order.totalAmount)
                    else -> "" to ""
                }
                if (title.isNotEmpty()) LocalNotificationHelper.show(requireContext(), title, body)
                Toast.makeText(requireContext(), "$title 처리됐어요", Toast.LENGTH_SHORT).show()
                parentFragmentManager.setFragmentResult("order_updated", Bundle().apply {
                    putInt("next_tab", statusToTabIndex(nextStatus))
                })
                dismiss()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "처리에 실패했어요", Toast.LENGTH_SHORT).show()
                binding.btnAction.isEnabled = true
            }
        }
    }

    private fun statusToTabIndex(status: String) = when (status) {
        STATUS_PREPARING -> 1
        STATUS_READY -> 2
        STATUS_COMPLETED -> 3
        else -> 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
