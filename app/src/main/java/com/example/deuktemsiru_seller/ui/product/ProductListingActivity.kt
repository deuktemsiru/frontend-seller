package com.example.deuktemsiru_seller.ui.product

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.app.TimePickerDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.R
import com.example.deuktemsiru_seller.data.SessionManager
import com.example.deuktemsiru_seller.databinding.ActivityProductListingBinding
import com.example.deuktemsiru_seller.databinding.ItemChooseMenuBinding
import com.example.deuktemsiru_seller.network.MenuItemApiResponse
import com.example.deuktemsiru_seller.network.RetrofitClient
import com.example.deuktemsiru_seller.network.SaleItemCreateRequest
import com.example.deuktemsiru_seller.ui.registration.MenuRegistrationActivity
import kotlinx.coroutines.launch

class ProductListingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductListingBinding
    private lateinit var session: SessionManager
    private var currentStep = 1
    private var menus: List<MenuItemApiResponse> = emptyList()
    private var selectedMenu: MenuItemApiResponse? = null
    private var selectedDiscountRate = 20
    private var selectedPriceMode: String = "rate"
    private var selectedQuantity = 5
    private var pickupStartMinutes = 16 * 60 + 30
    private var pickupEndMinutes = 18 * 60
    private var reloadMenusOnResume = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductListingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        session = SessionManager(this)
        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.btnNext.setOnClickListener { onNextClicked() }
        binding.btnPrev.setOnClickListener { loadStep(1) }
        loadMenus()
    }

    override fun onResume() {
        super.onResume()
        if (reloadMenusOnResume) {
            reloadMenusOnResume = false
            loadMenus()
        }
    }

    private fun loadMenus() {
        lifecycleScope.launch {
            try {
                menus = RetrofitClient.api.getMenus().data ?: emptyList()
                loadStep(1)
            } catch (e: Exception) {
                Toast.makeText(this@ProductListingActivity, "메뉴를 불러올 수 없어요", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun loadStep(step: Int) {
        currentStep = step
        binding.progress1.setBackgroundResource(if (step >= 1) R.drawable.bg_progress_active else R.drawable.bg_progress_inactive)
        binding.progress2.setBackgroundResource(if (step >= 2) R.drawable.bg_progress_active else R.drawable.bg_progress_inactive)
        binding.tvStepLabel.text = "$step/2"
        binding.stepContainer.removeAllViews()

        if (step == 1) {
            binding.btnPrev.visibility = View.GONE
            binding.btnNext.text = "다음"
            val stepView = LayoutInflater.from(this).inflate(R.layout.step_choose_menu, binding.stepContainer, false)
            binding.stepContainer.addView(stepView)
            setupStep1(stepView)
        } else {
            binding.btnPrev.visibility = View.VISIBLE
            binding.btnNext.text = "상품 등록"
            val stepView = LayoutInflater.from(this).inflate(R.layout.step_sale_details, binding.stepContainer, false)
            binding.stepContainer.addView(stepView)
            setupStep2(stepView)
        }
    }

    private fun setupStep1(view: View) {
        val container = view.findViewById<android.widget.LinearLayout>(R.id.menu_list_container)
        container.removeAllViews()

        if (menus.isEmpty()) {
            val empty = TextView(this).apply {
                text = "등록된 메뉴가 없어요. 먼저 메뉴를 추가해주세요."
                textSize = 13f
                setTextColor(getColor(R.color.text_sub))
                setPadding(0, 16, 0, 16)
            }
            container.addView(empty)
        } else {
            menus.forEach { menu ->
                val itemBinding = ItemChooseMenuBinding.inflate(LayoutInflater.from(this), container, false)
                itemBinding.tvMenuEmoji.text = menu.emoji
                itemBinding.tvMenuName.text = menu.name
                itemBinding.tvMenuPrice.text = "%,d원".format(menu.originalPrice)
                updateMenuSelection(itemBinding, menu == selectedMenu)
                itemBinding.root.setOnClickListener {
                    selectedMenu = if (selectedMenu?.id == menu.id) null else menu
                    menus.forEachIndexed { i, _ ->
                        val child = container.getChildAt(i)
                        val childBinding = ItemChooseMenuBinding.bind(child)
                        updateMenuSelection(childBinding, selectedMenu?.id == menus[i].id)
                    }
                }
                itemBinding.root.setOnLongClickListener {
                    confirmDeleteMenu(menu)
                    true
                }
                itemBinding.btnDeleteMenu.setOnClickListener {
                    confirmDeleteMenu(menu)
                }
                container.addView(itemBinding.root)
            }
        }

        view.findViewById<TextView>(R.id.btn_new_menu).setOnClickListener {
            reloadMenusOnResume = true
            startActivity(
                Intent(this, MenuRegistrationActivity::class.java)
                    .putExtra(MenuRegistrationActivity.EXTRA_MENU_ONLY, true)
            )
        }
    }

    private fun updateMenuSelection(itemBinding: ItemChooseMenuBinding, selected: Boolean) {
        itemBinding.tvCheck.text = if (selected) "●" else "○"
        itemBinding.tvCheck.setTextColor(getColor(if (selected) R.color.primary else R.color.text_muted))
        itemBinding.root.setBackgroundResource(if (selected) R.drawable.bg_menu_card_selected else R.drawable.bg_menu_card_normal)
    }

    private fun setupStep2(view: View) {
        val menu = selectedMenu ?: return
        view.findViewById<TextView>(R.id.tv_selected_emoji).text = menu.emoji
        view.findViewById<TextView>(R.id.tv_selected_name).text = menu.name
        view.findViewById<TextView>(R.id.tv_selected_price).text = "정상가 %,d원".format(menu.originalPrice)

        val layoutRateMode = view.findViewById<android.widget.LinearLayout>(R.id.layout_rate_mode)
        val layoutPriceMode = view.findViewById<android.widget.LinearLayout>(R.id.layout_price_mode)
        val btnModeRate = view.findViewById<TextView>(R.id.btn_mode_rate)
        val btnModePrice = view.findViewById<TextView>(R.id.btn_mode_price)

        fun updateModeUI() {
            if (selectedPriceMode == "rate") {
                layoutRateMode.visibility = android.view.View.VISIBLE
                layoutPriceMode.visibility = android.view.View.GONE
                btnModeRate.setBackgroundResource(R.drawable.bg_discount_preset_selected)
                btnModeRate.setTextColor(getColor(R.color.white))
                btnModePrice.setBackgroundResource(R.drawable.bg_discount_preset_normal)
                btnModePrice.setTextColor(getColor(R.color.text_sub))
            } else {
                layoutRateMode.visibility = android.view.View.GONE
                layoutPriceMode.visibility = android.view.View.VISIBLE
                btnModePrice.setBackgroundResource(R.drawable.bg_discount_preset_selected)
                btnModePrice.setTextColor(getColor(R.color.white))
                btnModeRate.setBackgroundResource(R.drawable.bg_discount_preset_normal)
                btnModeRate.setTextColor(getColor(R.color.text_sub))
            }
        }

        updateModeUI()
        btnModeRate.setOnClickListener { selectedPriceMode = "rate"; updateModeUI() }
        btnModePrice.setOnClickListener { selectedPriceMode = "price"; updateModeUI() }

        val etDiscount = view.findViewById<android.widget.EditText>(R.id.et_discount_rate)
        val tvDiscounted = view.findViewById<TextView>(R.id.tv_discounted_price)
        val tvQty = view.findViewById<TextView>(R.id.tv_quantity)

        val presetIds = mapOf(
            10 to view.findViewById<TextView>(R.id.preset_30),
            20 to view.findViewById<TextView>(R.id.preset_50),
            30 to view.findViewById<TextView>(R.id.preset_60),
            40 to view.findViewById<TextView>(R.id.preset_70),
        )
        presetIds.forEach { (discount, presetView) -> presetView.text = "$discount%" }

        fun refreshDiscount() {
            val rate = etDiscount.text?.toString()?.toIntOrNull() ?: 0
            selectedDiscountRate = rate
            val discounted = menu.originalPrice * (100 - rate) / 100
            tvDiscounted.text = "%,d원".format(discounted)
            presetIds.forEach { (d, tv) ->
                val sel = d == rate
                tv.setBackgroundResource(if (sel) R.drawable.bg_discount_preset_selected else R.drawable.bg_discount_preset_normal)
                tv.setTextColor(getColor(if (sel) R.color.white else R.color.text_sub))
            }
        }

        etDiscount.setText(selectedDiscountRate.toString())
        refreshDiscount()
        etDiscount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = refreshDiscount()
            override fun afterTextChanged(s: Editable?) = Unit
        })
        presetIds.forEach { (d, tv) ->
            tv.setOnClickListener {
                selectedDiscountRate = d
                etDiscount.setText(d.toString())
                etDiscount.setSelection(etDiscount.text?.length ?: 0)
            }
        }

        tvQty.text = selectedQuantity.toString()
        view.findViewById<View>(R.id.btn_qty_minus).setOnClickListener {
            if (selectedQuantity > 1) { selectedQuantity--; tvQty.text = selectedQuantity.toString() }
        }
        view.findViewById<View>(R.id.btn_qty_plus).setOnClickListener {
            if (selectedQuantity < 99) { selectedQuantity++; tvQty.text = selectedQuantity.toString() }
        }

        val containerStart = view.findViewById<android.widget.LinearLayout>(R.id.container_pickup_start)
        val containerEnd = view.findViewById<android.widget.LinearLayout>(R.id.container_pickup_end)
        val tvStart = view.findViewById<TextView>(R.id.tv_pickup_start)
        val tvEnd = view.findViewById<TextView>(R.id.tv_pickup_end)
        val chip30Min = view.findViewById<TextView>(R.id.chip_30min)
        val chip1Hour = view.findViewById<TextView>(R.id.chip_1hour)
        val chip2Hour = view.findViewById<TextView>(R.id.chip_2hour)

        fun highlightContainer(active: android.widget.LinearLayout, inactive: android.widget.LinearLayout) {
            active.setBackgroundResource(R.drawable.bg_card_primary_border)
            inactive.setBackgroundResource(R.drawable.bg_rounded_muted)
        }

        fun refreshPickupViews() {
            tvStart.text = formatTime(pickupStartMinutes)
            tvEnd.text = formatTime(pickupEndMinutes)
        }

        fun showTimePicker(initialMinutes: Int, onPicked: (Int) -> Unit) {
            TimePickerDialog(this, { _, hour, minute ->
                onPicked(hour * 60 + minute)
                refreshPickupViews()
            }, initialMinutes / 60, initialMinutes % 60, true).show()
        }

        fun setDuration(minutes: Int) {
            pickupEndMinutes = (pickupStartMinutes + minutes).coerceAtMost(23 * 60 + 59)
            highlightContainer(containerEnd, containerStart)
            refreshPickupViews()
        }

        containerStart.setOnClickListener {
            highlightContainer(containerStart, containerEnd)
            showTimePicker(pickupStartMinutes) { picked ->
                pickupStartMinutes = picked
                if (pickupEndMinutes <= pickupStartMinutes) pickupEndMinutes = (pickupStartMinutes + 60).coerceAtMost(23 * 60 + 59)
            }
        }
        containerEnd.setOnClickListener {
            highlightContainer(containerEnd, containerStart)
            showTimePicker(pickupEndMinutes) { picked ->
                if (picked <= pickupStartMinutes) {
                    Toast.makeText(this, "마감 시간은 시작 시간보다 늦어야 해요.", Toast.LENGTH_SHORT).show()
                } else {
                    pickupEndMinutes = picked
                }
            }
        }
        chip30Min.setOnClickListener { setDuration(30) }
        chip1Hour.setOnClickListener { setDuration(60) }
        chip2Hour.setOnClickListener { setDuration(120) }
        refreshPickupViews()
    }

    private fun onNextClicked() {
        if (currentStep == 1) {
            if (selectedMenu == null) { Toast.makeText(this, "메뉴를 선택해주세요", Toast.LENGTH_SHORT).show(); return }
            loadStep(2)
        } else {
            if (selectedPriceMode == "rate" && selectedDiscountRate !in 1..99) { Toast.makeText(this, "1~99 사이의 할인율을 입력해주세요", Toast.LENGTH_SHORT).show(); return }
            if (pickupEndMinutes <= pickupStartMinutes) { Toast.makeText(this, "픽업 종료 시간이 시작 시간보다 늦어야 해요.", Toast.LENGTH_SHORT).show(); return }
            registerProduct()
        }
    }

    private fun registerProduct() {
        val menu = selectedMenu ?: return
        binding.btnNext.isEnabled = false
        binding.btnNext.text = "등록 중..."
        lifecycleScope.launch {
            try {
                val discountPrice = if (selectedPriceMode == "price") {
                    val stepView = binding.stepContainer.getChildAt(0) ?: return@launch
                    val price = stepView.findViewById<android.widget.EditText>(R.id.et_direct_price)
                        ?.text?.toString()?.toIntOrNull() ?: run {
                        Toast.makeText(this@ProductListingActivity, "할인가를 입력해주세요", Toast.LENGTH_SHORT).show()
                        binding.btnNext.isEnabled = true
                        binding.btnNext.text = "상품 등록"
                        return@launch
                    }
                    if (price <= 0 || price >= menu.originalPrice) {
                        Toast.makeText(this@ProductListingActivity, "할인가는 정가보다 낮은 1원 이상이어야 해요.", Toast.LENGTH_SHORT).show()
                        binding.btnNext.isEnabled = true
                        binding.btnNext.text = "상품 등록"
                        return@launch
                    }
                    price
                } else {
                    menu.originalPrice * (100 - selectedDiscountRate) / 100
                }
                val response = RetrofitClient.api.createSaleItem(
                    SaleItemCreateRequest(
                        menuItemId = menu.id,
                        name = menu.name,
                        discountPrice = discountPrice,
                        originalPrice = menu.originalPrice,
                        quantityTotal = selectedQuantity,
                        pickupStart = formatTime(pickupStartMinutes),
                        pickupEnd = formatTime(pickupEndMinutes),
                        availableDate = java.time.LocalDate.now().toString(),
                    ),
                )
                if (response.code !in 200..299 || response.data == null) {
                    throw IllegalStateException(response.message)
                }
                Toast.makeText(this@ProductListingActivity, "상품이 등록됐어요!", Toast.LENGTH_SHORT).show()
                val intent = android.content.Intent(this@ProductListingActivity, com.example.deuktemsiru_seller.MainActivity::class.java).apply {
                    flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("navigate_to_product", true)
                }
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@ProductListingActivity, "등록에 실패했어요", Toast.LENGTH_SHORT).show()
                binding.btnNext.isEnabled = true
                binding.btnNext.text = "상품 등록"
            }
        }
    }

    private fun confirmDeleteMenu(menu: MenuItemApiResponse) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("메뉴 삭제")
            .setMessage("${menu.name}을(를) 삭제할까요?")
            .setPositiveButton("삭제") { _, _ ->
                lifecycleScope.launch {
                    runCatching {
                        RetrofitClient.api.deleteMenu(menu.id)
                    }.onSuccess {
                        if (selectedMenu?.id == menu.id) selectedMenu = null
                        Toast.makeText(this@ProductListingActivity, "메뉴가 삭제됐어요", Toast.LENGTH_SHORT).show()
                        loadMenus()
                    }.onFailure {
                        Toast.makeText(this@ProductListingActivity, "메뉴 삭제에 실패했어요", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun formatTime(minutes: Int) = "%02d:%02d".format(minutes / 60, minutes % 60)
}
