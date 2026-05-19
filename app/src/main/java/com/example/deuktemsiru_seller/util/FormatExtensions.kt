package com.example.deuktemsiru_seller.util

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import com.example.deuktemsiru_seller.R
import com.example.deuktemsiru_seller.data.SessionManager
import com.example.deuktemsiru_seller.ui.auth.LoginActivity
import retrofit2.HttpException
import java.time.LocalDate
import java.util.Locale

fun Int.toWon(): String = "%,d원".format(Locale.KOREA, this)

fun Int.toClockTime(): String = "%02d:%02d".format(Locale.KOREA, this / 60, this % 60)

fun String.toClockMinutes(defaultHour: Int = 21, defaultMinute: Int = 0): Int {
    val parts = split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: defaultHour
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: defaultMinute
    return hour * 60 + minute
}

fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Fragment.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    requireContext().toast(message, duration)
}

fun TextView.setSelectedChip(
    selected: Boolean,
    selectedBackground: Int = R.drawable.bg_discount_preset_selected,
    unselectedBackground: Int = R.drawable.bg_discount_preset_normal,
    selectedTextColor: Int = R.color.white,
    unselectedTextColor: Int = R.color.text_sub,
) {
    setBackgroundResource(if (selected) selectedBackground else unselectedBackground)
    setTextColor(ContextCompat.getColor(context, if (selected) selectedTextColor else unselectedTextColor))
}

fun View.visibleIf(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

fun Collection<View>.visibleIf(visible: Boolean) {
    forEach { it.visibleIf(visible) }
}

fun Fragment.handleSellerAuthFailure(
    error: Throwable,
    session: SessionManager,
    message: String = "판매자 계정으로 다시 로그인해주세요.",
): Boolean {
    if (error !is HttpException || error.code() !in listOf(401, 403)) return false
    session.clear()
    toast(message)
    startActivity(
        Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    )
    return true
}

fun ViewGroup.replaceChildren(children: Iterable<View>) {
    removeAllViews()
    children.forEach(::addView)
}

fun <T> ViewGroup.renderChildren(
    items: List<T>,
    emptyView: () -> View,
    itemView: (T) -> View,
) {
    replaceChildren(if (items.isEmpty()) listOf(emptyView()) else items.map(itemView))
}

fun LayoutInflater.inflateInto(layoutRes: Int, parent: ViewGroup): View =
    inflate(layoutRes, parent, false).also(parent::addView)

fun discountPresetViews(view: View): Map<Int, TextView> =
    mapOf(
        10 to view.findViewById(R.id.preset_30),
        20 to view.findViewById(R.id.preset_50),
        30 to view.findViewById(R.id.preset_60),
        40 to view.findViewById(R.id.preset_70),
    )

fun EditText.bindDiscountPresets(
    presets: Map<Int, TextView>,
    selectedRate: () -> Int,
    onRateChanged: (Int) -> Unit,
    refresh: () -> Unit,
) {
    presets.forEach { (discount, presetView) ->
        presetView.text = "$discount%"
        presetView.setOnClickListener {
            onRateChanged(discount)
            setText(discount.toString())
            setSelection(text?.length ?: 0)
        }
    }
    addTextChangedListener(simpleTextWatcher { text ->
        onRateChanged(text.toIntOrNull() ?: 0)
        refresh()
    })
    refreshDiscountPresets(presets, selectedRate())
}

fun refreshDiscountPresets(presets: Map<Int, TextView>, selectedRate: Int) {
    presets.forEach { (discount, presetView) ->
        presetView.setSelectedChip(discount == selectedRate)
    }
}

fun setupQuantityControls(
    root: View,
    initialQuantity: Int,
    onChanged: (Int) -> Unit,
) {
    var quantity = initialQuantity
    val quantityView = root.findViewById<TextView>(R.id.tv_quantity)
    fun refresh() {
        quantityView.text = quantity.toString()
        onChanged(quantity)
    }
    root.findViewById<View>(R.id.btn_qty_minus).setOnClickListener {
        if (quantity > 1) {
            quantity--
            refresh()
        }
    }
    root.findViewById<View>(R.id.btn_qty_plus).setOnClickListener {
        if (quantity < 99) {
            quantity++
            refresh()
        }
    }
    refresh()
}

fun LocalDate.offsetBy(period: com.example.deuktemsiru_seller.network.SalesPeriod, offset: Int): LocalDate =
    when (period) {
        com.example.deuktemsiru_seller.network.SalesPeriod.Day -> minusDays(offset.toLong())
        com.example.deuktemsiru_seller.network.SalesPeriod.Month -> minusMonths(offset.toLong())
        else -> minusWeeks(offset.toLong())
    }

fun Context.emptyTextView(
    message: String,
    topMarginDp: Int? = null,
    verticalPaddingDp: Int = 16,
    centered: Boolean = false,
): TextView = TextView(this).apply {
    text = message
    textSize = 13f
    setTextColor(ContextCompat.getColor(context, R.color.text_sub))
    setPadding(0, verticalPaddingDp.dp, 0, verticalPaddingDp.dp)
    if (centered) gravity = Gravity.CENTER
    topMarginDp?.let {
        layoutParams = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply { topMargin = it.dp }
    }
}
