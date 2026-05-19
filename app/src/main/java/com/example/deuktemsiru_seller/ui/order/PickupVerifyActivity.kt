package com.example.deuktemsiru_seller.ui.order

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.data.SessionManager
import com.example.deuktemsiru_seller.databinding.ActivityPickupVerifyBinding
import com.example.deuktemsiru_seller.network.ConfirmPickupRequest
import com.example.deuktemsiru_seller.network.OrderApiResponse
import com.example.deuktemsiru_seller.network.RetrofitClient
import com.example.deuktemsiru_seller.util.toast
import com.example.deuktemsiru_seller.util.toWon
import com.example.deuktemsiru_seller.util.visibleIf
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import java.util.Locale

class PickupVerifyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPickupVerifyBinding
    private lateinit var session: SessionManager
    private var currentOrderId: Long? = null
    private var currentPickupCode: String? = null

    private val cameraPermission = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchQrScan() else toast("카메라 권한이 필요해요")
    }

    private val qrLauncher = registerForActivityResult(ScanContract()) { result ->
        val code = result.contents
        if (!code.isNullOrBlank()) {
            val normalizedCode = code.toPickupCode()
            binding.etPickupCode.setText(normalizedCode)
            verifyCode(normalizedCode)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPickupVerifyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        session = SessionManager(this)
        binding.btnBack.setOnClickListener { finish() }
        binding.btnVerify.setOnClickListener {
            val code = binding.etPickupCode.text?.toString().orEmpty().toPickupCode()
            if (code.isBlank()) {
                showStatus("픽업 코드를 입력해주세요", success = false)
                return@setOnClickListener
            }
            binding.etPickupCode.setText(code)
            verifyCode(code)
        }
        binding.btnScanQr.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchQrScan()
            } else {
                cameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
        binding.btnComplete.setOnClickListener {
            currentOrderId?.let { orderId -> completePickup(orderId) }
        }
    }

    private fun launchQrScan() {
        val options = ScanOptions()
            .setDesiredBarcodeFormats("QR_CODE")
            .setPrompt("픽업 QR 코드를 화면 안에 맞춰주세요")
            .setBeepEnabled(false)
            .setOrientationLocked(false)
        qrLauncher.launch(options)
    }

    private fun verifyCode(code: String) {
        binding.btnVerify.isEnabled = false
        binding.cardResult.visibleIf(false)
        binding.cardCompleted.visibleIf(false)
        binding.tvVerifyStatus.visibleIf(false)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.verifyPickupCode(code)
                val order = response.data
                if (order != null) {
                    currentOrderId = order.id
                    currentPickupCode = code
                    showStatus("✓ 확인되었습니다", success = true)
                    populateResultCard(order)
                    binding.cardResult.visibleIf(true)
                } else {
                    showStatus("✗ 미확인되었습니다", success = false)
                }
            } catch (e: Exception) {
                showStatus("✗ 픽업 코드를 찾을 수 없어요", success = false)
            } finally {
                binding.btnVerify.isEnabled = true
            }
        }
    }

    private fun String.toPickupCode(): String =
        trim().uppercase(Locale.ROOT)

    private fun populateResultCard(order: OrderApiResponse) {
        binding.tvResultTitle.text = "주문 확인됨"
        binding.tvResultTitle.setTextColor(getColor(android.R.color.holo_green_dark))
        binding.tvResultCustomerName.text = order.customerName?.let { "고객: $it" } ?: ""
        binding.tvResultOrderNumber.text = "주문번호: ${order.orderNumber}"
        binding.tvResultItems.text = "상품: ${order.items.joinToString(", ") { "${it.emoji} ${it.name} ${it.quantity}개" }}"
        binding.tvResultAmount.text = "결제금액: ${order.totalAmount.toWon()}"
    }

    private fun completePickup(orderId: Long) {
        val code = currentPickupCode ?: run { showStatus("픽업 코드를 확인해주세요", success = false); return }
        lifecycleScope.launch {
            try {
                val completedOrder = RetrofitClient.api
                    .confirmPickup(orderId, ConfirmPickupRequest(code))
                    .data
                binding.cardResult.visibleIf(false)
                binding.etPickupCode.setText("")
                currentOrderId = null
                currentPickupCode = null

                if (completedOrder != null) {
                    showCompletedCard(completedOrder)
                } else {
                    showStatus("픽업 완료 처리됐어요!", success = true)
                }
            } catch (e: Exception) {
                showStatus("완료 처리에 실패했어요", success = false)
            }
        }
    }

    private fun showCompletedCard(order: OrderApiResponse) {
        val firstItem = order.items.firstOrNull()
        binding.tvCompletedEmoji.text = firstItem?.emoji ?: "🛍️"
        binding.tvCompletedName.text = firstItem?.let {
            if (order.items.size > 1) "${it.name} 외 ${order.items.size - 1}개" else it.name
        } ?: order.orderNumber
        binding.tvCompletedAmount.text = order.totalAmount.toWon()
        binding.cardCompleted.visibleIf(true)
        showStatus("픽업 완료 처리됐어요!", success = true)
    }

    private fun showStatus(message: String, success: Boolean) {
        binding.tvVerifyStatus.text = message
        binding.tvVerifyStatus.setTextColor(
            if (success) getColor(android.R.color.holo_green_dark)
            else getColor(android.R.color.holo_red_dark)
        )
        binding.tvVerifyStatus.visibleIf(true)
    }
}
