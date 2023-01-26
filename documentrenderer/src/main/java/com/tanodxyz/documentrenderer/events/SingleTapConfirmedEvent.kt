package com.tanodxyz.documentrenderer.events

import android.view.MotionEvent

open class SingleTapConfirmedEvent(motionEvent: MotionEvent?) : GenericMotionEvent(motionEvent) {
    override fun toString(): String {
        return "SingleTapConfirmedEvent: x=${getX()} | y=${getY()}"
    }
}