package com.tanodxyz.documentrenderer.events

import android.view.MotionEvent

open class LongPressEvent(motionEvent: MotionEvent?) : GenericMotionEvent(motionEvent) {
    override fun toString(): String {
        return "LongPressEvent: leftMargin=${getX()} | topMargin=${getY()}"
    }
}