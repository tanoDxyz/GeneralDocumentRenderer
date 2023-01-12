package com.tanodxyz.documentrenderer.elements

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.VectorDrawable
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.tanodxyz.documentrenderer.dpToPx
import java.security.SecureRandom

class DefaultCircularProgressBarElement(val context: Context) : IElement {
    private val colorArray = arrayOf(
        Color.BLACK, Color.GRAY,
        Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.BLUE, Color.DKGRAY, Color.GREEN, Color.RED
    )
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val secureRandom = SecureRandom()
    private var nextCircleRadius = 6
    private var numberOfCircles = 1

    init {
        circlePaint.style = Paint.Style.STROKE
        circlePaint.strokeCap = Paint.Cap.ROUND
        circlePaint.strokeWidth = context.resources.dpToPx(4)

    }

    override fun draw(canvas: Canvas) {
        val color = colorArray[secureRandom.nextInt(colorArray.count())]
        circlePaint.color = color
        canvas.apply {
            val halfWidth = canvas.width / 2F
            val halfHeight = canvas.height / 2F
            for(i:Int in 1 .. numberOfCircles) {
                drawCircle(halfWidth,halfHeight,context.resources.dpToPx((nextCircleRadius * i)),circlePaint)
            }
            if(numberOfCircles < MAX_NUMBER_OF_CIRCLES) {
                ++numberOfCircles
            } else {
                numberOfCircles = 1
            }
        }
    }

    companion object {
        const val MAX_NUMBER_OF_CIRCLES = 8
    }
}