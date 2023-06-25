package com.tanodxyz.documentrenderer.events

import android.view.MotionEvent

interface IMotionEventMarker {
    fun getX(): Float
    fun getY(): Float
    fun hasNoMotionEvent(): Boolean

    val event:MotionEvent?
}