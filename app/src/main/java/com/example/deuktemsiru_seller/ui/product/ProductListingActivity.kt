package com.example.deuktemsiru_seller.ui.product

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.deuktemsiru_seller.network.SaleItemRequest
import com.example.deuktemsiru_seller.ui.registration.MenuRegistrationActivity
import kotlinx.coroutines.launch

class ProductListingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductListingBinding
    private lateinit var session: SessionManager
    private var currentStep = 1
    private var menus: List<MenuItemApiResponse> = emptyList()
    private var selectedMenu: MenuItemApiResponse? = null
    private var selectedDiscountRate = 60
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
                menus = RetrofitClient.api.getMyStore(session.sellerId).menus
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

        val etDiscount = view.findViewById<android.widget.EditText>(R.id.et_discount_rate)
        val tvDiscounted = view.findViewById<TextView>(R.id.tv_discounted_price)
        val tvQty = view.findViewById<TextView>(R.id.tv_quantity)
        val tvStart = view.findViewById<TextView>(R.id.tv_pickup_start)
        val tvEnd = view.findViewById<TextView>(R.id.tv_pickup_end)

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

        fun refreshPickup() {
            tvStart.text = formatTime(pickupStartMinutes)
            tvEnd.text = formatTime(pickupEndMinutes)
            selectedPickupTimeSlot = "${formatTime(pickupStartMinutes)}-${formatTime(pickupEndMinutes)}"
        }
        refreshPickup()

        view.findViewById<View>(R.id.container_pickup_start).setOnClickListener {
            showTimePicker(pickupStartMinutes) { m ->
                pickupStartMinutes = m
                if (pickupEndMinutes <= pickupStartMinutes) pickupEndMinutes = (m + 30).coerceAtMost(23 * 60 + 59)
                refreshPickup()
            }
        }
        view.findViewById<View>(R.id.container_pickup_end).setOnClickListener {
            showTimePicker(pickupEndMinutes) { m ->
                if (m <= pickupStartMinutes) Toast.makeText(this, "마감 시간은 시작보다 늦어야 해요", Toast.LENGTH_SHORT).show()
                else { pickupEndMinutes = m; refreshPickup() }
            }
        }
        view.findViewById<View>(R.id.chip_30min).setOnClickListener { pickupEndMinutes = (pickupStartMinutes + 30).coerceAtMost(23 * 60 + 59); refreshPickup() }
        view.findViewById<View>(R.id.chip_1hour).setOnClickListener { pickupEndMinutes = (pickupStartMinutes + 60).coerceAtMost(23 * 60 + 59); refreshPickup() }
        view.findViewById<View>(R.id.chip_until_close).setOnClickListener {
            pickupEndMinutes = 21 * 60
            if (pickupEndMinutes <= pickupStartMinutes) pickupEndMinutes = (pickupStartMinutes + 30).coerceAtMost(23 * 60 + 59)
            refreshPickup()
        }
    }

    private fun onNextClicked() {
        if (currentStep == 1) {
            if (selectedMenu == null) { Toast.makeText(this, "메뉴를 선택해주세요", Toast.LENGTH_SHORT).show(); return }
            loadStep(2)
        } else {
            if (selectedDiscountRate !in 1..99) { Toast.makeText(this, "1~99 사이의 할인율을 입력해주세요", Toast.LENGTH_SHORT).show(); return }
            registerProduct()
        }
    }

    private fun registerProduct() {
        val menu = selectedMenu ?: return
        binding.btnNext.isEnabled = false
        binding.btnNext.text = "등록 중..."
        lifecycleScope.launch {
            try {
                RetrofitClient.api.createSaleItem(
                    sellerId = session.sellerId,
                    req = SaleItemRequest(
                        menuItemId = menu.id,
                        discountRate = selectedDiscountRate,
                        quantity = selectedQuantity,
                        pickupTimeSlot = selectedPickupTimeSlot,
                    ),
                )
                Toast.makeText(this@ProductListingActivity, "상품이 등록됐어요!", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@ProductListingActivity, "등록에 실패했어요", Toast.LENGTH_SHORT).show()
                binding.btnNext.isEnabled = true
                binding.btnNext.text = "상품 등록"
            }
        }
    }

    private fun showTimePicker(initialMinutes: Int, onPicked: (Int) -> Unit) {
        TimePickerDialog(this, { _, h, m -> onPicked(h * 60 + m) }, initialMinutes / 60, initialMinutes % 60, true).show()
    }

    private fun formatTime(minutes: Int) = "%02d:%02d".format(minutes / 60, minutes % 60)
}
