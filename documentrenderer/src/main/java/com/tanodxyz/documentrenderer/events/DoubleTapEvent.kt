package com.tanodxyz.documentrenderer.events

import android.view.MotionEvent

open class DoubleTapEvent(motionEvent: MotionEvent?) : GenericMotionEvent(motionEvent) {
    override fun toString(): String {
        return "DoubleTapEvent: leftMargin=${getX()} | topMargin=${getY()}"
    }
}