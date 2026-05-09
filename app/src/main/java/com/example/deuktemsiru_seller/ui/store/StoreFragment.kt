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
        if (session.isLoggedIn()) {
            loadStore()
            loadMenus()
        }

        binding.btnEdit.setOnClickListener { enterEditMode() }
        binding.btnSave.setOnClickListener { if (session.isLoggedIn()) saveStore() }
        binding.containerClosingTime.setOnClickListener { if (isEditMode) showTimePicker() }
    }

    // ── 가게 정보 ─────────────────────────────────────────────

    private fun loadStore() {
        lifecycleScope.launch {
            runCatching {
                val store = RetrofitClient.api.getMyStore().data ?: return@runCatching
                binding.tvStoreEmoji.text = "🏪"
                binding.tvStoreName.text = store.name
                binding.tvCategoryStatus.text = "영업 중  •  ${categoryLabel(store.category)}"
                binding.tvRating.text = "—"
                binding.etAddress.setText(store.address)
                binding.etPhone.setText(store.phone)
                binding.tvClosingTime.text = store.closingTime
            }.onFailure {
                Toast.makeText(requireContext(), "가게 정보를 불러올 수 없어요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveStore() {
        val address = binding.etAddress.text?.toString()?.trim() ?: ""
        val phone = binding.etPhone.text?.toString()?.trim() ?: ""
        val closingTime = binding.tvClosingTime.text?.toString() ?: ""

        if (address.isEmpty()) {
            binding.etAddress.error = "주소를 입력해주세요"
            return
        }

        lifecycleScope.launch {
            runCatching {
                binding.btnSave.isEnabled = false
                val store = RetrofitClient.api.updateStore(
                    UpdateStoreRequest(address = address, phone = phone, closingTime = closingTime)
                ).data ?: return@runCatching
                binding.tvStoreName.text = store.name
                binding.etAddress.setText(store.address)
                binding.etPhone.setText(store.phone)
                binding.tvClosingTime.text = store.closingTime
                exitEditMode()
                Toast.makeText(requireContext(), "가게 정보가 저장되었어요.", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(requireContext(), "저장에 실패했어요.", Toast.LENGTH_SHORT).show()
            }
            binding.btnSave.isEnabled = true
        }
    }

    // ── 메뉴 목록 ─────────────────────────────────────────────

    private fun loadMenus() {
        lifecycleScope.launch {
            runCatching {
                val menus = RetrofitClient.api.getMenus().data ?: emptyList()
                renderMenus(menus)
            }.onFailure {
                renderMenus(emptyList())
            }
        }
    }

    private fun renderMenus(menus: List<MenuItemApiResponse>) {
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
                text = "${menu.emoji} ${menu.name}\n%,d원".format(menu.originalPrice)
                textSize = 13f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val edit = TextView(requireContext()).apply {
                text = "수정"
                textSize = 13f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_dark))
                setPadding(12.dp, 8.dp, 12.dp, 8.dp)
                setOnClickListener { showMenuEditDialog(menu) }
            }
            val delete = TextView(requireContext()).apply {
                text = "삭제"
                textSize = 13f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.danger))
                setPadding(12.dp, 8.dp, 0, 8.dp)
                setOnClickListener { confirmDeleteMenu(menu) }
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

    private fun showMenuEditDialog(menu: MenuItemApiResponse) {
        val form = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16.dp, 8.dp, 16.dp, 0)
        }
        val nameField = editText("메뉴명", menu.name, "text")
        val priceField = editText("정상가", menu.originalPrice.toString(), "number")
        form.addView(nameField)
        form.addView(priceField)

        AlertDialog.Builder(requireContext())
            .setTitle(menu.name)
            .setView(form)
            .setPositiveButton("저장") { _, _ ->
                val name = nameField.text?.toString()?.trim().orEmpty().ifBlank { menu.name }
                val price = priceField.text?.toString()?.toIntOrNull() ?: menu.originalPrice
                updateMenu(menu.id, MenuItemUpdateRequest(name = name, originalPrice = price))
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun editText(hint: String, value: String, type: String): EditText =
        EditText(requireContext()).apply {
            this.hint = hint
            setText(value)
            textSize = 14f
            inputType = if (type == "number") android.text.InputType.TYPE_CLASS_NUMBER
                        else android.text.InputType.TYPE_CLASS_TEXT
        }

    private fun updateMenu(menuId: Long, req: MenuItemUpdateRequest) {
        lifecycleScope.launch {
            runCatching {
                RetrofitClient.api.updateMenu(menuId, req)
                Toast.makeText(requireContext(), "메뉴가 수정됐어요.", Toast.LENGTH_SHORT).show()
                loadMenus()
            }.onFailure {
                Toast.makeText(requireContext(), "메뉴 수정에 실패했어요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmDeleteMenu(menu: MenuItemApiResponse) {
        AlertDialog.Builder(requireContext())
            .setTitle("메뉴 삭제")
            .setMessage("${menu.name}을(를) 삭제할까요?")
            .setPositiveButton("삭제") { _, _ ->
                lifecycleScope.launch {
                    runCatching {
                        RetrofitClient.api.deleteMenu(menu.id)
                        Toast.makeText(requireContext(), "메뉴가 삭제됐어요.", Toast.LENGTH_SHORT).show()
                        loadMenus()
                    }.onFailure {
                        Toast.makeText(requireContext(), "메뉴 삭제에 실패했어요.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // ── 편집 모드 ─────────────────────────────────────────────

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

    private fun showTimePicker() {
        val current = binding.tvClosingTime.text?.toString() ?: "21:00"
        val parts = current.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 21
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        TimePickerDialog(requireContext(), { _, h, m ->
            binding.tvClosingTime.text = "%02d:%02d".format(h, m)
        }, hour, minute, true).show()
    }

    // ── 유틸 ──────────────────────────────────────────────────

    private fun categoryLabel(category: String) = when (category.uppercase()) {
        "BAKERY" -> "베이커리"
        "LUNCHBOX" -> "도시락"
        "SALAD" -> "샐러드"
        "CAFE" -> "카페"
        else -> category
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
