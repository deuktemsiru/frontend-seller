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
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SettlementActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettlementBinding
    private var currentOffset = 0

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

        lifecycleScope.launch {
            runCatching {
                val sales = RetrofitClient.api.getSales(
                    period = "MONTH",
                    date = target.format(DateTimeFormatter.ISO_LOCAL_DATE),
                ).data ?: return@runCatching

                val total = sales.salesData.sumOf { it.amount }
                val commission = (total * 0.025).toInt()
                val settlement = total - commission

                binding.tvTotalSales.text = "%,d원".format(total)
                binding.tvCommission.text = "%,d원".format(commission)
                binding.tvSettlementAmount.text = "%,d원".format(settlement)
            }.onFailure {
                Toast.makeText(this@SettlementActivity, "데이터를 불러올 수 없어요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showWithdrawDialog() {
        val amount = binding.tvSettlementAmount.text.toString()
        AlertDialog.Builder(this)
            .setTitle("출금 신청")
            .setMessage("정산 예정금액 ${amount}을 출금 신청하시겠어요?\n\n영업일 기준 3–5일 이내 등록된 계좌로 입금됩니다.")
            .setPositiveButton("신청하기") { _, _ ->
                Toast.makeText(this, "출금 신청이 완료됐어요.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소", null)
            .show()
    }
}
