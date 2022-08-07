package com.tanodxyz.documentrenderer

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.EdgeEffect
import android.widget.OverScroller
import androidx.core.view.ViewCompat
import androidx.core.widget.EdgeEffectCompat.getDistance

class AnotherView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private lateinit var paint: Paint
    val c = Rect(20, 20, 500, 1000)
    private var offsetX = 0
    init {
        init()
    }

    private val mScroller = OverScroller(this.context)

    private fun init() {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.setColor(Color.MAGENTA)
        paint.style = Paint.Style.FILL_AND_STROKE

        val gestureDetector =
            GestureDetector(this.context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean {
                    // Initiates the decay phase of any active edge effects.
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                        releaseEdgeEffects()
                    }
                    // Aborts any active scroll animations and invalidates.
                    mScroller.forceFinished(true)
                    ViewCompat.postInvalidateOnAnimation(this@AnotherView)
                    return true
                }

                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent?,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    scroll(-(distanceX.toInt()),-(distanceY.toInt()))
                    return true
                }
            })
        setOnTouchListener { view, event ->
            gestureDetector.onTouchEvent(event)
        }
    }

    fun scroll(deltaX: Int, deltaY: Int) {
// Initiates the decay phase of any active edge effects.
        // On Android 12 and higher, the edge effect (stretch) should
        // continue.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            releaseEdgeEffects()
        }

        // Before flinging, aborts the current animation.
        mScroller.forceFinished(true)
        // Begins the animation
        mScroller.startScroll(offsetX,0,deltaX,0)
        // Invalidates to trigger computeScroll()
        ViewCompat.postInvalidateOnAnimation(this)
    }
    fun releaseEdgeEffects() {

    }
    override fun computeScroll() {
        super.computeScroll()
        if(mScroller.computeScrollOffset()) {
            val currX = mScroller.currX
            offsetX = currX
            invalidate()
        }
    }
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            drawColor(Color.WHITE)
            c.left = offsetX
            drawRect(c, paint)
        }
    }
}