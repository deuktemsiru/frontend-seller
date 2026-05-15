package com.example.deuktemsiru_seller.ui.sales

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class Slice(val label: String, val value: Int)

    private var slices: List<Slice> = emptyList()
    private val colors = listOf(
        0xFF2E7D32.toInt(), 0xFF4CAF50.toInt(), 0xFF81C784.toInt(),
        0xFF1565C0.toInt(), 0xFF42A5F5.toInt(),
    )
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val oval = RectF()

    fun setSlices(data: List<Slice>) { slices = data; invalidate() }

    override fun onDraw(canvas: Canvas) {
        if (slices.isEmpty()) return
        val total = slices.sumOf { it.value }.toFloat().coerceAtLeast(1f)
        val size = min(width, height).toFloat()
        val cx = width / 2f; val cy = height / 2f; val r = size / 2f * 0.85f
        oval.set(cx - r, cy - r, cx + r, cy + r)
        var startAngle = -90f
        textPaint.textSize = r * 0.13f
        slices.forEachIndexed { i, slice ->
            val sweep = slice.value / total * 360f
            paint.color = colors[i % colors.size]
            canvas.drawArc(oval, startAngle, sweep, true, paint)
            // label at midpoint
            val mid = Math.toRadians((startAngle + sweep / 2).toDouble())
            val lx = (cx + r * 0.65f * Math.cos(mid)).toFloat()
            val ly = (cy + r * 0.65f * Math.sin(mid)).toFloat() + textPaint.textSize / 3
            val pct = "%.0f%%".format(slice.value / total * 100)
            canvas.drawText(pct, lx, ly, textPaint)
            startAngle += sweep
        }
    }
}
