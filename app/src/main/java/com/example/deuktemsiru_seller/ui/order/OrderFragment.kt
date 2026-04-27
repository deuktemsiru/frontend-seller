package com.example.deuktemsiru_seller.ui.order

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.deuktemsiru_seller.R
import com.example.deuktemsiru_seller.databinding.FragmentOrderBinding
import com.example.deuktemsiru_seller.databinding.ItemOrderNewBinding
import com.example.deuktemsiru_seller.databinding.ItemOrderPreparingBinding

class OrderFragment : Fragment() {

    private var _binding: FragmentOrderBinding? = null
    private val binding get() = _binding!!

    private var currentTab = 0

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
        setupTabs()
        showNewOrders()
    }

    private fun setupTabs() {
        binding.tabNew.setOnClickListener { selectTab(0) }
        binding.tabPreparing.setOnClickListener { selectTab(1) }
        binding.tabPickup.setOnClickListener { selectTab(2) }
        binding.tabDone.setOnClickListener { selectTab(3) }
    }

    private fun selectTab(index: Int) {
        currentTab = index
        val tabs = listOf(binding.tabNew, binding.tabPreparing, binding.tabPickup, binding.tabDone)
        tabs.forEachIndexed { i, tab ->
            tab.setTextColor(if (i == index) requireContext().getColor(R.color.primary)
                           else requireContext().getColor(R.color.text_muted))
        }
        when (index) {
            0 -> showNewOrders()
            1 -> showPreparingOrders()
            2 -> showPickupOrders()
            3 -> showCompletedOrders()
        }
    }

    private fun showNewOrders() {
        binding.orderListContainer.removeAllViews()

        val orders = listOf(
            Triple("#OM-7842", "방금 도착", "17:30"),
            Triple("#OM-7843", "2분 전", "17:45"),
            Triple("#OM-7844", "5분 전", "18:00")
        )

        orders.forEach { (num, time, pickup) ->
            val itemBinding = ItemOrderNewBinding.inflate(layoutInflater, binding.orderListContainer, false)
            itemBinding.tvOrderNumber.text = num
            itemBinding.tvOrderTime.text = time
            itemBinding.tvPickupTime.text = pickup
            itemBinding.btnAccept.setOnClickListener {
                Toast.makeText(requireContext(), "$num 수락 완료 — 손님에게 알림이 발송됩니다", Toast.LENGTH_SHORT).show()
                binding.orderListContainer.removeView(itemBinding.root)
            }
            itemBinding.btnReject.setOnClickListener {
                Toast.makeText(requireContext(), "거절 사유를 선택해주세요", Toast.LENGTH_SHORT).show()
            }
            binding.orderListContainer.addView(itemBinding.root)
        }
    }

    private fun showPreparingOrders() {
        binding.orderListContainer.removeAllViews()

        repeat(2) { i ->
            val itemBinding = ItemOrderPreparingBinding.inflate(layoutInflater, binding.orderListContainer, false)
            itemBinding.btnReady.setOnClickListener {
                Toast.makeText(requireContext(), "픽업 대기로 이동 — 손님에게 '준비 완료' 알림을 보냈습니다", Toast.LENGTH_SHORT).show()
                binding.orderListContainer.removeView(itemBinding.root)
            }
            binding.orderListContainer.addView(itemBinding.root)
        }
    }

    private fun showPickupOrders() {
        binding.orderListContainer.removeAllViews()
        val emptyView = createEmptyView("픽업 대기 중인 주문 1건")
        binding.orderListContainer.addView(emptyView)
    }

    private fun showCompletedOrders() {
        binding.orderListContainer.removeAllViews()
        val emptyView = createEmptyView("오늘 완료된 주문 8건")
        binding.orderListContainer.addView(emptyView)
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
