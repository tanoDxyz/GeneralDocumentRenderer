package com.tanodxyz.documentrenderer.elements

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.animation.AccelerateDecelerateInterpolator
import com.tanodxyz.documentrenderer.dpToPx
import java.security.SecureRandom

/**
 * subject to change
 */
class DefaultCircularProgressBarElement(val context: Context) : IElement {
    val parentArcColor = Color.WHITE
    val fillArcColor = Color.MAGENTA
    private val ovalSpace = RectF()
    var currentPercentage = DEFAULT_PERCENTAGE_VALUE
    var ovalSize = context.resources.dpToPx(24)
    var parentArcStrokeSize = context.resources.dpToPx(6)
    var fillArcStrokeSize = context.resources.dpToPx(6)
    var startAngle = 270
    val parentArcPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = parentArcColor
        strokeWidth = parentArcStrokeSize
    }

    private val fillArcPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = fillArcColor
        strokeWidth = fillArcStrokeSize
        // 1
        strokeCap = Paint.Cap.ROUND
    }


    override fun draw(canvas: Canvas) {
        setSpace(canvas.width.toFloat(), canvas.height.toFloat())
        ++currentPercentage
        --startAngle
        if(getCurrentPercentageToFill() > 360) {
            currentPercentage = DEFAULT_PERCENTAGE_VALUE
        }
        canvas.let {
            // 2
            drawBackgroundArc(it)
            // 3
            drawInnerArc(it)
        }
    }


    private fun getCurrentPercentageToFill() =
        (360 * (currentPercentage / 100))


    private fun drawBackgroundArc(it: Canvas) {
        it.drawArc(ovalSpace, 0f, 360f, false, parentArcPaint)
    }

    private fun drawInnerArc(canvas: Canvas) {
        // 4
        val percentageToFill = getCurrentPercentageToFill()
        // 5
        canvas.drawArc(ovalSpace, startAngle.toFloat(), percentageToFill, false, fillArcPaint)
    }

    private fun setSpace(width: Float, height: Float) {
        val horizontalCenter = (width.div(2))
        val verticalCenter = (height.div(2))
        ovalSpace.set(
            horizontalCenter - ovalSize,
            verticalCenter - ovalSize,
            horizontalCenter + ovalSize,
            verticalCenter + ovalSize
        )
    }


    companion object {
        const val DEFAULT_PERCENTAGE_VALUE = 10F
    }
}