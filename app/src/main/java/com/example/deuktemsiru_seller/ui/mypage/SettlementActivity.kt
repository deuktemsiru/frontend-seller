package com.example.deuktemsiru_seller.ui.mypage

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.databinding.ActivitySettlementBinding
import com.example.deuktemsiru_seller.network.RetrofitClient
import com.example.deuktemsiru_seller.network.SettlementWithdrawRequest
import com.example.deuktemsiru_seller.util.toWon
import kotlinx.coroutines.launch
import java.time.LocalDate

class SettlementActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettlementBinding
    private var currentOffset = 0
    private var currentSettlementAmount: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettlementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnPrevMonth.setOnClickListener {
            currentOffset++
            loadSettlement()
        }

        binding.btnNextMonth.setOnClickListener {
            if (currentOffset > 0) {
                currentOffset--
                loadSettlement()
            }
        }

        binding.btnWithdraw.setOnClickListener { showWithdrawDialog() }

        loadSettlement()
    }

    private fun loadSettlement() {
        val target = LocalDate.now().minusMonths(currentOffset.toLong())
        binding.tvMonthLabel.text = "${target.year}년 ${target.monthValue}월"
        binding.btnNextMonth.alpha = if (currentOffset > 0) 1f else 0.3f

        binding.tvTotalSales.text = "—"
        binding.tvCommission.text = "—"
        binding.tvSettlementAmount.text = "—"
        currentSettlementAmount = null

        lifecycleScope.launch {
            runCatching {
                val settlementItem = RetrofitClient.api.getSettlements(
                    year = target.year,
                    month = target.monthValue,
                ).data?.settlements?.firstOrNull()

                val total = settlementItem?.totalSales ?: 0
                val commission = settlementItem?.platformFee ?: 0
                val settlement = settlementItem?.settlementAmount ?: 0

                currentSettlementAmount = settlement
                binding.tvTotalSales.text = total.toWon()
                binding.tvCommission.text = commission.toWon()
                binding.tvSettlementAmount.text = settlement.toWon()
            }.onFailure {
                Toast.makeText(this@SettlementActivity, "데이터를 불러올 수 없어요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showWithdrawDialog() {
        val numericAmount = currentSettlementAmount
        if (numericAmount == null) {
            Toast.makeText(this, "정산 데이터를 불러오는 중이에요.", Toast.LENGTH_SHORT).show()
            return
        }
        if (numericAmount <= 0) {
            Toast.makeText(this, "출금 가능한 금액이 없어요.", Toast.LENGTH_SHORT).show()
            return
        }
        val amount = numericAmount.toWon()
        AlertDialog.Builder(this)
            .setTitle("출금 신청")
            .setMessage("정산 예정금액 ${amount}을 출금 신청하시겠어요?\n\n영업일 기준 3–5일 이내 등록된 계좌로 입금됩니다.")
            .setPositiveButton("신청하기") { _, _ ->
                val target = LocalDate.now().minusMonths(currentOffset.toLong())
                lifecycleScope.launch {
                    runCatching {
                        RetrofitClient.api.requestWithdrawal(
                            SettlementWithdrawRequest(target.year, target.monthValue)
                        )
                    }.onSuccess {
                        Toast.makeText(this@SettlementActivity, "출금 신청이 완료됐어요.", Toast.LENGTH_SHORT).show()
                        loadSettlement()
                    }.onFailure {
                        Toast.makeText(this@SettlementActivity, "출금 신청에 실패했어요.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }
}
