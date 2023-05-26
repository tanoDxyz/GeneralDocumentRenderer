package com.tanodxyz.documentrenderer.events

import android.view.MotionEvent

open class ScaleEvent(
    motionEvent: MotionEvent?,
    val scaleFactor: Float,
    val focusX: Float,
    val focusY: Float
) : GenericMotionEvent(motionEvent) {
    override fun toString(): String {
        return "ScaleEvent: leftMargin=${getX()} | topMargin=${getY()}"
    }
}