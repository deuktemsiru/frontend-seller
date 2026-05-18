package com.example.deuktemsiru_seller.ui.product

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.deuktemsiru_seller.network.SaleStatus
import com.example.deuktemsiru_seller.network.UpdateSaleItemRequest
import com.example.deuktemsiru_seller.network.UpdateSaleStatusRequest
import com.example.deuktemsiru_seller.ui.auth.LoginActivity
import com.example.deuktemsiru_seller.util.emptyTextView
import com.example.deuktemsiru_seller.util.renderChildren
import com.example.deuktemsiru_seller.util.toWon
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ProductFragment : Fragment() {

    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var session: SessionManager

    private companion object {
        const val TAG = "ProductFragment"
    }

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
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                val items = RetrofitClient.api.getSaleItems().data ?: emptyList()
                renderItems(items)
            }.onFailure { error ->
                if (handleAuthFailure(error)) return@onFailure
                if (error is HttpException && error.code() == 404) {
                    renderItems(emptyList())
                    return@onFailure
                }
                Log.e(TAG, "Failed to load sale items", error)
                Toast.makeText(requireContext(), "상품 목록을 불러올 수 없어요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleAuthFailure(error: Throwable): Boolean {
        if (error !is HttpException || error.code() !in listOf(401, 403)) return false
        session.clear()
        Toast.makeText(requireContext(), "판매자 계정으로 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
        startActivity(
            Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        return true
    }

    private fun renderItems(items: List<SaleItemApiResponse>) {
        binding.saleItemsContainer.renderChildren(
            items = items,
            emptyView = ::emptyView,
        ) { item ->
            val itemBinding = ItemSaleItemBinding.inflate(layoutInflater, binding.saleItemsContainer, false)
            itemBinding.tvItemEmoji.text = item.emoji
            itemBinding.tvItemName.text = item.name
            itemBinding.tvItemDetail.text =
                "${item.discountedPrice.toWon()} · 잔여 ${item.remainingItems}/${item.totalItems}개 · ${item.displayPickupTime}"

            val (statusText, badgeBackground, badgeTextColor) = when (item.saleStatus) {
                SaleStatus.Available -> Triple("● 판매중", R.drawable.bg_status_available, 0xFF2E7D32.toInt())
                SaleStatus.SoldOut -> Triple("● 품절", R.drawable.bg_status_soldout, 0xFFE65100.toInt())
                SaleStatus.Expired -> Triple("종료", R.drawable.bg_status_expired, 0xFF616161.toInt())
                else        -> Triple(item.status, R.drawable.bg_status_expired, 0xFF616161.toInt())
            }
            itemBinding.tvItemStatus.text = statusText
            itemBinding.tvItemStatus.setTextColor(badgeTextColor)
            itemBinding.tvItemStatus.setBackgroundResource(badgeBackground)

            fun styleButton(btn: TextView, isActive: Boolean) {
                val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)
                if (isActive) {
                    btn.setBackgroundResource(R.drawable.bg_button_primary)
                    btn.setTextColor(0xFFFFFFFF.toInt())
                } else {
                    btn.setBackgroundResource(R.drawable.bg_button_outline)
                    btn.setTextColor(primaryColor)
                }
            }

            itemBinding.btnEdit.setOnClickListener { showEditDialog(item) }
            itemBinding.btnStatusAvailable.setOnClickListener { updateStatus(item.id, SaleStatus.Available.apiValue) }
            itemBinding.btnStatusSoldout.setOnClickListener { updateStatus(item.id, SaleStatus.SoldOut.apiValue) }
            val isFinal = item.saleStatus.isFinal
            itemBinding.btnCancel.text = if (isFinal) "삭제" else "취소"
            itemBinding.btnCancel.setOnClickListener { confirmCancel(item) }
            styleButton(itemBinding.btnStatusAvailable, item.saleStatus == SaleStatus.Available)
            styleButton(itemBinding.btnStatusSoldout, item.saleStatus == SaleStatus.SoldOut)
            styleButton(itemBinding.btnCancel, false)
            itemBinding.btnStatusAvailable.isEnabled = !isFinal
            itemBinding.btnStatusSoldout.isEnabled = !isFinal
            itemBinding.btnCancel.isEnabled = true
            itemBinding.btnStatusAvailable.alpha = if (isFinal) 0.35f else 1f
            itemBinding.btnStatusSoldout.alpha = if (isFinal) 0.35f else 1f
            itemBinding.btnCancel.alpha = 1f

            itemBinding.root
        }
    }

    private fun updateStatus(itemId: Long, status: String) {
        viewLifecycleOwner.lifecycleScope.launch {
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
                val newPrice = etPrice.text?.toString()?.toIntOrNull()
                val newQty = etQty.text?.toString()?.toIntOrNull()
                if (newPrice == null || newPrice <= 0) {
                    Toast.makeText(requireContext(), "올바른 가격을 입력해주세요", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (newQty == null || newQty < 0) {
                    Toast.makeText(requireContext(), "올바른 수량을 입력해주세요", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewLifecycleOwner.lifecycleScope.launch {
                    runCatching {
                        RetrofitClient.api.updateSaleItem(
                            item.id,
                            UpdateSaleItemRequest(discountPrice = newPrice, quantityRemaining = newQty),
                        )
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
        val isFinal = item.saleStatus.isFinal
        val actionLabel = if (isFinal) "삭제" else "취소"
        AlertDialog.Builder(requireContext())
            .setTitle("상품 $actionLabel")
            .setMessage("${item.name} 상품을 ${actionLabel}할까요?")
            .setPositiveButton("${actionLabel}하기") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    runCatching {
                        RetrofitClient.api.cancelSaleItem(item.id)
                        Toast.makeText(requireContext(), "상품이 ${actionLabel}됐어요", Toast.LENGTH_SHORT).show()
                        loadSaleItems()
                    }.onFailure {
                        Toast.makeText(requireContext(), "${actionLabel}에 실패했어요", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("닫기", null)
            .show()
    }

    private fun emptyView(): View = requireContext().emptyTextView(
        message = "오늘 등록된 상품이 없어요\n아래 '+ 상품 등록' 버튼을 눌러 등록해보세요",
        verticalPaddingDp = 60,
        centered = true,
    ).apply {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
