package com.tanodxyz.documentrenderer.events

interface IEvent {
    fun getX(): Float
    fun getY(): Float
    fun hasNoMotionEvent(): Boolean
}