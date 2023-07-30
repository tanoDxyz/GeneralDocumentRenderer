package com.tanodxyz.documentrenderer.elements


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import android.util.SparseArray
import com.tanodxyz.documentrenderer.R
import com.tanodxyz.documentrenderer.dpToPx
import com.tanodxyz.documentrenderer.spToPx

/**
 * An [IElement] that can be attached to [com.tanodxyz.documentrenderer.DocumentRenderView].
 * it basically shows indeterminate loader with some text on the right side.
 */
class DefaultCircularProgressBarElement(val context: Context) : IElement {
    var textToDisplay: String = context.getString(R.string.loading)
    var parentArcColor = Color.WHITE
    var textColor = Color.WHITE
    var textSize = context.resources.spToPx(18)
    var fillArcColor = Color.MAGENTA
    private val ovalSpace = RectF()
    var currentPercentage = DEFAULT_PERCENTAGE_VALUE
    var ovalSize = context.resources.dpToPx(24)
    var parentArcStrokeSize = context.resources.dpToPx(6)
    var fillArcStrokeSize = context.resources.dpToPx(6)
    var startAngle = 270
    val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = this@DefaultCircularProgressBarElement.textSize
        color = textColor
        isFakeBoldText = true
    }
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

    override fun draw(canvas: Canvas, args: SparseArray<Any>?) {
        setSpace(canvas.width.toFloat(), canvas.height.toFloat())
        ++currentPercentage
        --startAngle
        if (getCurrentPercentageToFill() > 360) {
            currentPercentage = DEFAULT_PERCENTAGE_VALUE
        }

        canvas.let {
            drawBackgroundArc(it)
            drawInnerArc(it)
            drawText(it)
        }
    }

    private fun drawText(canvas: Canvas) {
        val textWidth = textPaint.measureText(textToDisplay)
        val halfWidth = canvas.width.div(2)
        val textDrawX = halfWidth - (textWidth.div(2))
        val textDrawY = ovalSpace.bottom + context.resources.dpToPx(24)
        canvas.drawText(textToDisplay, textDrawX, textDrawY, textPaint)
    }

    private fun getCurrentPercentageToFill() =
        (360 * (currentPercentage / 100))

    private fun drawBackgroundArc(it: Canvas) {
        it.drawArc(ovalSpace, 0f, 360f, false, parentArcPaint)
    }

    private fun drawInnerArc(canvas: Canvas) {
        val percentageToFill = getCurrentPercentageToFill()
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