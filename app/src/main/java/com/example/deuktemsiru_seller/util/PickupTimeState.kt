package com.example.deuktemsiru_seller.util

data class PickupTimeState(
    var startMinutes: Int = 16 * 60 + 30,
    var endMinutes: Int = 18 * 60,
) {
    fun ensureEndAfterStart(minDurationMinutes: Int) {
        if (endMinutes <= startMinutes) {
            endMinutes = (startMinutes + minDurationMinutes).coerceAtMost(LAST_MINUTE_OF_DAY)
        }
    }

    fun setDuration(minutes: Int) {
        endMinutes = (startMinutes + minutes).coerceAtMost(LAST_MINUTE_OF_DAY)
    }

    val startLabel: String get() = startMinutes.toClockTime()
    val endLabel: String get() = endMinutes.toClockTime()
    val rangeLabel: String get() = "$startLabel - $endLabel"

    companion object {
        const val LAST_MINUTE_OF_DAY = 23 * 60 + 59
    }
}
