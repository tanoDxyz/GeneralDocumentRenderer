package com.tanodxyz.documentrenderer.events

import android.view.MotionEvent

open class GenericMotionEvent(var motionEvent: MotionEvent? = null) : IMotionEventMarker {
    override fun getX(): Float {
        return motionEvent?.x?:-1F
    }

    override fun getY(): Float {
        return motionEvent?.y?:-1F
    }

    override fun hasNoMotionEvent(): Boolean {
        return motionEvent == null
    }

    override fun toString(): String {
        return "GenericMotionEvent: x=${getX()} | y=${getY()} action=${motionEvent?.action}"
    }
}