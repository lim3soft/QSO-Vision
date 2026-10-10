package com.mmdvm.qsovision.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.SystemClock
import android.view.View
import kotlin.math.*

/** PTT-driven activity animation, not a measured audio spectrum. */
class ActivityWaveView(context: Context) : View(context) {
    private val ink = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = resources.displayMetrics.density * 1.2f; strokeCap = Paint.Cap.ROUND }
    var active = false
        set(value) { if (field != value) { field = value; invalidate() } }
    init { contentDescription = "Transmission activity animation" }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val phase = SystemClock.uptimeMillis() / 280.0
        val count = 65
        for (i in 0 until count) {
            val x = (i + 1f) * width / (count + 1)
            val envelope = sin(Math.PI * (i + 1) / (count + 1))
            val motion = abs(sin(i * .39 - phase) * cos(i * .11 + phase * .57))
            val half = if (active) (2 + height * .43 * envelope * motion).toFloat() else 1.5f
            ink.color = if (active) 0xff40f5ff.toInt() else 0xff346276.toInt()
            canvas.drawLine(x, height / 2f - half, x, height / 2f + half, ink)
        }
        if (active && isShown && windowVisibility == VISIBLE) postInvalidateDelayed(33)
    }
}
