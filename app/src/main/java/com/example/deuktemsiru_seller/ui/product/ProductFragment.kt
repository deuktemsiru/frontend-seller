package com.example.deuktemsiru_seller.ui.product

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.R
import com.example.deuktemsiru_seller.data.SessionManager
import com.example.deuktemsiru_seller.databinding.FragmentProductBinding
import com.example.deuktemsiru_seller.databinding.ItemSaleItemBinding
import com.example.deuktemsiru_seller.network.RetrofitClient
import com.example.deuktemsiru_seller.network.SaleItemApiResponse
import com.example.deuktemsiru_seller.network.UpdateSaleStatusRequest
import kotlinx.coroutines.launch

class ProductFragment : Fragment() {

    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var session: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())
        binding.btnAddProduct.setOnClickListener {
            startActivity(Intent(requireContext(), ProductListingActivity::class.java))
        }
        loadSaleItems()
    }

    override fun onResume() {
        super.onResume()
        loadSaleItems()
    }

    private fun loadSaleItems() {
        if (!session.isLoggedIn()) return
        lifecycleScope.launch {
            try {
                val items = RetrofitClient.api.getSaleItems(session.sellerId)
                renderItems(items)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "상품 목록을 불러올 수 없어요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderItems(items: List<SaleItemApiResponse>) {
        binding.saleItemsContainer.removeAllViews()
        if (items.isEmpty()) {
            binding.saleItemsContainer.addView(emptyView())
            return
        }
        items.forEach { item ->
            val itemBinding = ItemSaleItemBinding.inflate(layoutInflater, binding.saleItemsContainer, false)
            itemBinding.tvItemEmoji.text = item.emoji
            itemBinding.tvItemName.text = item.name
            itemBinding.tvItemDetail.text = "${"%,d원".format(item.discountedPrice)} · 잔여 ${item.remainingItems}/${item.totalItems}개 · ${item.pickupTimeSlot}"

            val (statusText, statusBg, statusColor) = when (item.status) {
                "AVAILABLE" -> Triple("판매중", R.drawable.bg_status_available, 0xFF2E7D32.toInt())
                "SOLD_OUT" -> Triple("품절", R.drawable.bg_status_soldout, 0xFFE65100.toInt())
                else -> Triple("종료", R.drawable.bg_status_expired, 0xFF9E9E9E.toInt())
            }
            itemBinding.tvItemStatus.text = statusText
            itemBinding.tvItemStatus.setBackgroundResource(statusBg)
            itemBinding.tvItemStatus.setTextColor(statusColor)

            itemBinding.btnStatusAvailable.setOnClickListener { updateStatus(item.id, "AVAILABLE") }
            itemBinding.btnStatusSoldout.setOnClickListener { updateStatus(item.id, "SOLD_OUT") }
            itemBinding.btnCancel.setOnClickListener { confirmCancel(item) }

            binding.saleItemsContainer.addView(itemBinding.root)
        }
    }

    private fun updateStatus(itemId: Long, status: String) {
        lifecycleScope.launch {
            try {
                RetrofitClient.api.updateSaleStatus(itemId, session.sellerId, UpdateSaleStatusRequest(status))
                loadSaleItems()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "상태 변경에 실패했어요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmCancel(item: SaleItemApiResponse) {
        AlertDialog.Builder(requireContext())
            .setTitle("상품 취소")
            .setMessage("${item.name} 상품 등록을 취소할까요?")
            .setPositiveButton("취소하기") { _, _ ->
                lifecycleScope.launch {
                    try {
                        RetrofitClient.api.cancelSaleItem(item.id, session.sellerId)
                        Toast.makeText(requireContext(), "상품이 취소됐어요", Toast.LENGTH_SHORT).show()
                        loadSaleItems()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "취소에 실패했어요", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("닫기", null)
            .show()
    }

    private fun emptyView(): View = TextView(requireContext()).apply {
        text = "오늘 등록된 상품이 없어요\n아래 '+ 상품 등록' 버튼을 눌러 등록해보세요"
        textSize = 13f
        setTextColor(ContextCompat.getColor(requireContext(), R.color.text_sub))
        gravity = android.view.Gravity.CENTER
        setPadding(0, 60, 0, 60)
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
