package com.example.deuktemsiru_seller.ui.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.R
import com.example.deuktemsiru_seller.data.SessionManager
import com.example.deuktemsiru_seller.databinding.BottomSheetOrderDetailBinding
import com.example.deuktemsiru_seller.network.OrderStatus
import com.example.deuktemsiru_seller.network.OrderApiResponse
import com.example.deuktemsiru_seller.network.RetrofitClient
import com.example.deuktemsiru_seller.network.UpdateOrderStatusRequest
import com.example.deuktemsiru_seller.network.badgeStyle
import com.example.deuktemsiru_seller.util.LocalNotificationHelper
import com.example.deuktemsiru_seller.util.renderChildren
import com.example.deuktemsiru_seller.util.toast
import com.example.deuktemsiru_seller.util.toWon
import com.example.deuktemsiru_seller.util.visibleIf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class OrderDetailBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetOrderDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var order: OrderApiResponse
    private lateinit var session: SessionManager

    companion object {
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
        binding.rowCustomerName.visibleIf(!name.isNullOrBlank())
        binding.tvCustomerName.text = name.orEmpty()

        binding.tvPickupTime.text = order.pickupTime ?: ""
        binding.tvPickupCode.text = order.pickupCode?.chunked(2)?.joinToString("-") ?: "-"
        binding.tvTotalAmount.text = order.totalAmount.toWon()

        val badge = order.orderStatus.badgeStyle(order.status)
        binding.tvStatusBadge.text = badge.text
        binding.tvStatusBadge.setBackgroundResource(badge.backgroundRes)
        binding.tvStatusBadge.setTextColor(badge.textColor)

        binding.itemsContainer.renderChildren(
            items = order.items,
            emptyView = { TextView(requireContext()) },
        ) { item ->
            val row = TextView(requireContext()).apply {
                val emojiPart = item.emoji?.let { "$it " } ?: ""
                text = "$emojiPart${item.name}  ×${item.quantity}  ${(item.price * item.quantity).toWon()}"
                textSize = 14f
                setTextColor(requireContext().getColor(R.color.text))
                setPadding(0, 4, 0, 4)
            }
            row
        }

        val (actionText, nextStatus) = when (order.orderStatus) {
            OrderStatus.Pending -> "수락하기" to OrderStatus.Confirmed
            OrderStatus.Confirmed -> "픽업 완료 처리" to OrderStatus.PickedUp
            else -> null to null
        }

        if (actionText != null && nextStatus != null) {
            binding.btnAction.text = actionText
            binding.btnAction.visibleIf(true)
            binding.btnAction.setOnClickListener { performAction(nextStatus) }
        } else {
            binding.btnAction.visibleIf(false)
        }
    }

    private fun performAction(nextStatus: OrderStatus) {
        binding.btnAction.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                RetrofitClient.api.updateOrderStatus(
                    order.id, UpdateOrderStatusRequest(nextStatus.apiValue)
                )
                val (title, body) = when (nextStatus) {
                    OrderStatus.Confirmed -> "🛍️ 주문 수락됨" to (order.orderNumber ?: "")
                    OrderStatus.PickedUp -> "🎉 픽업 완료" to "+${order.totalAmount.toWon()}"
                    else -> "" to ""
                }
                if (title.isNotEmpty()) LocalNotificationHelper.show(requireContext(), title, body)
                toast("$title 처리됐어요")
                parentFragmentManager.setFragmentResult("order_updated", Bundle().apply {
                    putInt("next_tab", statusToTabIndex(nextStatus))
                })
                dismiss()
            } catch (e: Exception) {
                toast("처리에 실패했어요")
                binding.btnAction.isEnabled = true
            }
        }
    }

    private fun statusToTabIndex(status: OrderStatus) = when (status) {
        OrderStatus.Confirmed -> 1
        OrderStatus.PickedUp -> 3
        else -> 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
