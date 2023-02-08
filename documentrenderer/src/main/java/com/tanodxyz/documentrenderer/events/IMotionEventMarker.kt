package com.tanodxyz.documentrenderer.events

interface IMotionEventMarker {
    fun getX(): Float
    fun getY(): Float
    fun hasNoMotionEvent(): Boolean
}