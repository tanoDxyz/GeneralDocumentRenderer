package com.tanodxyz.documentrenderer

import android.content.Context
import android.graphics.PointF
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.tanodxyz.documentrenderer.DocumentRenderView.Companion.MAXIMUM_ZOOM
import com.tanodxyz.documentrenderer.DocumentRenderView.Companion.MINIMUM_ZOOM
import com.tanodxyz.documentrenderer.events.EventManager

class TouchEventsManager(val context: Context) :
    GestureDetector.SimpleOnGestureListener(),
    ScaleGestureDetector.OnScaleGestureListener {
    private var scaling: Boolean = false
    private var eventsListener: TouchEventsListener? = null
    private val scaleGestureDetector = ScaleGestureDetector(context, this)
    private val gestureDetector = GestureDetector(context, this)
    var enabled = true
    var scrollingEnabled = true
    var flingingEnabled = true
    var scalingEnabled = true

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
            if (scrolling && scrollingEnabled) {
                finishScroll(motionEvent)
            }
        }
        return retVal
    }

    private fun finishScroll(motionEvent: MotionEvent?) {
        scrolling = false
        eventsListener?.onScrollEnd(motionEvent)
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (scrollingEnabled) {
            // determine scroll direction
            return if (eventsListener == null) {
                false
            } else {
                val previousY = eventsListener!!.getCurrentY()
                val previousX = eventsListener!!.getCurrentX()
                val currentY = previousY + (-distanceY)
                val currentX = previousX + (-distanceX)
                val movementDirections =
                    calculateScrollDirections(previousX, previousY, currentX, currentY)
                scrolling = true
                eventsListener?.onScrollStart(
                    e1, e2,
                    movementDirections,
                    -distanceX,
                    -distanceY,
                    currentX,
                    currentY
                )
                true
            }
        }
        return false
    }

    fun calculateScrollDirections(
        previousX: Float,
        previousY: Float,
        currentX: Float,
        currentY: Float
    ): MovementDirections {
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
        return movementDirections
    }

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        if (scalingEnabled) {
            var dr = detector!!.scaleFactor
            val wantedZoom: Float = (eventsListener?.getCurrentZoom() ?: 1F) * dr
            val minZoom: Float =
                Math.min(MINIMUM_ZOOM, eventsListener?.getMinZoom() ?: MINIMUM_ZOOM)
            val maxZoom: Float =
                Math.min(MAXIMUM_ZOOM, eventsListener?.getMaxZoom() ?: MAXIMUM_ZOOM)
            val currentZoom = eventsListener?.getCurrentZoom() ?: MINIMUM_ZOOM
            if (wantedZoom < minZoom) {
                dr = minZoom / currentZoom
            } else if (wantedZoom > maxZoom) {
                dr = maxZoom / currentZoom
            }
            eventsListener?.zoomCenteredRelativeTo(dr, PointF(detector!!.focusX, detector!!.focusY))
            return true
        } else {
            return false
        }
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        eventsListener?.onSingleTapUp(e)
        return false
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        eventsListener?.onDoubleTapEvent(e)
        return false
    }

    override fun onLongPress(e: MotionEvent?) {
        eventsListener?.onLongPress(e)
    }

    override fun onDown(e: MotionEvent?): Boolean {
        eventsListener?.onDownEvent()
        return true
    }

    override fun onShowPress(e: MotionEvent?) {
        eventsListener?.onShowPress(e)
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        if (scalingEnabled) {
            eventsListener?.apply {
                if (getCurrentZoom() < getMidZoom()) {
                    // zoom with animation
                    zoomWithAnimation(e!!.x, e.y, getMidZoom())
                } else if (getCurrentZoom() < getMaxZoom()) {
                    // zoom with animation
                    zoomWithAnimation(e!!.x, e.y, getMaxZoom())
                } else {
                    resetZoomWithAnimation()
                }
                this.onDoubleTap(e)
            }
            return true
        } else {
            eventsListener?.onDoubleTap(e)
            return false
        }
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        eventsListener?.onSingleTapConfirmed(e)
        return true
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return if (flingingEnabled) {
            return eventsListener?.onFling(e1, e2, velocityX, velocityY) ?: false
        } else {
            false
        }
    }

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        return if (scalingEnabled) {
            scaling = true
            eventsListener?.onScaleBegin()
            true
        } else {
            false
        }
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        scaling = false
        eventsListener?.onScaleEnd()
    }

    interface TouchEventsListener {
        fun getCurrentX(): Float
        fun getCurrentY(): Float
        fun getCurrentZoom(): Float
        fun getMinZoom(): Float
        fun getMidZoom(): Float
        fun getMaxZoom(): Float
        fun isZooming(): Boolean
        fun onScrollStart(
            downEvent: MotionEvent?,
            moveEvent: MotionEvent?,
            movementDirections: MovementDirections? = null,
            distanceX: Float,
            distanceY: Float,
            absoluteX: Float,
            absoluteY: Float
        )

        fun onScrollEnd(motionEvent: MotionEvent?)

        fun onFling(
            downEvent: MotionEvent?,
            moveEvent: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean

        fun onDownEvent()
        fun zoomCenteredRelativeTo(dr: Float, pointF: PointF)

        fun resetZoomWithAnimation()
        fun zoomWithAnimation(centerX: Float, centerY: Float, scale: Float)
        fun zoomWithAnimation(scale: Float)
        fun onDoubleTap(e: MotionEvent?)
        fun onScaleEnd()
        fun onScaleBegin()
        fun onSingleTapConfirmed(e: MotionEvent?)
        fun onLongPress(e: MotionEvent?)
        fun onDoubleTapEvent(e: MotionEvent?)
        fun onSingleTapUp(e: MotionEvent?)
        fun onShowPress(e: MotionEvent?)
    }

    data class MovementDirections(
        var left: Boolean = false,
        var top: Boolean = false,
        var right: Boolean = false,
        var bottom: Boolean = false
    )

    data class Settings(
        val minZoom: Float = DocumentRenderView.DEFAULT_MIN_SCALE,
        val midZoom: Float = DocumentRenderView.DEFAULT_MID_SCALE,
        val maxZoom: Float = DocumentRenderView.DEFAULT_MAX_SCALE
    )

}