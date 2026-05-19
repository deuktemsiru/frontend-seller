package com.example.deuktemsiru_seller.ui.sales

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.R
import com.example.deuktemsiru_seller.data.SessionManager
import com.example.deuktemsiru_seller.databinding.FragmentSalesBinding
import com.example.deuktemsiru_seller.network.RetrofitClient
import com.example.deuktemsiru_seller.network.SalesPeriod
import com.example.deuktemsiru_seller.network.TopMenu
import com.example.deuktemsiru_seller.util.emptyTextView
import com.example.deuktemsiru_seller.util.handleSellerAuthFailure
import com.example.deuktemsiru_seller.util.offsetBy
import com.example.deuktemsiru_seller.util.renderChildren
import com.example.deuktemsiru_seller.util.toast
import com.example.deuktemsiru_seller.util.toWon
import com.example.deuktemsiru_seller.util.visibleIf
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SalesFragment : Fragment() {

    private var _binding: FragmentSalesBinding? = null
    private val binding get() = _binding!!

    private var currentPeriod = SalesPeriod.Day
    private var currentOffset = 0
    private var showPieChart = false
    private lateinit var session: SessionManager

    private companion object {
        const val TAG = "SalesFragment"
    }

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

        session = SessionManager(requireContext())

        binding.tabDaily.setOnClickListener { switchPeriod(SalesPeriod.Day) }
        binding.tabWeekly.setOnClickListener { switchPeriod(SalesPeriod.Week) }
        binding.tabMonthly.setOnClickListener { switchPeriod(SalesPeriod.Month) }
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

    private fun switchPeriod(period: SalesPeriod) {
        currentPeriod = period
        currentOffset = 0
        updateTabs()
        loadSales()
    }

    private fun updateTabs() {
        val tabs = listOf(
            binding.tabDaily to SalesPeriod.Day,
            binding.tabWeekly to SalesPeriod.Week,
            binding.tabMonthly to SalesPeriod.Month,
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
        binding.tvSalesTitle.text = currentPeriod.title
    }

    private fun computeDateStr(): String {
        return targetDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    private fun targetDate(): LocalDate = LocalDate.now().offsetBy(currentPeriod, currentOffset)

    private fun loadSales() {
        binding.tvLoadingIndicator.visibleIf(true)
        updatePeriodLabel()

        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                val sales = RetrofitClient.api.getSales(
                    period = currentPeriod.apiValue,
                    date = computeDateStr(),
                ).data ?: return@runCatching

                val total = sales.salesData.sumOf { it.amount }
                binding.tvTotalSales.text = total.toWon()

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
            }.onFailure { error ->
                if (handleSellerAuthFailure(error, session)) return@onFailure
                if (error is HttpException && error.code() == 404) {
                    renderEmptySales()
                    return@onFailure
                }
                Log.e(TAG, "Failed to load sales", error)
                if (isAdded && context != null) {
                    toast("데이터를 불러올 수 없어요.")
                }
            }
            binding.tvLoadingIndicator.visibleIf(false)
        }
    }

    private fun renderEmptySales() {
        binding.tvTotalSales.text = 0.toWon()
        binding.barChart.setEntries(emptyList())
        binding.tvWasteCount.text = "절감 폐기 0개"
        binding.tvCo2Amount.text = "탄소 0kg 절감"
        renderTopMenus(emptyList())
        binding.pieChart.setSlices(emptyList())
        updateInsight(emptyList(), emptyList())
    }

    private fun updatePeriodLabel() {
        binding.tvPeriodRange.text = when (currentPeriod) {
            SalesPeriod.Day -> {
                targetDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            }
            SalesPeriod.Month -> {
                val month = targetDate()
                "${month.year}년 ${month.monthValue}월"
            }
            else -> {
                val endDay = targetDate()
                val startDay = endDay.minusDays(6)
                val fmt = DateTimeFormatter.ofPattern("MM.dd")
                "${startDay.format(fmt)} – ${endDay.format(fmt)}"
            }
        }
        binding.btnNext.alpha = if (currentOffset > 0) 1f else 0.3f
    }

    private fun toggleChartMode() {
        showPieChart = !showPieChart
        binding.containerTopMenus.visibleIf(!showPieChart)
        binding.pieChart.visibleIf(showPieChart)
        binding.btnToggleChart.text = if (showPieChart) "TOP 3 목록" else "파이차트"
    }

    private fun renderTopMenus(menus: List<TopMenu>) {
        data class RankStyle(val bg: Int, val color: Int)
        val rankStyles = listOf(
            RankStyle(R.drawable.bg_rounded_primary_light, R.color.primary_dark),
            RankStyle(R.drawable.bg_rounded_muted, R.color.text_muted),
            RankStyle(R.drawable.bg_rounded_muted, R.color.text_muted),
        )

        binding.containerTopMenus.renderChildren(
            items = menus.withIndex().toList(),
            emptyView = {
                requireContext().emptyTextView("아직 판매 데이터가 없어요", verticalPaddingDp = 14)
            },
        ) { (i, menu) ->
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_top_menu, binding.containerTopMenus, false)
            val rankStyle = rankStyles.getOrElse(i) { rankStyles.last() }
            itemView.findViewById<TextView>(R.id.tvRank).apply {
                text = "${i + 1}"
                setBackgroundResource(rankStyle.bg)
                setTextColor(requireContext().getColor(rankStyle.color))
            }
            itemView.findViewById<TextView>(R.id.tvMenuEmoji).text = menu.emoji ?: "🍽️"
            itemView.findViewById<TextView>(R.id.tvMenuName).text = menu.name
            itemView.findViewById<TextView>(R.id.tvMenuCount).text = "${menu.count}건 판매"
            if (i < menus.lastIndex) {
                itemView.findViewById<View>(R.id.dividerMenu).visibility = View.VISIBLE
            }
            itemView
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
