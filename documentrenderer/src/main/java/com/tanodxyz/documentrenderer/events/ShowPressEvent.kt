package com.tanodxyz.documentrenderer.events

import android.view.MotionEvent

open class ShowPressEvent(motionEvent: MotionEvent?) : GenericMotionEvent(motionEvent) {
    override fun toString(): String {
        return "ShowPressEvent: xPadding=${getX()} | yPadding=${getY()}"
    }
}