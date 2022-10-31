package com.tanodxyz.documentrenderer

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector

class TouchEventsManager(val context: Context) : GestureDetector.SimpleOnGestureListener(),
    ScaleGestureDetector.OnScaleGestureListener {
    private var eventsListener: TouchEventsListener? = null
    private val scaleGestureDetector = ScaleGestureDetector(context, this)
    private val gestureDetector = GestureDetector(context, this)

    private var enabled = true
    private var scrolling = false
    fun registerListener(eventsListener: TouchEventsListener) {
        this.eventsListener = eventsListener
    }

    fun onTouchEvent(motionEvent: MotionEvent?): Boolean {

        if (!enabled) {
            return false
        }

        var retVal = scaleGestureDetector.onTouchEvent(motionEvent)
        retVal = gestureDetector.onTouchEvent(motionEvent) || retVal

        if (motionEvent!!.action == MotionEvent.ACTION_UP) {
            if (scrolling) {
                finishScroll()
            }
        }
        return retVal
    }

    private fun finishScroll() {
        scrolling = false
        eventsListener?.onScrollEnd()
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        // determine scroll direction
        return if (eventsListener == null) {
            false
        } else {
            val previousY = eventsListener!!.getCurrentY()
            val previousX = eventsListener!!.getCurrentX()
            val currentY = previousY + (-distanceY)
            val currentX = previousX + (-distanceX)
            val movementDirections = MovementDirections()
            if (previousY <= -1) {
                if (currentY <= previousY) movementDirections.top = true
                else movementDirections.bottom = true
            } else {
                if (currentY <= previousY) movementDirections.top = true
                else movementDirections.bottom = true
            }
            if (previousX <= -1) {
                if (currentX <= previousX) movementDirections.left = true
                else movementDirections.right = true
            } else {
                if (currentX <= previousX) movementDirections.left = true
                else movementDirections.right = true
            }
            scrolling = true
            eventsListener?.onScrollStart(
                movementDirections,
                -distanceX,
                -distanceY,
                currentX,
                currentY
            )
            true
        }

    }

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        return true
    }

    override fun onDown(e: MotionEvent?): Boolean {
        eventsListener?.onStopFling()
        return true
    }
    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        return true
    }
    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return eventsListener?.onFling(e1,e2,velocityX,velocityY) ?: false
    }
    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
    }

    interface TouchEventsListener {
        fun getCurrentX(): Float
        fun getCurrentY(): Float
        fun onScrollStart(
            movementDirections: MovementDirections? = null,
            distanceX: Float,
            distanceY: Float,
            absoluteX: Float,
            absoluteY: Float
        )
        fun onScrollEnd()

        fun onFling(
            downEvent: MotionEvent?,
            moveEvent: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean

        fun onStopFling()
    }

    data class MovementDirections(
        var left: Boolean = false,
        var top: Boolean = false,
        var right: Boolean = false,
        var bottom: Boolean = false
    )
}