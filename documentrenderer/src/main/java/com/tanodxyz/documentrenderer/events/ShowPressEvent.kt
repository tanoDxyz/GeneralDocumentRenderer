package com.tanodxyz.documentrenderer.events

import android.view.MotionEvent

open class ShowPressEvent(motionEvent: MotionEvent?) : GenericMotionEvent(motionEvent) {
    override fun toString(): String {
        return "ShowPressEvent: leftMargin=${getX()} | topMargin=${getY()}"
    }
}