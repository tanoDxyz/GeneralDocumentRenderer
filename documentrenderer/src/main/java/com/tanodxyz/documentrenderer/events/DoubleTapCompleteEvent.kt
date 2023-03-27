package com.tanodxyz.documentrenderer.events

import android.view.MotionEvent

open class DoubleTapCompleteEvent(motionEvent: MotionEvent?) : DoubleTapEvent(motionEvent) {
    override fun toString(): String {
        return "DoubleTapCompleteEvent: x=${getX()} | y=${getY()}"
    }
}