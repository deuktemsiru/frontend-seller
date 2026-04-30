package com.example.deuktemsiru_seller.ui.store

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.R
import com.example.deuktemsiru_seller.data.SessionManager
import com.example.deuktemsiru_seller.databinding.FragmentStoreBinding
import com.example.deuktemsiru_seller.network.MenuItemApiResponse
import com.example.deuktemsiru_seller.network.MenuItemUpdateRequest
import com.example.deuktemsiru_seller.network.RetrofitClient
import com.example.deuktemsiru_seller.network.UpdateStoreRequest
import kotlinx.coroutines.launch

class StoreFragment : Fragment() {

    private var _binding: FragmentStoreBinding? = null
    private val binding get() = _binding!!

    private var isEditMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentStoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val session = SessionManager(requireContext())
        if (session.isLoggedIn()) loadStore(session.sellerId)

        binding.btnEdit.setOnClickListener { enterEditMode() }
        binding.btnSave.setOnClickListener {
            if (session.isLoggedIn()) saveStore(session.sellerId)
        }
        binding.containerClosingTime.setOnClickListener {
            if (isEditMode) showTimePicker()
        }
    }

    private fun loadStore(sellerId: Long) {
        lifecycleScope.launch {
            try {
                val store = RetrofitClient.api.getMyStore(sellerId)
                binding.tvStoreEmoji.text = store.emoji
                binding.tvStoreName.text = store.name
                binding.tvCategoryStatus.text = "영업 중  •  ${categoryLabel(store.category)}"
                binding.tvRating.text = "%.1f".format(store.rating)
                binding.etAddress.setText(store.address)
                binding.etPhone.setText(store.phone)
                binding.tvClosingTime.text = store.closingTime
                renderMenus(store.menus, sellerId)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "가게 정보를 불러올 수 없어요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderMenus(menus: List<MenuItemApiResponse>, sellerId: Long) {
        binding.menuContainer.removeAllViews()
        if (menus.isEmpty()) {
            binding.menuContainer.addView(menuEmptyView())
            return
        }

        menus.forEach { menu ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(0, 12.dp, 0, 12.dp)
            }
            val summary = TextView(requireContext()).apply {
                text = "${menu.emoji} ${menu.name}\n${formatWon(menu.discountedPrice)} · 잔여 ${menu.remainingItems}개 · ${soldOutLabel(menu)}"
                textSize = 13f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val edit = TextView(requireContext()).apply {
                text = "수정"
                textSize = 13f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_dark))
                setPadding(12.dp, 8.dp, 12.dp, 8.dp)
                setOnClickListener { showMenuEditDialog(menu, sellerId) }
            }
            val delete = TextView(requireContext()).apply {
                text = "삭제"
                textSize = 13f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.danger))
                setPadding(12.dp, 8.dp, 0, 8.dp)
                setOnClickListener { confirmDeleteMenu(menu, sellerId) }
            }

            row.addView(summary)
            row.addView(edit)
            row.addView(delete)
            binding.menuContainer.addView(row)
        }
    }

    private fun menuEmptyView(): View = TextView(requireContext()).apply {
        text = "등록된 메뉴가 없어요"
        textSize = 13f
        setTextColor(ContextCompat.getColor(requireContext(), R.color.text_sub))
        setPadding(0, 16.dp, 0, 16.dp)
    }

    private fun showMenuEditDialog(menu: MenuItemApiResponse, sellerId: Long) {
        val form = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16.dp, 8.dp, 16.dp, 0)
        }
        val qty = editText("잔여 수량", menu.remainingItems.toString(), "number")
        val discount = editText("할인율", menu.discountRate.toString(), "number")
        val pickup = editText("픽업 시간", menu.pickupTimeSlot, "text")
        form.addView(qty)
        form.addView(discount)
        form.addView(pickup)

        AlertDialog.Builder(requireContext())
            .setTitle(menu.name)
            .setView(form)
            .setPositiveButton("저장") { _, _ ->
                updateMenu(
                    sellerId = sellerId,
                    menuId = menu.id,
                    remainingItems = qty.text?.toString()?.toIntOrNull() ?: menu.remainingItems,
                    discountRate = discount.text?.toString()?.toIntOrNull() ?: menu.discountRate,
                    pickupTimeSlot = pickup.text?.toString()?.trim().orEmpty().ifBlank { menu.pickupTimeSlot },
                )
            }
            .setNeutralButton(if (menu.isSoldOut) "판매 재개" else "품절 처리") { _, _ ->
                setMenuSoldOut(sellerId, menu.id, !menu.isSoldOut)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun editText(hint: String, value: String, type: String): EditText =
        EditText(requireContext()).apply {
            this.hint = hint
            setText(value)
            textSize = 14f
            inputType = if (type == "number") android.text.InputType.TYPE_CLASS_NUMBER else android.text.InputType.TYPE_CLASS_TEXT
        }

    private fun updateMenu(sellerId: Long, menuId: Long, remainingItems: Int, discountRate: Int, pickupTimeSlot: String) {
        lifecycleScope.launch {
            try {
                RetrofitClient.api.updateMenu(
                    menuItemId = menuId,
                    sellerId = sellerId,
                    req = MenuItemUpdateRequest(
                        remainingItems = remainingItems,
                        discountRate = discountRate,
                        pickupTimeSlot = pickupTimeSlot,
                    ),
                )
                Toast.makeText(requireContext(), "메뉴가 수정됐어요.", Toast.LENGTH_SHORT).show()
                loadStore(sellerId)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "메뉴 수정에 실패했어요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setMenuSoldOut(sellerId: Long, menuId: Long, isSoldOut: Boolean) {
        lifecycleScope.launch {
            try {
                RetrofitClient.api.updateMenu(
                    menuItemId = menuId,
                    sellerId = sellerId,
                    req = MenuItemUpdateRequest(isSoldOut = isSoldOut),
                )
                loadStore(sellerId)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "메뉴 상태 변경에 실패했어요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmDeleteMenu(menu: MenuItemApiResponse, sellerId: Long) {
        AlertDialog.Builder(requireContext())
            .setTitle("메뉴 삭제")
            .setMessage("${menu.name}을(를) 삭제할까요?")
            .setPositiveButton("삭제") { _, _ ->
                lifecycleScope.launch {
                    try {
                        RetrofitClient.api.deleteMenu(menu.id, sellerId)
                        Toast.makeText(requireContext(), "메뉴가 삭제됐어요.", Toast.LENGTH_SHORT).show()
                        loadStore(sellerId)
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "메뉴 삭제에 실패했어요.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun enterEditMode() {
        isEditMode = true
        binding.etAddress.isEnabled = true
        binding.etPhone.isEnabled = true
        binding.etAddress.setTextColor(ContextCompat.getColor(requireContext(), R.color.text))
        binding.etPhone.setTextColor(ContextCompat.getColor(requireContext(), R.color.text))
        binding.tvClosingTimeArrow.visibility = View.VISIBLE
        binding.btnEdit.visibility = View.GONE
        binding.btnSave.visibility = View.VISIBLE

        binding.etAddress.requestFocus()
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm.showSoftInput(binding.etAddress, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun exitEditMode() {
        isEditMode = false
        binding.etAddress.isEnabled = false
        binding.etPhone.isEnabled = false
        binding.tvClosingTimeArrow.visibility = View.GONE
        binding.btnEdit.visibility = View.VISIBLE
        binding.btnSave.visibility = View.GONE

        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun saveStore(sellerId: Long) {
        val address = binding.etAddress.text?.toString()?.trim() ?: ""
        val phone = binding.etPhone.text?.toString()?.trim() ?: ""
        val closingTime = binding.tvClosingTime.text?.toString() ?: ""

        if (address.isEmpty()) {
            binding.etAddress.error = "주소를 입력해주세요"
            return
        }

        lifecycleScope.launch {
            try {
                binding.btnSave.isEnabled = false
                val store = RetrofitClient.api.updateStore(
                    sellerId = sellerId,
                    req = UpdateStoreRequest(address = address, phone = phone, closingTime = closingTime),
                )
                binding.tvStoreName.text = store.name
                binding.etAddress.setText(store.address)
                binding.etPhone.setText(store.phone)
                binding.tvClosingTime.text = store.closingTime
                exitEditMode()
                Toast.makeText(requireContext(), "가게 정보가 저장되었어요.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "저장에 실패했어요.", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnSave.isEnabled = true
            }
        }
    }

    private fun showTimePicker() {
        val current = binding.tvClosingTime.text?.toString() ?: "21:00"
        val parts = current.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 21
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        TimePickerDialog(requireContext(), { _, h, m ->
            binding.tvClosingTime.text = "%02d:%02d".format(h, m)
        }, hour, minute, true).show()
    }

    private fun categoryLabel(category: String) = when (category.uppercase()) {
        "BAKERY" -> "베이커리"
        "LUNCHBOX" -> "도시락"
        "SALAD" -> "샐러드"
        "CAFE" -> "카페"
        else -> category
    }

    private fun soldOutLabel(menu: MenuItemApiResponse): String =
        if (menu.isSoldOut) "품절" else menu.pickupTimeSlot

    private fun formatWon(price: Int): String = "%,d원".format(price)

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
