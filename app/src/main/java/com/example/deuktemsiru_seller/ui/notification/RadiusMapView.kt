package com.example.deuktemsiru_seller.ui.notification

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class RadiusMapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var radiusKm: Int = 3
        set(value) { field = value; invalidate() }

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE8F0E9.toInt()
        style = Paint.Style.FILL
    }
    private val roadPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    private val circleFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x332E7D32
        style = Paint.Style.FILL
    }
    private val circleStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF2E7D32.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF2E7D32.toInt()
        style = Paint.Style.FILL
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF2E7D32.toInt()
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val locationLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val locationBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF2E7D32.toInt()
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        val cx = w / 2f; val cy = h / 2f
        val maxRadius = min(w, h) * 0.42f

        // background
        canvas.drawRect(0f, 0f, w, h, bgPaint)

        // road grid
        val gridStep = 40f
        var x = 0f
        while (x < w) { canvas.drawLine(x, 0f, x, h, roadPaint); x += gridStep }
        var y = 0f
        while (y < h) { canvas.drawLine(0f, y, w, y, roadPaint); y += gridStep }

        // radius circle
        val radiusPx = when (radiusKm) { 3 -> maxRadius * 0.4f; 5 -> maxRadius * 0.65f; else -> maxRadius }
        canvas.drawCircle(cx, cy, radiusPx, circleFillPaint)
        canvas.drawCircle(cx, cy, radiusPx, circleStrokePaint)

        // radius label
        labelPaint.textSize = 11f * resources.displayMetrics.density
        canvas.drawText("${radiusKm}km", cx + radiusPx * 0.7f, cy - 4f, labelPaint)

        // pin
        val pinR = 10f * resources.displayMetrics.density
        canvas.drawCircle(cx, cy, pinR, pinPaint)
        canvas.drawCircle(cx, cy, pinR * 0.4f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; style = Paint.Style.FILL })

        // location label
        val labelText = "한국공학대학교"
        locationLabelPaint.textSize = 10f * resources.displayMetrics.density
        val textW = locationLabelPaint.measureText(labelText)
        val labelTop = cy - pinR * 2.5f - locationLabelPaint.textSize
        val rect = RectF(cx - textW/2 - 8, labelTop - locationLabelPaint.textSize, cx + textW/2 + 8, labelTop + 4)
        canvas.drawRoundRect(rect, 6f, 6f, locationBgPaint)
        canvas.drawText(labelText, cx, labelTop, locationLabelPaint)
    }
}
