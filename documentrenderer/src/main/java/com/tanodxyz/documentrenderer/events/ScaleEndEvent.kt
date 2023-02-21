package com.tanodxyz.documentrenderer.events

import android.view.MotionEvent

open class ScaleEndEvent(motionEvent: MotionEvent?) : GenericMotionEvent(motionEvent) {
    override fun toString(): String {
        return "ScaleEndEvent: x=${getX()} | y=${getY()}"
    }
}