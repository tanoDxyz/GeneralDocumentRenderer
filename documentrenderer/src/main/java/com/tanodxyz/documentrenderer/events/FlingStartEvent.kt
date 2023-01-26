package com.tanodxyz.documentrenderer.events

import android.view.MotionEvent

open class FlingStartEvent(
    val downEvent: MotionEvent?,
    val moveEvent: MotionEvent?,
    val velocityX: Float,
    val velocityY: Float
) : IEvent {
    override fun getX(): Float {
        return moveEvent?.x ?: -1F
    }

    override fun getY(): Float {
        return moveEvent?.y ?: -1F
    }

    override fun toString(): String {
        return "FlingStartEvent: x=${getX()} | y=${getY()}"
    }

}