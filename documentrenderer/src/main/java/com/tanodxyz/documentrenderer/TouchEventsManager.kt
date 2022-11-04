package com.tanodxyz.documentrenderer

import android.content.Context
import android.graphics.PointF
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.tanodxyz.documentrenderer.DocumentRenderView.Companion.MAXIMUM_ZOOM
import com.tanodxyz.documentrenderer.DocumentRenderView.Companion.MINIMUM_ZOOM

class TouchEventsManager(val context: Context, val settings: Settings = Settings()) :
    GestureDetector.SimpleOnGestureListener(),
    ScaleGestureDetector.OnScaleGestureListener {
    private var scaling: Boolean = false
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
            println("p0i: previousX = $previousX | previousY = $previousY | scrollX = $distanceX | scrollY = $distanceY | absX = $currentX | absy = $currentY")
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
        var dr = detector!!.scaleFactor
        val wantedZoom: Float = (eventsListener?.getCurrentZoom() ?: 1F) * dr
        val minZoom: Float = Math.min(MINIMUM_ZOOM, eventsListener?.getMinZoom() ?: MINIMUM_ZOOM)
        val maxZoom: Float = Math.min(MAXIMUM_ZOOM, eventsListener?.getMaxZoom() ?: MAXIMUM_ZOOM)
        val currentZoom = eventsListener?.getCurrentZoom() ?: MINIMUM_ZOOM
        if (wantedZoom < minZoom) {
            dr = minZoom / currentZoom
        } else if (wantedZoom > maxZoom) {
            dr = maxZoom / currentZoom
        }
        eventsListener?.zoomCenteredRelativeTo(dr, PointF(detector!!.focusX, detector!!.focusY))
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
        return eventsListener?.onFling(e1, e2, velocityX, velocityY) ?: false
    }

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        scaling = true
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        scaling = false
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
        fun zoomCenteredRelativeTo(dr: Float, pointF: PointF)
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