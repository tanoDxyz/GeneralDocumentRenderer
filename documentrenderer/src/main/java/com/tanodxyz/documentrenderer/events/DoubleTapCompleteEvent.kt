package com.tanodxyz.documentrenderer.events

import android.view.MotionEvent

open class DoubleTapCompleteEvent(motionEvent: MotionEvent?) : GenericMotionEvent(motionEvent) {
    override fun toString(): String {
        return "DoubleTapCompleteEvent: leftMargin=${getX()} | topMargin=${getY()}"
    }
}