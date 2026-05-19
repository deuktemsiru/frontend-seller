package com.example.deuktemsiru_seller.network

import com.example.deuktemsiru_seller.R

enum class OrderStatus(val apiValue: String) {
    Pending("PENDING"),
    Confirmed("CONFIRMED"),
    PickedUp("PICKED_UP"),
    Cancelled("CANCELLED"),
    Completed("COMPLETED"),
    Unknown("");

    companion object {
        fun from(value: String?): OrderStatus =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: Unknown
    }
}

data class BadgeStyle(
    val text: String,
    val backgroundRes: Int,
    val textColor: Int,
)

fun OrderStatus.badgeStyle(fallback: String = apiValue): BadgeStyle =
    when (this) {
        OrderStatus.Pending -> BadgeStyle("신규 주문", R.drawable.bg_status_soldout, 0xFFE65100.toInt())
        OrderStatus.Confirmed -> BadgeStyle("준비 중", R.drawable.bg_status_available, 0xFF2E7D32.toInt())
        OrderStatus.PickedUp -> BadgeStyle("픽업 완료", R.drawable.bg_status_expired, 0xFF757575.toInt())
        OrderStatus.Cancelled -> BadgeStyle("취소됨", R.drawable.bg_status_expired, 0xFF757575.toInt())
        else -> BadgeStyle(fallback, R.drawable.bg_status_expired, 0xFF757575.toInt())
    }

enum class SaleStatus(val apiValue: String) {
    Available("AVAILABLE"),
    SoldOut("SOLD_OUT"),
    Expired("EXPIRED"),
    Unknown("");

    val isFinal: Boolean get() = this == SoldOut || this == Expired

    companion object {
        fun from(value: String?): SaleStatus =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: Unknown
    }
}

fun SaleStatus.badgeStyle(fallback: String = apiValue): BadgeStyle =
    when (this) {
        SaleStatus.Available -> BadgeStyle("● 판매중", R.drawable.bg_status_available, 0xFF2E7D32.toInt())
        SaleStatus.SoldOut -> BadgeStyle("● 품절", R.drawable.bg_status_soldout, 0xFFE65100.toInt())
        SaleStatus.Expired -> BadgeStyle("종료", R.drawable.bg_status_expired, 0xFF616161.toInt())
        else -> BadgeStyle(fallback, R.drawable.bg_status_expired, 0xFF616161.toInt())
    }

enum class NotificationTarget(val localValue: String, val apiValue: String) {
    Regular("regular", "REGULAR"),
    Nearby("nearby", "NEARBY");

    companion object {
        fun fromLocal(value: String): NotificationTarget =
            entries.firstOrNull { it.localValue == value } ?: Regular
    }
}

enum class SalesPeriod(val apiValue: String, val title: String) {
    Day("DAY", "일간 매출"),
    Week("WEEK", "주간 매출"),
    Month("MONTH", "월간 매출");

    companion object {
        fun from(value: String): SalesPeriod =
            entries.firstOrNull { it.apiValue == value } ?: Day
    }
}
