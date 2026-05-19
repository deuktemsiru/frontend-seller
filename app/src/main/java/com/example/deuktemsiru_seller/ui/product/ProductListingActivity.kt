package com.example.deuktemsiru_seller.ui.product

import android.content.Intent
import android.os.Bundle
import android.app.TimePickerDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
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
import com.example.deuktemsiru_seller.util.PickupTimeState
import com.example.deuktemsiru_seller.util.bindDiscountPresets
import com.example.deuktemsiru_seller.util.discountPresetViews
import com.example.deuktemsiru_seller.util.emptyTextView
import com.example.deuktemsiru_seller.util.inflateInto
import com.example.deuktemsiru_seller.util.renderChildren
import com.example.deuktemsiru_seller.util.refreshDiscountPresets
import com.example.deuktemsiru_seller.util.setSelectedChip
import com.example.deuktemsiru_seller.util.setupQuantityControls
import com.example.deuktemsiru_seller.util.toWon
import com.example.deuktemsiru_seller.util.toast
import com.example.deuktemsiru_seller.util.visibleIf
import kotlinx.coroutines.launch
import java.time.LocalDate

class ProductListingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductListingBinding
    private lateinit var session: SessionManager
    private var currentStep = 1
    private var menus: List<MenuItemApiResponse> = emptyList()
    private var selectedMenu: MenuItemApiResponse? = null
    private var selectedDiscountRate = 20
    private var selectedPriceMode: String = "rate"
    private var selectedQuantity = 5
    private val pickupTime = PickupTimeState()
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
                toast("메뉴를 불러올 수 없어요")
                finish()
            }
        }
    }

    private fun loadStep(step: Int) {
        currentStep = step
        listOf(binding.progress1, binding.progress2).forEachIndexed { index, progress ->
            progress.setBackgroundResource(if (step > index) R.drawable.bg_progress_active else R.drawable.bg_progress_inactive)
        }
        binding.tvStepLabel.text = "$step/2"
        binding.stepContainer.removeAllViews()

        val (layoutRes, nextText, setup) = when (step) {
            1 -> Triple(R.layout.step_choose_menu, "다음", ::setupStep1)
            else -> Triple(R.layout.step_sale_details, "상품 등록", ::setupStep2)
        }
        binding.btnPrev.visibleIf(step > 1)
        binding.btnNext.text = nextText
        setup(layoutInflater.inflateInto(layoutRes, binding.stepContainer))
    }

    private fun setupStep1(view: View) {
        val container = view.findViewById<android.widget.LinearLayout>(R.id.menu_list_container)
        container.renderChildren(
            items = menus,
            emptyView = { emptyTextView("등록된 메뉴가 없어요. 먼저 메뉴를 추가해주세요.") },
        ) { menu ->
                val itemBinding = ItemChooseMenuBinding.inflate(LayoutInflater.from(this), container, false)
                itemBinding.tvMenuEmoji.text = menu.displayEmoji
                itemBinding.tvMenuName.text = menu.name
                itemBinding.tvMenuPrice.text = menu.originalPrice.toWon()
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
                itemBinding.root
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
        view.findViewById<TextView>(R.id.tv_selected_emoji).text = menu.displayEmoji
        view.findViewById<TextView>(R.id.tv_selected_name).text = menu.name
        view.findViewById<TextView>(R.id.tv_selected_price).text = "정상가 ${menu.originalPrice.toWon()}"

        val layoutRateMode = view.findViewById<android.widget.LinearLayout>(R.id.layout_rate_mode)
        val layoutPriceMode = view.findViewById<android.widget.LinearLayout>(R.id.layout_price_mode)
        val btnModeRate = view.findViewById<TextView>(R.id.btn_mode_rate)
        val btnModePrice = view.findViewById<TextView>(R.id.btn_mode_price)

        fun updateModeUI() {
            if (selectedPriceMode == "rate") {
                layoutRateMode.visibleIf(true)
                layoutPriceMode.visibleIf(false)
            } else {
                layoutRateMode.visibleIf(false)
                layoutPriceMode.visibleIf(true)
            }
            btnModeRate.setSelectedChip(selectedPriceMode == "rate")
            btnModePrice.setSelectedChip(selectedPriceMode == "price")
        }

        updateModeUI()
        btnModeRate.setOnClickListener { selectedPriceMode = "rate"; updateModeUI() }
        btnModePrice.setOnClickListener { selectedPriceMode = "price"; updateModeUI() }

        val etDiscount = view.findViewById<android.widget.EditText>(R.id.et_discount_rate)
        val tvDiscounted = view.findViewById<TextView>(R.id.tv_discounted_price)
        val presetIds = discountPresetViews(view)

        fun refreshDiscount() {
            val rate = etDiscount.text?.toString()?.toIntOrNull() ?: 0
            selectedDiscountRate = rate
            val discounted = menu.originalPrice * (100 - rate) / 100
            tvDiscounted.text = discounted.toWon()
            refreshDiscountPresets(presetIds, rate)
        }

        etDiscount.setText(selectedDiscountRate.toString())
        etDiscount.bindDiscountPresets(
            presets = presetIds,
            selectedRate = { selectedDiscountRate },
            onRateChanged = { selectedDiscountRate = it },
            refresh = ::refreshDiscount,
        )

        setupQuantityControls(view, selectedQuantity) { selectedQuantity = it }

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
            tvStart.text = pickupTime.startLabel
            tvEnd.text = pickupTime.endLabel
        }

        fun showTimePicker(initialMinutes: Int, onPicked: (Int) -> Unit) {
            TimePickerDialog(this, { _, hour, minute ->
                onPicked(hour * 60 + minute)
                refreshPickupViews()
            }, initialMinutes / 60, initialMinutes % 60, true).show()
        }

        fun setDuration(minutes: Int) {
            pickupTime.setDuration(minutes)
            highlightContainer(containerEnd, containerStart)
            refreshPickupViews()
        }

        containerStart.setOnClickListener {
            highlightContainer(containerStart, containerEnd)
            showTimePicker(pickupTime.startMinutes) { picked ->
                pickupTime.startMinutes = picked
                pickupTime.ensureEndAfterStart(minDurationMinutes = 60)
            }
        }
        containerEnd.setOnClickListener {
            highlightContainer(containerEnd, containerStart)
            showTimePicker(pickupTime.endMinutes) { picked ->
                if (picked <= pickupTime.startMinutes) {
                    toast("마감 시간은 시작 시간보다 늦어야 해요.")
                } else {
                    pickupTime.endMinutes = picked
                }
            }
        }
        chip30Min.setOnClickListener { setDuration(30) }
        chip1Hour.setOnClickListener { setDuration(60) }
        chip2Hour.setOnClickListener { setDuration(120) }
        refreshPickupViews()
    }

    private fun onNextClicked() {
        when {
            currentStep == 1 && selectedMenu == null -> toast("메뉴를 선택해주세요")
            currentStep == 1 -> loadStep(2)
            validateSaleDetails() -> registerProduct()
        }
    }

    private fun validateSaleDetails(): Boolean {
        if (selectedPriceMode == "rate" && selectedDiscountRate !in 1..99) {
            toast("1~99 사이의 할인율을 입력해주세요")
            return false
        }
        if (pickupTime.endMinutes <= pickupTime.startMinutes) {
            toast("픽업 종료 시간이 시작 시간보다 늦어야 해요.")
            return false
        }
        return true
    }

    private fun registerProduct() {
        val menu = selectedMenu ?: return
        setSubmitting(true)
        lifecycleScope.launch {
            try {
                val discountPrice = resolveDiscountPrice(menu) ?: return@launch
                val response = RetrofitClient.api.createSaleItem(
                    SaleItemCreateRequest(
                        menuItemId = menu.id,
                        name = menu.name,
                        discountPrice = discountPrice,
                        originalPrice = menu.originalPrice,
                        quantityTotal = selectedQuantity,
                        pickupStart = pickupTime.startLabel,
                        pickupEnd = pickupTime.endLabel,
                        availableDate = LocalDate.now().toString(),
                    ),
                )
                if (response.code !in 200..299 || response.data == null) {
                    throw IllegalStateException(response.message)
                }
                toast("상품이 등록됐어요!")
                val intent = android.content.Intent(this@ProductListingActivity, com.example.deuktemsiru_seller.MainActivity::class.java).apply {
                    flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("navigate_to_product", true)
                }
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                toast("등록에 실패했어요")
                setSubmitting(false)
            }
        }
    }

    private fun resolveDiscountPrice(menu: MenuItemApiResponse): Int? {
        if (selectedPriceMode != "price") {
            return menu.originalPrice * (100 - selectedDiscountRate) / 100
        }
        val stepView = binding.stepContainer.getChildAt(0) ?: return null
        val price = stepView.findViewById<android.widget.EditText>(R.id.et_direct_price)
            ?.text?.toString()?.toIntOrNull()
        return when {
            price == null -> {
                toast("할인가를 입력해주세요")
                setSubmitting(false)
                null
            }
            price <= 0 || price >= menu.originalPrice -> {
                toast("할인가는 정가보다 낮은 1원 이상이어야 해요.")
                setSubmitting(false)
                null
            }
            else -> price
        }
    }

    private fun setSubmitting(submitting: Boolean) {
        binding.btnNext.isEnabled = !submitting
        binding.btnNext.text = if (submitting) "등록 중..." else "상품 등록"
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
                        toast("메뉴가 삭제됐어요")
                        loadMenus()
                    }.onFailure {
                        toast("메뉴 삭제에 실패했어요")
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }
}
