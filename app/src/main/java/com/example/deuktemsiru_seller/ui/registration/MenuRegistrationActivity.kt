package com.example.deuktemsiru_seller.ui.registration

import android.app.TimePickerDialog
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.R
import com.example.deuktemsiru_seller.data.SessionManager
import com.example.deuktemsiru_seller.databinding.ActivityMenuRegistrationBinding
import com.example.deuktemsiru_seller.network.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

class MenuRegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuRegistrationBinding
    private var currentStep = 1
    private val totalSteps = 4

    private var selectedMenuName = ""
    private var selectedImageUri: Uri? = null
    private val selectedEmoji = "🍽️"
    private var selectedOriginalPrice = 0
    private var selectedCostPrice: Int? = null
    private var selectedAllergyInfo: String? = null
    private var selectedDiscountRate = 0
    private var selectedQuantity = 5
    private var pickupStartMinutes = 16 * 60 + 30
    private var pickupEndMinutes = 18 * 60
    private var selectedPickupTimeSlot = "16:30-18:00"
    private var currentStepView: View? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
        currentStepView?.let { refreshStep1Image(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadStep(1)

        binding.btnBack.setOnClickListener {
            if (currentStep > 1) loadStep(currentStep - 1) else finish()
        }

        binding.btnNext.setOnClickListener {
            if (currentStep == 1 && !validateStep1()) {
                return@setOnClickListener
            }
            if (currentStep == 2 && !validateStep2()) {
                return@setOnClickListener
            }

            if (currentStep < totalSteps) {
                loadStep(currentStep + 1)
            } else {
                registerMenu()
            }
        }

    }

    private fun registerMenu() {
        val session = SessionManager(this)
        RetrofitClient.authToken = session.token
        if (!session.isLoggedIn()) {
            Toast.makeText(this, "로그인이 필요해요.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnNext.isEnabled = false
        binding.btnNext.text = "등록 중..."

        lifecycleScope.launch {
            try {
                RetrofitClient.api.addMenuWithImage(
                    sellerId = session.sellerId,
                    name = selectedMenuName.toTextPart(),
                    emoji = selectedEmoji.toTextPart(),
                    originalPrice = selectedOriginalPrice.toString().toTextPart(),
                    discountRate = selectedDiscountRate.toString().toTextPart(),
                    quantity = selectedQuantity.toString().toTextPart(),
                    pickupTimeSlot = selectedPickupTimeSlot.toTextPart(),
                    image = createImagePart(),
                )
                Toast.makeText(
                    this@MenuRegistrationActivity,
                    "마감 메뉴가 등록됐어요! 단골 알림을 보내볼까요?",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@MenuRegistrationActivity, "등록에 실패했어요.", Toast.LENGTH_SHORT).show()
                binding.btnNext.isEnabled = true
                binding.btnNext.text = "등록하기"
            }
        }
    }

    private fun loadStep(step: Int) {
        currentStep = step
        updateProgressBar()
        updateStepLabel()
        updateBottomButtons()

        val layoutRes = when (step) {
            1 -> R.layout.step1_menu_select
            2 -> R.layout.step2_price_setting
            3 -> R.layout.step3_quantity_time
            4 -> R.layout.step4_preview
            else -> R.layout.step1_menu_select
        }

        binding.stepContainer.removeAllViews()
        val stepView = layoutInflater.inflate(layoutRes, binding.stepContainer, false)
        binding.stepContainer.addView(stepView)
        currentStepView = stepView

        if (step == 1) setupStep1(stepView)
        if (step == 2) setupStep2(stepView)
        if (step == 3) setupStep3(stepView)
        if (step == 4) setupStep4(stepView)
    }

    private fun setupStep1(view: View) {
        val etName = view.findViewById<EditText>(R.id.et_menu_name)
        val etPrice = view.findViewById<EditText>(R.id.et_original_price)
        val etCostPrice = view.findViewById<EditText?>(R.id.et_cost_price)
        val etAllergyInfo = view.findViewById<EditText?>(R.id.et_allergy_info)
        val tvSummary = view.findViewById<TextView>(R.id.tv_selected_menu_summary)
        val imageUpload = view.findViewById<View>(R.id.container_image_upload)

        etName.setText(selectedMenuName)
        if (selectedOriginalPrice > 0) etPrice.setText(selectedOriginalPrice.toString())
        selectedCostPrice?.let { etCostPrice?.setText(it.toString()) }
        selectedAllergyInfo?.let { etAllergyInfo?.setText(it) }
        refreshStep1Image(view)

        imageUpload.setOnClickListener {
            imagePicker.launch("image/*")
        }

        fun refreshSummary() {
            val name = etName.text?.toString()?.trim().orEmpty()
            val price = etPrice.text?.toString()?.toIntOrNull() ?: 0
            tvSummary.text = if (name.isBlank() || price <= 0) {
                "메뉴 정보를 입력해주세요"
            } else {
                "$name  ·  ${formatWon(price)}"
            }
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = refreshSummary()
            override fun afterTextChanged(s: Editable?) = Unit
        }
        etName.addTextChangedListener(watcher)
        etPrice.addTextChangedListener(watcher)
        refreshSummary()
    }

    private fun refreshStep1Image(view: View) {
        val imageView = view.findViewById<ImageView>(R.id.iv_menu_image)
        val hintView = view.findViewById<TextView>(R.id.tv_image_upload_hint)
        val uri = selectedImageUri
        if (uri == null) {
            imageView.visibility = View.GONE
            hintView.visibility = View.VISIBLE
        } else {
            imageView.setImageURI(uri)
            imageView.visibility = View.VISIBLE
            hintView.visibility = View.GONE
        }
    }

    private fun validateStep1(): Boolean {
        val view = binding.stepContainer.getChildAt(0) ?: return true
        val etName = view.findViewById<EditText>(R.id.et_menu_name)
        val etPrice = view.findViewById<EditText>(R.id.et_original_price)

        val name = etName.text?.toString()?.trim().orEmpty()
        val price = etPrice.text?.toString()?.toIntOrNull() ?: 0

        if (name.isBlank()) {
            etName.error = "메뉴명을 입력해주세요"
            etName.requestFocus()
            return false
        }
        if (price <= 0) {
            etPrice.error = "정상가를 입력해주세요"
            etPrice.requestFocus()
            return false
        }

        selectedMenuName = name
        selectedOriginalPrice = price
        selectedCostPrice = view.findViewById<EditText?>(R.id.et_cost_price)?.text?.toString()?.toIntOrNull()
        selectedAllergyInfo = view.findViewById<EditText?>(R.id.et_allergy_info)?.text?.toString()?.trim()?.takeIf { it.isNotBlank() }
        return true
    }

    private fun setupStep4(view: View) {
        val session = SessionManager(this)
        view.findViewById<TextView>(R.id.tv_preview_store_name).text =
            session.storeName.ifBlank { "내 가게" }
        val previewImage = view.findViewById<ImageView>(R.id.iv_preview_menu_image)
        selectedImageUri?.let { previewImage.setImageURI(it) }
        view.findViewById<TextView>(R.id.tv_preview_menu_name).text = selectedMenuName
        view.findViewById<TextView>(R.id.tv_preview_discount_price).text = formatWon(discountedPrice())
        view.findViewById<TextView>(R.id.tv_preview_discount_rate).text = " ${selectedDiscountRate}%↓"
        view.findViewById<TextView>(R.id.tv_preview_quantity_time).text =
            "잔여 ${selectedQuantity}개 · 픽업 ${pickupTimeLabel()}"
        view.findViewById<TextView>(R.id.tv_summary_menu_name).text = selectedMenuName
        view.findViewById<TextView>(R.id.tv_summary_original_price).text = formatWon(selectedOriginalPrice)
        view.findViewById<TextView>(R.id.tv_summary_discount_price).text =
            "${formatWon(discountedPrice())} (${selectedDiscountRate}% 할인)"
        view.findViewById<TextView>(R.id.tv_summary_quantity).text = "${selectedQuantity}개"
        view.findViewById<TextView>(R.id.tv_summary_pickup_time).text = pickupTimeLabel()
    }

    private fun setupStep2(view: View) {
        val presetIds = mapOf(
            30 to view.findViewById<TextView>(R.id.preset_30),
            50 to view.findViewById<TextView>(R.id.preset_50),
            60 to view.findViewById<TextView>(R.id.preset_60),
            70 to view.findViewById<TextView>(R.id.preset_70)
        )
        val etDiscountRate = view.findViewById<EditText>(R.id.et_discount_rate)
        val tvOriginalPrice = view.findViewById<TextView>(R.id.tv_original_price)
        val tvDiscountPrice = view.findViewById<TextView>(R.id.tv_discount_price)
        val tvCustomerSaving = view.findViewById<TextView>(R.id.tv_customer_saving)
        val tvSellerRecovery = view.findViewById<TextView>(R.id.tv_seller_recovery)

        fun refreshPrices() {
            tvOriginalPrice.text = formatWon(selectedOriginalPrice)
            tvDiscountPrice.text = formatWon(discountedPrice())
            tvCustomerSaving.text = formatWon(selectedOriginalPrice - discountedPrice())
            tvSellerRecovery.text = formatWon(discountedPrice())
            presetIds.forEach { (discount, presetView) ->
                val isSelected = discount == selectedDiscountRate
                presetView.setBackgroundResource(
                    if (isSelected) R.drawable.bg_discount_preset_selected
                    else R.drawable.bg_discount_preset_normal
                )
                presetView.setTextColor(getColor(if (isSelected) R.color.white else R.color.text_sub))
            }
        }

        if (selectedDiscountRate > 0) {
            etDiscountRate.setText(selectedDiscountRate.toString())
        }

        etDiscountRate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                selectedDiscountRate = s?.toString()?.toIntOrNull() ?: 0
                refreshPrices()
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        presetIds.forEach { (discount, presetView) ->
            presetView.setOnClickListener {
                selectedDiscountRate = discount
                etDiscountRate.setText(discount.toString())
                etDiscountRate.setSelection(etDiscountRate.text?.length ?: 0)
            }
        }
        refreshPrices()
    }

    private fun validateStep2(): Boolean {
        val view = binding.stepContainer.getChildAt(0) ?: return true
        val etDiscountRate = view.findViewById<EditText>(R.id.et_discount_rate)
        val discountRate = etDiscountRate.text?.toString()?.toIntOrNull() ?: 0
        if (discountRate !in 1..99) {
            etDiscountRate.error = "1~99 사이의 할인율을 입력해주세요"
            etDiscountRate.requestFocus()
            return false
        }

        selectedDiscountRate = discountRate
        return true
    }

    private fun setupStep3(view: View) {
        var qty = selectedQuantity
        val tvQty = view.findViewById<TextView>(R.id.tv_quantity)
        val tvStart = view.findViewById<TextView>(R.id.tv_pickup_start)
        val tvEnd = view.findViewById<TextView>(R.id.tv_pickup_end)
        tvQty.text = qty.toString()
        refreshPickupViews(tvStart, tvEnd)

        view.findViewById<View>(R.id.btn_qty_minus).setOnClickListener {
            if (qty > 1) {
                qty--
                tvQty.text = qty.toString()
                selectedQuantity = qty
            }
        }

        view.findViewById<View>(R.id.btn_qty_plus).setOnClickListener {
            if (qty < 99) {
                qty++
                tvQty.text = qty.toString()
                selectedQuantity = qty
            }
        }

        view.findViewById<View>(R.id.container_pickup_start).setOnClickListener {
            showTimePicker(pickupStartMinutes) { minutes ->
                pickupStartMinutes = minutes
                if (pickupEndMinutes <= pickupStartMinutes) pickupEndMinutes = (pickupStartMinutes + 30).coerceAtMost(23 * 60 + 59)
                refreshPickupViews(tvStart, tvEnd)
            }
        }

        view.findViewById<View>(R.id.container_pickup_end).setOnClickListener {
            showTimePicker(pickupEndMinutes) { minutes ->
                if (minutes <= pickupStartMinutes) {
                    Toast.makeText(this, "마감 시간은 시작 시간보다 늦어야 해요.", Toast.LENGTH_SHORT).show()
                } else {
                    pickupEndMinutes = minutes
                    refreshPickupViews(tvStart, tvEnd)
                }
            }
        }

        view.findViewById<View>(R.id.chip_30min).setOnClickListener {
            pickupEndMinutes = (pickupStartMinutes + 30).coerceAtMost(23 * 60 + 59)
            refreshPickupViews(tvStart, tvEnd)
        }
        view.findViewById<View>(R.id.chip_1hour).setOnClickListener {
            pickupEndMinutes = (pickupStartMinutes + 60).coerceAtMost(23 * 60 + 59)
            refreshPickupViews(tvStart, tvEnd)
        }
        view.findViewById<View>(R.id.chip_until_close).setOnClickListener {
            pickupEndMinutes = 21 * 60
            if (pickupEndMinutes <= pickupStartMinutes) pickupEndMinutes = (pickupStartMinutes + 30).coerceAtMost(23 * 60 + 59)
            refreshPickupViews(tvStart, tvEnd)
        }
    }

    private fun showTimePicker(initialMinutes: Int, onPicked: (Int) -> Unit) {
        TimePickerDialog(this, { _, hourOfDay, minute ->
            onPicked(hourOfDay * 60 + minute)
        }, initialMinutes / 60, initialMinutes % 60, true).show()
    }

    private fun refreshPickupViews(tvStart: TextView, tvEnd: TextView) {
        tvStart.text = formatTime(pickupStartMinutes)
        tvEnd.text = formatTime(pickupEndMinutes)
        selectedPickupTimeSlot = "${formatTime(pickupStartMinutes)}-${formatTime(pickupEndMinutes)}"
    }

    private fun discountedPrice(): Int = selectedOriginalPrice * (100 - selectedDiscountRate) / 100

    private fun pickupTimeLabel(): String =
        "${formatTime(pickupStartMinutes)} – ${formatTime(pickupEndMinutes)}"

    private fun formatTime(minutes: Int): String = "%02d:%02d".format(minutes / 60, minutes % 60)

    private fun formatWon(price: Int): String = "%,d원".format(price)

    private fun String.toTextPart(): RequestBody =
        RequestBody.create(MediaType.parse("text/plain"), this)

    private fun createImagePart(): MultipartBody.Part? {
        val uri = selectedImageUri ?: return null
        val mimeType = contentResolver.getType(uri) ?: "image/*"
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        val filename = getDisplayName(uri) ?: "menu-image"
        val body = RequestBody.create(MediaType.parse(mimeType), bytes)
        return MultipartBody.Part.createFormData("image", filename, body)
    }

    private fun getDisplayName(uri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            cursor = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            } else {
                null
            }
        } finally {
            cursor?.close()
        }
    }

    private fun updateProgressBar() {
        val progressViews = listOf(
            binding.progress1, binding.progress2, binding.progress3, binding.progress4
        )
        progressViews.forEachIndexed { index, view ->
            view.setBackgroundResource(
                if (index < currentStep) R.drawable.bg_progress_active
                else R.drawable.bg_progress_inactive
            )
        }
    }

    private fun updateStepLabel() {
        binding.tvStepLabel.text = "$currentStep/$totalSteps"
    }

    private fun updateBottomButtons() {
        when (currentStep) {
            totalSteps -> {
                binding.btnNext.text = "등록하기"
            }
            else -> {
                binding.btnNext.text = "다음 ($currentStep/${totalSteps})"
            }
        }
    }
}
