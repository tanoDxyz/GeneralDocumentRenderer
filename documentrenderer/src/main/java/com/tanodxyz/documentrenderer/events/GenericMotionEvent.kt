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

    override val event: MotionEvent?
        get() = motionEvent

    override fun toString(): String {
        return "GenericMotionEvent: leftMargin=${getX()} | topMargin=${getY()} action=${motionEvent?.action}"
    }
}