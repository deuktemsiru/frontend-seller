package com.example.deuktemsiru_seller.ui.product

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
            runCatching {
                val items = RetrofitClient.api.getSaleItems().data?.products ?: emptyList()
                renderItems(items)
            }.onFailure {
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
            itemBinding.tvItemDetail.text =
                "${"%,d원".format(item.discountedPrice)} · 잔여 ${item.remainingItems}/${item.totalItems}개 · ${item.displayPickupTime}"

            val (statusText, badgeBgColor, badgeTextColor) = when (item.status) {
                "AVAILABLE" -> Triple("● 판매중", 0xFF2E7D32.toInt(), 0xFFFFFFFF.toInt())
                "SOLD_OUT"  -> Triple("● 품절",   0xFFE65100.toInt(), 0xFFFFFFFF.toInt())
                "CANCELLED" -> Triple("취소됨",   0xFF9E9E9E.toInt(), 0xFFFFFFFF.toInt())
                else        -> Triple("종료",     0xFF9E9E9E.toInt(), 0xFFFFFFFF.toInt())
            }
            itemBinding.tvItemStatus.text = statusText
            itemBinding.tvItemStatus.setTextColor(badgeTextColor)
            val badgeBg = android.graphics.drawable.GradientDrawable().apply {
                setColor(badgeBgColor)
                cornerRadius = 20f * resources.displayMetrics.density
            }
            itemBinding.tvItemStatus.background = badgeBg
            itemBinding.tvItemStatus.setPadding(
                (8 * resources.displayMetrics.density).toInt(),
                (4 * resources.displayMetrics.density).toInt(),
                (8 * resources.displayMetrics.density).toInt(),
                (4 * resources.displayMetrics.density).toInt()
            )

            fun styleButton(btn: android.widget.Button, isActive: Boolean, activeColor: Int) {
                if (isActive) {
                    val bg = android.graphics.drawable.GradientDrawable().apply {
                        setColor(activeColor)
                        cornerRadius = 8f * resources.displayMetrics.density
                    }
                    btn.background = bg
                    btn.setTextColor(0xFFFFFFFF.toInt())
                } else {
                    btn.setBackgroundResource(R.drawable.bg_rounded_muted)
                    btn.setTextColor(requireContext().getColor(R.color.text_sub))
                }
            }

            itemBinding.btnEdit.setOnClickListener { showEditDialog(item) }
            itemBinding.btnStatusAvailable.setOnClickListener { updateStatus(item.id, "AVAILABLE") }
            itemBinding.btnStatusSoldout.setOnClickListener { updateStatus(item.id, "SOLD_OUT") }
            itemBinding.btnCancel.setOnClickListener { confirmCancel(item) }
            val isFinal = item.status == "CANCELLED" || item.status == "EXPIRED"
            styleButton(itemBinding.btnStatusAvailable, item.status == "AVAILABLE", 0xFF2E7D32.toInt())
            styleButton(itemBinding.btnStatusSoldout, item.status == "SOLD_OUT", 0xFFE65100.toInt())
            itemBinding.btnCancel.setBackgroundResource(R.drawable.bg_rounded_muted)
            itemBinding.btnCancel.setTextColor(requireContext().getColor(R.color.text_sub))
            itemBinding.btnStatusAvailable.isEnabled = !isFinal
            itemBinding.btnStatusSoldout.isEnabled = !isFinal
            itemBinding.btnCancel.isEnabled = !isFinal
            itemBinding.btnStatusAvailable.alpha = if (isFinal) 0.35f else 1f
            itemBinding.btnStatusSoldout.alpha = if (isFinal) 0.35f else 1f
            itemBinding.btnCancel.alpha = if (isFinal) 0.35f else 1f

            binding.saleItemsContainer.addView(itemBinding.root)
        }
    }

    private fun updateStatus(itemId: Long, status: String) {
        lifecycleScope.launch {
            runCatching {
                RetrofitClient.api.updateSaleStatus(itemId, UpdateSaleStatusRequest(status))
                loadSaleItems()
            }.onFailure {
                Toast.makeText(requireContext(), "상태 변경에 실패했어요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditDialog(item: SaleItemApiResponse) {
        val dialogView = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
        }
        val etPrice = android.widget.EditText(requireContext()).apply {
            hint = "할인가 (원)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(item.discountedPrice.toString())
        }
        val etQty = android.widget.EditText(requireContext()).apply {
            hint = "잔여 수량"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(item.remainingItems.toString())
        }
        val tvPickup = android.widget.TextView(requireContext()).apply {
            text = "픽업시간: ${item.displayPickupTime}"
            textSize = 13f
            setTextColor(requireContext().getColor(R.color.text_sub))
            setPadding(0, 12, 0, 4)
        }
        dialogView.addView(etPrice)
        dialogView.addView(etQty)
        dialogView.addView(tvPickup)

        AlertDialog.Builder(requireContext())
            .setTitle("상품 수정")
            .setView(dialogView)
            .setPositiveButton("저장") { _, _ ->
                lifecycleScope.launch {
                    runCatching {
                        RetrofitClient.api.updateSaleStatus(item.id, UpdateSaleStatusRequest(item.status))
                        Toast.makeText(requireContext(), "수정됐어요", Toast.LENGTH_SHORT).show()
                        loadSaleItems()
                    }.onFailure {
                        Toast.makeText(requireContext(), "수정에 실패했어요", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("닫기", null)
            .show()
    }

    private fun confirmCancel(item: SaleItemApiResponse) {
        AlertDialog.Builder(requireContext())
            .setTitle("상품 취소")
            .setMessage("${item.name} 상품 등록을 취소할까요?")
            .setPositiveButton("취소하기") { _, _ ->
                lifecycleScope.launch {
                    runCatching {
                        RetrofitClient.api.cancelSaleItem(item.id)
                        Toast.makeText(requireContext(), "상품이 취소됐어요", Toast.LENGTH_SHORT).show()
                        loadSaleItems()
                    }.onFailure {
                        Toast.makeText(requireContext(), "취소에 실패했어요", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("닫기", null)
            .show()
    }

    private fun emptyView(): View = android.widget.TextView(requireContext()).apply {
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
