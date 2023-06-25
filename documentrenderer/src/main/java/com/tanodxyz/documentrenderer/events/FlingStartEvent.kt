package com.tanodxyz.documentrenderer.events

import android.view.MotionEvent

open class FlingStartEvent(
    val downEvent: MotionEvent?,
    val moveEvent: MotionEvent?,
    val velocityX: Float,
    val velocityY: Float
) : IMotionEventMarker {
    override fun getX(): Float {
        return moveEvent?.x ?: -1F
    }

    override fun getY(): Float {
        return moveEvent?.y ?: -1F
    }

    override fun hasNoMotionEvent(): Boolean {
        return moveEvent == null
    }

    override val event: MotionEvent?
        get() = moveEvent

    override fun toString(): String {
        return "FlingStartEvent: leftMargin=${getX()} | topMargin=${getY()}"
    }

}