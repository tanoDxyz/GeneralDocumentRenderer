package com.tanodxyz.documentrenderer.events

import android.view.MotionEvent

open class FlingEndEvent(motionEvent: MotionEvent?) : GenericMotionEvent(motionEvent) {
    override fun toString(): String {
        return "FlingEndEvent: x=${getX()} | y=${getY()}"
    }
}