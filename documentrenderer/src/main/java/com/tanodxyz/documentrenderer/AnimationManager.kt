package com.tanodxyz.documentrenderer

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.graphics.PointF
import android.view.animation.DecelerateInterpolator


class AnimationManager(private val animationListener: AnimationListener) {
    private var valueAnimator: ValueAnimator? = null

    //    private val scroller: OverScroller
    private var flinging = false
    private var pageFlingScroll = false

    val isScrollOrFling: Boolean get() = pageFlingScroll

    fun startXAnimation(
        xFrom: Float,
        xTo: Float,
        movementDirections: TouchEventsManager.MovementDirections
    ) {
        stopAll()
        valueAnimator = ValueAnimator.ofFloat(xFrom, xTo).apply {
            val xAnimation = XAnimation(movementDirections)
            setInterpolator(DecelerateInterpolator())
            addUpdateListener(xAnimation)
            addListener(xAnimation)
            setDuration(400)
            start()
        }
    }

    fun startYAnimation(
        yFrom: Float,
        yTo: Float,
        movementDirections: TouchEventsManager.MovementDirections?
    ) {
        stopAll()
        valueAnimator = ValueAnimator.ofFloat(yFrom, yTo).apply {
            val yAnimation = YAnimation(movementDirections)
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

    fun stopAll() {
        if (valueAnimator != null) {
            valueAnimator!!.cancel()
            valueAnimator = null
        }
    }


    internal inner class XAnimation(val movementDirections: TouchEventsManager.MovementDirections) :
        AnimatorListenerAdapter(),
        AnimatorUpdateListener {
        override fun onAnimationUpdate(animation: ValueAnimator) {
            val offset = animation.animatedValue as Float
            animationListener.moveTo(offset, animationListener.getCurrentY(), movementDirections)
        }

        override fun onAnimationCancel(animation: Animator) {
            pageFlingScroll = false
        }

        override fun onAnimationEnd(animation: Animator) {
            pageFlingScroll = false
        }
    }

    internal inner class YAnimation(val movementDirections: TouchEventsManager.MovementDirections?) :
        AnimatorListenerAdapter(),
        AnimatorUpdateListener {
        override fun onAnimationUpdate(animation: ValueAnimator) {
            val offset = animation.animatedValue as Float
            animationListener.moveTo(animationListener.getCurrentX(), offset, movementDirections)
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
        fun moveTo(
            offsetX: Float,
            offsetY: Float,
            movementDirections: TouchEventsManager.MovementDirections? = null
        )

        fun getCurrentX(): Float
        fun getCurrentY(): Float
        fun zoomCenteredTo(zoom: Float, pivot: PointF)
    }
}
