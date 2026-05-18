package com.example.deuktemsiru_seller.util

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.deuktemsiru_seller.R
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
