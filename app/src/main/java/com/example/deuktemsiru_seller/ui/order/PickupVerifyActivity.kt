package com.example.deuktemsiru_seller.ui.order

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.data.SessionManager
import com.example.deuktemsiru_seller.databinding.ActivityPickupVerifyBinding
import com.example.deuktemsiru_seller.network.RetrofitClient
import com.example.deuktemsiru_seller.network.UpdateOrderStatusRequest
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

class PickupVerifyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPickupVerifyBinding
    private lateinit var session: SessionManager
    private var currentOrderId: Long? = null

    private val cameraPermission = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchQrScan() else Toast.makeText(this, "카메라 권한이 필요해요", Toast.LENGTH_SHORT).show()
    }

    private val qrLauncher = registerForActivityResult(ScanContract()) { result ->
        val code = result.contents
        if (!code.isNullOrBlank()) {
            binding.etPickupCode.setText(code)
            verifyCode(code)
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
            val code = binding.etPickupCode.text?.toString()?.trim().orEmpty()
            if (code.isBlank()) { Toast.makeText(this, "픽업 코드를 입력해주세요", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
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
        binding.cardResult.visibility = View.GONE
        lifecycleScope.launch {
            try {
                val order = RetrofitClient.api.verifyPickupCode(code).data ?: return@launch
                currentOrderId = order.id
                binding.tvResultTitle.text = "✓ 주문 확인됨"
                binding.tvResultTitle.setTextColor(getColor(android.R.color.holo_green_dark))
                binding.tvResultCustomerName.text = order.customerName?.let { "고객: $it" } ?: ""
                binding.tvResultOrderNumber.text = "주문번호: ${order.orderNumber}"
                binding.tvResultItems.text = "상품: ${order.items.joinToString(", ") { "${it.emoji} ${it.name} ${it.quantity}개" }}"
                binding.tvResultAmount.text = "결제금액: %,d원".format(order.totalAmount)
                binding.cardResult.visibility = View.VISIBLE
            } catch (e: Exception) {
                Toast.makeText(this@PickupVerifyActivity, "픽업 코드를 찾을 수 없어요", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnVerify.isEnabled = true
            }
        }
    }

    private fun completePickup(orderId: Long) {
        lifecycleScope.launch {
            try {
                RetrofitClient.api.updateOrderStatus(orderId, UpdateOrderStatusRequest("COMPLETED"))
                Toast.makeText(this@PickupVerifyActivity, "픽업 완료 처리됐어요!", Toast.LENGTH_SHORT).show()
                binding.cardResult.visibility = View.GONE
                binding.etPickupCode.setText("")
                currentOrderId = null
            } catch (e: Exception) {
                Toast.makeText(this@PickupVerifyActivity, "완료 처리에 실패했어요", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
