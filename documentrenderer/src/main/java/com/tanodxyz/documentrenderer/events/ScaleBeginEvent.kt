package com.tanodxyz.documentrenderer.events

import android.view.MotionEvent

open class ScaleBeginEvent(motionEvent: MotionEvent?) : GenericMotionEvent(motionEvent) {
    override fun toString(): String {
        return "ScaleBeginEvent: x=${getX()} | y=${getY()}"
    }
}