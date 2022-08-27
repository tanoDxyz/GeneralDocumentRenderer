package com.tanodxyz.documentrenderer

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import java.util.*

class DragPinchManager(
    context: Context,
    private val movementAndZoomHandler: MovementAndZoomHandler,
    private val animationManager: AnimationManager
) :
    GestureDetector.SimpleOnGestureListener(), ScaleGestureDetector.OnScaleGestureListener {

    private val gestureDetector = GestureDetector(context, this)
    private val scaleGestureDetector = ScaleGestureDetector(context, this)

    private var scrolling = false
    private var scaling = false
    private var enabled = true

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

    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }

    override fun onContextClick(e: MotionEvent?): Boolean {
        return super.onContextClick(e)
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        return super.onDoubleTap(e)
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        return super.onDoubleTapEvent(e)
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return super.onFling(e1, e2, velocityX, velocityY)
    }

    override fun onLongPress(e: MotionEvent?) {
        super.onLongPress(e)
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        // determine scroll direction
        val previousY = movementAndZoomHandler.getCurrentY()
        val previousX = movementAndZoomHandler.getCurrentX()
        val currentY = previousY + (-distanceY)
        val currentX = previousX + (-distanceX)
        val scrollDirections = ScrollDirections()
            if (previousY <= -1) {
                if (currentY <= previousY) scrollDirections.top = true
                else scrollDirections.bottom = true
            } else {
                if (currentY <= previousY) scrollDirections.top = true
                else scrollDirections.bottom = true
            }
        if (previousX <= -1) {
            if (currentX <= previousX) scrollDirections.left = true
            else scrollDirections.right = true
        } else {
            if (currentX <= previousX) scrollDirections.left = true
            else scrollDirections.right = true
        }
        scrolling = true
        movementAndZoomHandler.onScrollStart(scrollDirections)
        movementAndZoomHandler.scrollTo(currentX, currentY, scrollDirections)
        return true
    }

    private fun finishScroll() {
        scrolling = false
        movementAndZoomHandler.onScrollEnd()
    }


    override fun onShowPress(e: MotionEvent?) {
        super.onShowPress(e)
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        return super.onSingleTapConfirmed(e)
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return super.onSingleTapUp(e)
    }

    fun release() {}
    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
    }

    data class ScrollDirections(
        var left: Boolean = false,
        var top: Boolean = false,
        var right: Boolean = false,
        var bottom: Boolean = false
    )
}