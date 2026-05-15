package com.example.deuktemsiru_seller.ui.product

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.NumberPicker
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
    private var selectedDiscountRate = 60
    private var selectedPriceMode: String = "rate"
    private var selectedQuantity = 5
    private var pickupStartMinutes = 16 * 60 + 30
    private var pickupEndMinutes = 18 * 60
    private var selectedPickupTimeSlot = "16:30-18:00"

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
                container.addView(itemBinding.root)
            }
        }

        view.findViewById<android.widget.Button>(R.id.btn_new_menu).setOnClickListener {
            startActivity(Intent(this, MenuRegistrationActivity::class.java))
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
            30 to view.findViewById<TextView>(R.id.preset_30),
            50 to view.findViewById<TextView>(R.id.preset_50),
            60 to view.findViewById<TextView>(R.id.preset_60),
            70 to view.findViewById<TextView>(R.id.preset_70),
        )

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

        val pickerStartHour = view.findViewById<NumberPicker>(R.id.picker_start_hour)
        val pickerStartMin = view.findViewById<NumberPicker>(R.id.picker_start_minute)
        val pickerEndHour = view.findViewById<NumberPicker>(R.id.picker_end_hour)
        val pickerEndMin = view.findViewById<NumberPicker>(R.id.picker_end_minute)
        val containerStart = view.findViewById<android.widget.LinearLayout>(R.id.container_pickup_start)
        val containerEnd = view.findViewById<android.widget.LinearLayout>(R.id.container_pickup_end)

        val hours = (0..23).map { "%02d".format(it) }.toTypedArray()
        val minutes = (0..59 step 5).map { "%02d".format(it) }.toTypedArray()
        listOf(pickerStartHour, pickerEndHour).forEach { p -> p.minValue = 0; p.maxValue = 23; p.displayedValues = hours; p.wrapSelectorWheel = true }
        listOf(pickerStartMin, pickerEndMin).forEach { p -> p.minValue = 0; p.maxValue = 11; p.displayedValues = minutes; p.wrapSelectorWheel = true }

        pickerStartHour.value = pickupStartMinutes / 60
        pickerStartMin.value = (pickupStartMinutes % 60) / 5
        pickerEndHour.value = pickupEndMinutes / 60
        pickerEndMin.value = (pickupEndMinutes % 60) / 5

        fun syncPickupFromPickers() {
            pickupStartMinutes = pickerStartHour.value * 60 + pickerStartMin.value * 5
            pickupEndMinutes = pickerEndHour.value * 60 + pickerEndMin.value * 5
            selectedPickupTimeSlot = "${formatTime(pickupStartMinutes)}-${formatTime(pickupEndMinutes)}"
        }

        fun highlightContainer(active: android.widget.LinearLayout, inactive: android.widget.LinearLayout) {
            active.setBackgroundResource(R.drawable.bg_card_primary_border)
            inactive.setBackgroundResource(R.drawable.bg_rounded_muted)
        }

        pickerStartHour.setOnValueChangedListener { _, _, _ -> syncPickupFromPickers() }
        pickerStartMin.setOnValueChangedListener { _, _, _ -> syncPickupFromPickers() }
        pickerEndHour.setOnValueChangedListener { _, _, _ -> syncPickupFromPickers(); highlightContainer(containerEnd, containerStart) }
        pickerEndMin.setOnValueChangedListener { _, _, _ -> syncPickupFromPickers(); highlightContainer(containerEnd, containerStart) }
        pickerStartHour.setOnScrollListener { _, _ -> highlightContainer(containerStart, containerEnd) }
        pickerStartMin.setOnScrollListener { _, _ -> highlightContainer(containerStart, containerEnd) }
    }

    private fun onNextClicked() {
        if (currentStep == 1) {
            if (selectedMenu == null) { Toast.makeText(this, "메뉴를 선택해주세요", Toast.LENGTH_SHORT).show(); return }
            loadStep(2)
        } else {
            if (selectedPriceMode == "rate" && selectedDiscountRate !in 1..99) { Toast.makeText(this, "1~99 사이의 할인율을 입력해주세요", Toast.LENGTH_SHORT).show(); return }
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
                    val stepView = binding.stepContainer.getChildAt(0)
                    stepView?.findViewById<android.widget.EditText>(R.id.et_direct_price)
                        ?.text?.toString()?.toIntOrNull() ?: run {
                        Toast.makeText(this@ProductListingActivity, "할인가를 입력해주세요", Toast.LENGTH_SHORT).show()
                        binding.btnNext.isEnabled = true
                        binding.btnNext.text = "상품 등록"
                        return@launch
                    }
                } else {
                    menu.originalPrice * (100 - selectedDiscountRate) / 100
                }
                RetrofitClient.api.createSaleItem(
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

    private fun formatTime(minutes: Int) = "%02d:%02d".format(minutes / 60, minutes % 60)
}
