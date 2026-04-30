package com.example.deuktemsiru_seller.ui.sales

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.deuktemsiru_seller.R

class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    data class Entry(val label: String, val value: Int, val isHighlighted: Boolean = false)

    private var entries: List<Entry> = emptyList()

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 10f * resources.displayMetrics.density
    }
    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val barRect = RectF()
    private val cornerRadius = 6f * resources.displayMetrics.density

    fun setEntries(data: List<Entry>) {
        entries = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (entries.isEmpty()) return

        val labelAreaHeight = labelPaint.textSize + 4f * resources.displayMetrics.density
        val barAreaHeight = height - labelAreaHeight
        val maxVal = entries.maxOf { it.value }.coerceAtLeast(1)
        val slotWidth = width.toFloat() / entries.size
        val barWidth = slotWidth * 0.45f

        entries.forEachIndexed { i, entry ->
            val centerX = slotWidth * i + slotWidth / 2f
            val barHeight = (entry.value.toFloat() / maxVal) * barAreaHeight * 0.88f
            val minBarHeight = if (entry.value > 0) cornerRadius * 2 else 0f
            val actualBarHeight = barHeight.coerceAtLeast(minBarHeight)

            if (entry.value > 0) {
                barPaint.color = if (entry.isHighlighted) {
                    ContextCompat.getColor(context, R.color.primary)
                } else {
                    ContextCompat.getColor(context, R.color.primary_light)
                }
                barRect.set(
                    centerX - barWidth / 2f,
                    barAreaHeight - actualBarHeight,
                    centerX + barWidth / 2f,
                    barAreaHeight,
                )
                canvas.drawRoundRect(barRect, cornerRadius, cornerRadius, barPaint)
            } else {
                emptyPaint.color = ContextCompat.getColor(context, R.color.border)
                barRect.set(
                    centerX - barWidth / 2f,
                    barAreaHeight - cornerRadius * 2,
                    centerX + barWidth / 2f,
                    barAreaHeight,
                )
                canvas.drawRoundRect(barRect, cornerRadius, cornerRadius, emptyPaint)
            }

            labelPaint.color = if (entry.isHighlighted) {
                ContextCompat.getColor(context, R.color.primary)
            } else {
                ContextCompat.getColor(context, R.color.text_muted)
            }
            labelPaint.isFakeBoldText = entry.isHighlighted
            canvas.drawText(entry.label, centerX, height.toFloat(), labelPaint)
        }
    }
}
