package com.tanodxyz.documentrenderer

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent

class DragPinchManager(context: Context, private val movementAndZoomHandler: MovementAndZoomHandler):
    GestureDetector.SimpleOnGestureListener() {

    private val animationManager = AnimationManager(movementAndZoomHandler)
    private val gestureDetector = GestureDetector(context,this)
    fun onTouchEvent(motionEvent: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(motionEvent)
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
        println("Bako: x&y $distanceX $distanceY")
        return true
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
}