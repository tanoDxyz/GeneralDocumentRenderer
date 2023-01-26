package com.tanodxyz.documentrenderer

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.PointF
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.OverScroller
import androidx.core.view.MotionEventCompat


class AnimationManager(context: Context, private val animationListener: AnimationListener) {
    private var valueAnimator: ValueAnimator? = null

    private var flinging = false
    private var pageFlingScroll = false
    var scroller: OverScroller? = null
    val isScrollOrFling: Boolean get() = pageFlingScroll

    init {
        scroller = OverScroller(context)
    }

    fun startXAnimation(
        xFrom: Float,
        xTo: Float
    ) {
        stopAll()
        valueAnimator = ValueAnimator.ofFloat(xFrom, xTo).apply {
            val xAnimation = XAnimation()
            setInterpolator(DecelerateInterpolator())
            addUpdateListener(xAnimation)
            addListener(xAnimation)
            setDuration(400)
            start()
        }
    }

    fun startPageFlingAnimation(targetOffset: Float) {
        if (animationListener.isSwipeVertical()) {
            startYAnimation(animationListener.getCurrentY(), targetOffset)
        } else {
            startXAnimation(animationListener.getCurrentX(), targetOffset)
        }
        pageFlingScroll = true
    }


    fun startYAnimation(
        yFrom: Float,
        yTo: Float
    ) {
        stopAll()
        valueAnimator = ValueAnimator.ofFloat(yFrom, yTo).apply {
            val yAnimation = YAnimation()
            setInterpolator(DecelerateInterpolator())
            addUpdateListener(yAnimation)
            addListener(yAnimation)
            setDuration(400)
            start()
        }
    }

    fun startZoomAnimation(centerX: Float, centerY: Float, zoomFrom: Float, zoomTo: Float) {
        stopAll()
        valueAnimator = ValueAnimator.ofFloat(zoomFrom, zoomTo).apply {
            setInterpolator(DecelerateInterpolator())
            val zoomAnim = ZoomAnimation(centerX, centerY)
            addUpdateListener(zoomAnim)
            addListener(zoomAnim)
            setDuration(400)
            start()
        }
    }

    fun startFlingAnimation(
        startX: Int,
        startY: Int,
        velocityX: Int,
        velocityY: Int,
        minX: Int,
        maxX: Int,
        minY: Int,
        maxY: Int
    ) {
        stopAll()
        flinging = true
        scroller!!.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY)
        animationListener.redraw()
    }

    fun computeScrollOffset() {
        if (scroller!!.computeScrollOffset()) {
            animationListener.moveTo(scroller!!.currX.toFloat(), scroller!!.currY.toFloat())
        } else if (flinging) {
            flinging = false
            animationListener.redraw()
            animationListener.flingFinished(
                MotionEvent.obtain(
                    System.nanoTime(),
                    System.nanoTime(),
                    MotionEvent.ACTION_UP,
                    scroller!!.currX.toFloat(),
                    scroller!!.currY.toFloat(),
                    -1
                )
            )
        }
    }

    fun getCurrentFlingX(): Int {
        return scroller!!.currX
    }

    fun getCurrentFlingY(): Int {
        return scroller!!.currY
    }

    fun stopFling() {
        flinging = false
        scroller!!.forceFinished(true)
    }

    fun isFlinging(): Boolean {
        return flinging /*|| pageFlinging*/
    }

    fun stopAll() {
        if (valueAnimator != null) {
            valueAnimator!!.cancel()
            valueAnimator = null
        }
        stopFling()
    }


    internal inner class XAnimation() :
        AnimatorListenerAdapter(),
        AnimatorUpdateListener {
        override fun onAnimationUpdate(animation: ValueAnimator) {
            val offset = animation.animatedValue as Float
            animationListener.moveTo(offset, animationListener.getCurrentY())
        }

        override fun onAnimationCancel(animation: Animator) {
            pageFlingScroll = false
        }

        override fun onAnimationEnd(animation: Animator) {
            pageFlingScroll = false
        }
    }

    internal inner class YAnimation() :
        AnimatorListenerAdapter(),
        AnimatorUpdateListener {
        override fun onAnimationUpdate(animation: ValueAnimator) {
            val offset = animation.animatedValue as Float
            animationListener.moveTo(animationListener.getCurrentX(), offset)
        }

        override fun onAnimationCancel(animation: Animator) {
            pageFlingScroll = false
        }

        override fun onAnimationEnd(animation: Animator) {
            pageFlingScroll = false
        }
    }

    internal inner class ZoomAnimation(private val centerX: Float, private val centerY: Float) :
        AnimatorUpdateListener, Animator.AnimatorListener {
        override fun onAnimationUpdate(animation: ValueAnimator) {
            val zoom = animation.animatedValue as Float
            animationListener.zoomCenteredTo(zoom, PointF(centerX, centerY))
        }

        override fun onAnimationCancel(animation: Animator) {
        }

        override fun onAnimationEnd(animation: Animator) {
        }

        override fun onAnimationRepeat(animation: Animator) {}
        override fun onAnimationStart(animation: Animator) {}
    }

    interface AnimationListener {
        fun moveTo(absX: Float, absY: Float, moveHandle: Boolean = true)
        fun getCurrentX(): Float
        fun getCurrentY(): Float
        fun zoomCenteredTo(zoom: Float, pivot: PointF)
        fun redraw()
        fun isSwipeVertical(): Boolean
        fun flingFinished(motionEvent: MotionEvent?)
    }
}
