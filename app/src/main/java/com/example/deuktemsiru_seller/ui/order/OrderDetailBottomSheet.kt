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
import kotlinx.coroutines.launch

class OrderDetailBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetOrderDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var order: OrderApiResponse
    private lateinit var session: SessionManager

    companion object {
        private const val STATUS_NEW = "PENDING"
        private const val STATUS_CONFIRMED = "CONFIRMED"
        private const val STATUS_PICKED_UP = "PICKED_UP"
        private const val STATUS_CANCELLED = "CANCELLED"

        fun newInstance(order: OrderApiResponse) = OrderDetailBottomSheet().apply {
            arguments = Bundle().apply { putSerializable("order", order) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        order = arguments?.getSerializable("order") as? OrderApiResponse ?: run { dismiss(); return }
    }

    override fun onStart() {
        super.onStart()
        dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.setBackgroundResource(R.drawable.bg_bottom_sheet)
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
        binding.tvOrderNumber.text = order.orderNumber ?: "#${order.id}"
        binding.tvCreatedAt.text = order.createdAt.take(16).replace("T", " ")

        val name = order.customerName
        if (name.isNullOrBlank()) {
            binding.rowCustomerName.visibility = View.GONE
        } else {
            binding.tvCustomerName.text = name
        }

        binding.tvPickupTime.text = order.pickupTime ?: ""
        binding.tvPickupCode.text = order.pickupCode?.chunked(2)?.joinToString("-") ?: "-"
        binding.tvTotalAmount.text = "%,d원".format(order.totalAmount)

        val (statusText, statusBg, statusColor) = when (order.status.uppercase()) {
            STATUS_NEW -> Triple("신규 주문", R.drawable.bg_status_soldout, 0xFFE65100.toInt())
            STATUS_CONFIRMED -> Triple("준비 중", R.drawable.bg_status_available, 0xFF2E7D32.toInt())
            STATUS_PICKED_UP -> Triple("픽업 완료", R.drawable.bg_status_expired, 0xFF757575.toInt())
            STATUS_CANCELLED -> Triple("취소됨", R.drawable.bg_status_expired, 0xFF757575.toInt())
            else -> Triple(order.status, R.drawable.bg_status_expired, 0xFF757575.toInt())
        }
        binding.tvStatusBadge.text = statusText
        binding.tvStatusBadge.setBackgroundResource(statusBg)
        binding.tvStatusBadge.setTextColor(statusColor)

        binding.itemsContainer.removeAllViews()
        order.items.forEach { item ->
            val row = TextView(requireContext()).apply {
                val emojiPart = item.emoji?.let { "$it " } ?: ""
                text = "$emojiPart${item.name}  ×${item.quantity}  %,d원".format(item.price * item.quantity)
                textSize = 14f
                setTextColor(requireContext().getColor(R.color.text))
                setPadding(0, 4, 0, 4)
            }
            binding.itemsContainer.addView(row)
        }

        val (actionText, nextStatus) = when (order.status.uppercase()) {
            STATUS_NEW -> "수락하기" to STATUS_CONFIRMED
            STATUS_CONFIRMED -> "픽업 완료 처리" to STATUS_PICKED_UP
            else -> null to null
        }

        if (actionText != null && nextStatus != null) {
            binding.btnAction.text = actionText
            binding.btnAction.visibility = View.VISIBLE
            binding.btnAction.setOnClickListener { performAction(nextStatus) }
        } else {
            binding.btnAction.visibility = View.GONE
        }
    }

    private fun performAction(nextStatus: String) {
        binding.btnAction.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                RetrofitClient.api.updateOrderStatus(
                    order.id, UpdateOrderStatusRequest(nextStatus)
                )
                val (title, body) = when (nextStatus) {
                    STATUS_CONFIRMED -> "🛍️ 주문 수락됨" to (order.orderNumber ?: "")
                    STATUS_PICKED_UP -> "🎉 픽업 완료" to "+%,d원".format(order.totalAmount)
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
        STATUS_CONFIRMED -> 1
        STATUS_PICKED_UP -> 3
        else -> 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
