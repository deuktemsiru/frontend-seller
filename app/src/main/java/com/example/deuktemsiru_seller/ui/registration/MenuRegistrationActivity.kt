package com.example.deuktemsiru_seller.ui.registration

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deuktemsiru_seller.R
import com.example.deuktemsiru_seller.databinding.ActivityMenuRegistrationBinding

class MenuRegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuRegistrationBinding
    private var currentStep = 1
    private val totalSteps = 4

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
            if (currentStep > 1) {
                loadStep(currentStep - 1)
            } else {
                finish()
            }
        }

        binding.btnNext.setOnClickListener {
            if (currentStep < totalSteps) {
                loadStep(currentStep + 1)
            } else {
                Toast.makeText(this, "마감 메뉴가 등록됐어요! 단골 알림을 보내볼까요?", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        binding.btnPreviewAction.setOnClickListener {
            loadStep(4)
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

        if (step == 3) {
            setupStep3(stepView)
        }
        if (step == 2) {
            setupStep2(stepView)
        }
    }

    private fun setupStep2(view: View) {
        val prices = mapOf(30 to 10500, 50 to 7500, 60 to 5900, 70 to 4500)
        val presetIds = mapOf(
            30 to view.findViewById<View>(R.id.preset_30),
            50 to view.findViewById<View>(R.id.preset_50),
            60 to view.findViewById<View>(R.id.preset_60),
            70 to view.findViewById<View>(R.id.preset_70)
        )
        val tvDiscountPrice = view.findViewById<android.widget.TextView>(R.id.tv_discount_price)

        presetIds.forEach { (discount, presetView) ->
            presetView.setOnClickListener {
                presetIds.forEach { (_, v) ->
                    v.setBackgroundResource(R.drawable.bg_discount_preset_normal)
                }
                presetView.setBackgroundResource(R.drawable.bg_discount_preset_selected)
                val price = prices[discount] ?: 0
                tvDiscountPrice.text = "${String.format("%,d", price)}원"
            }
        }
    }

    private fun setupStep3(view: View) {
        var qty = 5
        val tvQty = view.findViewById<android.widget.TextView>(R.id.tv_quantity)

        view.findViewById<View>(R.id.btn_qty_minus).setOnClickListener {
            if (qty > 1) {
                qty--
                tvQty.text = qty.toString()
            }
        }

        view.findViewById<View>(R.id.btn_qty_plus).setOnClickListener {
            if (qty < 99) {
                qty++
                tvQty.text = qty.toString()
            }
        }
    }

    private fun updateProgressBar() {
        val progressViews = listOf(
            binding.progress1,
            binding.progress2,
            binding.progress3,
            binding.progress4
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
                binding.btnPreviewAction.visibility = View.GONE
            }
            totalSteps - 1 -> {
                binding.btnNext.text = "다음 ($currentStep/${totalSteps})"
                binding.btnPreviewAction.visibility = View.VISIBLE
            }
            else -> {
                binding.btnNext.text = "다음 ($currentStep/${totalSteps})"
                binding.btnPreviewAction.visibility = View.GONE
            }
        }
    }
}
