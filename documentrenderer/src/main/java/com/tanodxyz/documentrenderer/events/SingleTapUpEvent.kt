package com.tanodxyz.documentrenderer.events

import android.view.MotionEvent

open class SingleTapUpEvent(motionEvent: MotionEvent?) : GenericMotionEvent(motionEvent) {
    override fun toString(): String {
        return "SingleTapUpEvent: xPadding=${getX()} | yPadding=${getY()}"
    }
}