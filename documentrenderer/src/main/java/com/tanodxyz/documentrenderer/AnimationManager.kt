package com.tanodxyz.documentrenderer

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.graphics.PointF
import android.view.animation.DecelerateInterpolator


class AnimationManager(private val movementAndZoomHandler: MovementAndZoomHandler) {
    private var valueAnimator: ValueAnimator? = null

    //    private val scroller: OverScroller
    private var flinging = false
    private var pageFlinging = false
    fun startXAnimation(xFrom: Float, xTo: Float) {
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

    fun startYAnimation(yFrom: Float, yTo: Float) {
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

    fun stopAll() {
        if (valueAnimator != null) {
            valueAnimator!!.cancel()
            valueAnimator = null
        }
    }


    internal inner class XAnimation : AnimatorListenerAdapter(),
        AnimatorUpdateListener {
        override fun onAnimationUpdate(animation: ValueAnimator) {
            val offset = animation.animatedValue as Float
            movementAndZoomHandler.moveTo(offset, movementAndZoomHandler.getCurrentY())
        }

        override fun onAnimationCancel(animation: Animator) {
            pageFlinging = false
        }

        override fun onAnimationEnd(animation: Animator) {
            pageFlinging = false
        }
    }

    internal inner class YAnimation : AnimatorListenerAdapter(),
        AnimatorUpdateListener {
        override fun onAnimationUpdate(animation: ValueAnimator) {
            val offset = animation.animatedValue as Float
            movementAndZoomHandler.moveTo(movementAndZoomHandler.getCurrentX(), offset)
        }

        override fun onAnimationCancel(animation: Animator) {
            pageFlinging = false
        }

        override fun onAnimationEnd(animation: Animator) {
            pageFlinging = false
        }
    }

    internal inner class ZoomAnimation(private val centerX: Float, private val centerY: Float) :
        AnimatorUpdateListener, Animator.AnimatorListener {
        override fun onAnimationUpdate(animation: ValueAnimator) {
            val zoom = animation.animatedValue as Float
            movementAndZoomHandler.zoomCenteredTo(zoom, PointF(centerX,centerY))
        }

        override fun onAnimationCancel(animation: Animator) {
        }

        override fun onAnimationEnd(animation: Animator) {
        }

        override fun onAnimationRepeat(animation: Animator) {}
        override fun onAnimationStart(animation: Animator) {}
    }
}
