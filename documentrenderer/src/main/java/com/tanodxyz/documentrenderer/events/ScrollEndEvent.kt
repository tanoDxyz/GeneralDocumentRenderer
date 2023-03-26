package com.tanodxyz.documentrenderer.events

import android.view.MotionEvent

open class ScrollEndEvent(motionEvent: MotionEvent?) : GenericMotionEvent(motionEvent) {
    override fun toString(): String {
        return "ScrollEndEvent: xPadding=${getX()} | yPadding=${getY()}"
    }
}