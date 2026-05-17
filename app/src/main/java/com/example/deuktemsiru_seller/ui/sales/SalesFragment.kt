package com.example.deuktemsiru_seller.ui.sales

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.R
import com.example.deuktemsiru_seller.data.SessionManager
import com.example.deuktemsiru_seller.databinding.FragmentSalesBinding
import com.example.deuktemsiru_seller.network.RetrofitClient
import com.example.deuktemsiru_seller.network.TopMenu
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SalesFragment : Fragment() {

    private var _binding: FragmentSalesBinding? = null
    private val binding get() = _binding!!

    private var currentPeriod = "DAY"
    private var currentOffset = 0
    private var showPieChart = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val session = SessionManager(requireContext())

        binding.tabDaily.setOnClickListener { switchPeriod("DAY") }
        binding.tabWeekly.setOnClickListener { switchPeriod("WEEK") }
        binding.tabMonthly.setOnClickListener { switchPeriod("MONTH") }
        binding.btnToggleChart.setOnClickListener { toggleChartMode() }

        binding.btnPrev.setOnClickListener {
            currentOffset++
            if (session.isLoggedIn()) loadSales()
        }
        binding.btnNext.setOnClickListener {
            if (currentOffset > 0) {
                currentOffset--
                if (session.isLoggedIn()) loadSales()
            }
        }

        if (session.isLoggedIn()) loadSales()
    }

    private fun switchPeriod(period: String) {
        currentPeriod = period
        currentOffset = 0
        updateTabs()
        loadSales()
    }

    private fun updateTabs() {
        val tabs = listOf(
            binding.tabDaily to "DAY",
            binding.tabWeekly to "WEEK",
            binding.tabMonthly to "MONTH",
        )
        tabs.forEach { (tab, period) ->
            if (period == currentPeriod) {
                tab.setTextColor(requireContext().getColor(R.color.white))
                tab.setBackgroundResource(R.drawable.bg_button_primary)
            } else {
                tab.setTextColor(requireContext().getColor(R.color.text_muted))
                tab.background = null
            }
        }
        binding.tvSalesTitle.text = when (currentPeriod) {
            "DAY" -> "일간 매출"
            "MONTH" -> "월간 매출"
            else -> "주간 매출"
        }
    }

    private fun computeDateStr(): String {
        val today = LocalDate.now()
        val target = when (currentPeriod) {
            "DAY" -> today.minusDays(currentOffset.toLong())
            "MONTH" -> today.minusMonths(currentOffset.toLong())
            else -> today.minusWeeks(currentOffset.toLong())
        }
        return target.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    private fun loadSales() {
        binding.tvLoadingIndicator.visibility = View.VISIBLE
        updatePeriodLabel()

        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                val sales = RetrofitClient.api.getSales(
                    period = currentPeriod,
                    date = computeDateStr(),
                ).data ?: return@runCatching

                val total = sales.salesData.sumOf { it.amount }
                binding.tvTotalSales.text = "%,d원".format(total)

                val entries = sales.salesData.mapIndexed { i, d ->
                    BarChartView.Entry(
                        label = d.date,
                        value = d.amount,
                        isHighlighted = currentOffset == 0 && i == sales.salesData.lastIndex,
                    )
                }
                binding.barChart.setEntries(entries)

                val totalOrders = sales.todayOrderCount
                binding.tvWasteCount.text = "절감 폐기 ${totalOrders}개"
                val carbonSaved = sales.carbonSavedKg ?: 0f
                binding.tvCo2Amount.text = "탄소 ${carbonSaved.toInt()}kg 절감"

                renderTopMenus(sales.topMenus)
                binding.pieChart.setSlices(sales.topMenus.map { PieChartView.Slice(it.name, it.count) })
                updateInsight(sales.salesData.map { it.amount }, sales.topMenus)
            }.onFailure {
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "데이터를 불러올 수 없어요.", Toast.LENGTH_SHORT).show()
                }
            }
            binding.tvLoadingIndicator.visibility = View.GONE
        }
    }

    private fun updatePeriodLabel() {
        val today = LocalDate.now()
        binding.tvPeriodRange.text = when (currentPeriod) {
            "DAY" -> {
                val day = today.minusDays(currentOffset.toLong())
                day.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            }
            "MONTH" -> {
                val month = today.minusMonths(currentOffset.toLong())
                "${month.year}년 ${month.monthValue}월"
            }
            else -> {
                val endDay = today.minusWeeks(currentOffset.toLong())
                val startDay = endDay.minusDays(6)
                val fmt = DateTimeFormatter.ofPattern("MM.dd")
                "${startDay.format(fmt)} – ${endDay.format(fmt)}"
            }
        }
        binding.btnNext.alpha = if (currentOffset > 0) 1f else 0.3f
    }

    private fun toggleChartMode() {
        showPieChart = !showPieChart
        binding.containerTopMenus.visibility = if (showPieChart) View.GONE else View.VISIBLE
        binding.pieChart.visibility = if (showPieChart) View.VISIBLE else View.GONE
        binding.btnToggleChart.text = if (showPieChart) "TOP 3 목록" else "파이차트"
    }

    private fun renderTopMenus(menus: List<TopMenu>) {
        binding.containerTopMenus.removeAllViews()
        if (menus.isEmpty()) {
            val empty = TextView(requireContext()).apply {
                text = "아직 판매 데이터가 없어요"
                textSize = 13f
                setTextColor(requireContext().getColor(R.color.text_sub))
                val pad = (14 * resources.displayMetrics.density).toInt()
                setPadding(pad, pad, pad, pad)
            }
            binding.containerTopMenus.addView(empty)
            return
        }

        data class RankStyle(val bg: Int, val color: Int)
        val rankStyles = listOf(
            RankStyle(R.drawable.bg_rounded_primary_light, R.color.primary_dark),
            RankStyle(R.drawable.bg_rounded_muted, R.color.text_muted),
            RankStyle(R.drawable.bg_rounded_muted, R.color.text_muted),
        )

        menus.forEachIndexed { i, menu ->
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_top_menu, binding.containerTopMenus, false)
            itemView.findViewById<TextView>(R.id.tvRank).apply {
                text = "${i + 1}"
                setBackgroundResource(rankStyles[i].bg)
                setTextColor(requireContext().getColor(rankStyles[i].color))
            }
            itemView.findViewById<TextView>(R.id.tvMenuEmoji).text = menu.emoji ?: "🍽️"
            itemView.findViewById<TextView>(R.id.tvMenuName).text = menu.name
            itemView.findViewById<TextView>(R.id.tvMenuCount).text = "${menu.count}건 판매"
            if (i < menus.lastIndex) {
                itemView.findViewById<View>(R.id.dividerMenu).visibility = View.VISIBLE
            }
            binding.containerTopMenus.addView(itemView)
        }
    }

    private fun updateInsight(amounts: List<Int>, topMenus: List<TopMenu>) {
        binding.tvInsight.text = when {
            amounts.all { it == 0 } -> "💡 아직 판매 데이터가 없어요. 첫 번째 메뉴를 등록해보세요!"
            topMenus.isNotEmpty() -> "💡 ${topMenus.first().name}이(가) 가장 잘 팔려요. 재고를 충분히 준비해보세요!"
            else -> "💡 꾸준한 할인 메뉴 등록이 단골 고객을 만들어요!"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
