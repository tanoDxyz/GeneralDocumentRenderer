package com.tanodxyz.documentrenderer.events

import android.view.MotionEvent
import com.tanodxyz.documentrenderer.TouchEventsManager

open class ScrollStartEvent(
    val downEvent: MotionEvent?,
    val moveEvent: MotionEvent?,
    val movementDirections: TouchEventsManager.MovementDirections?,
    val distanceTraveledX: Float,
    val distanceTraveledY: Float,
    val absoluteX: Float,
    val absoluteY: Float
) : IMotionEventMarker {
    override fun getX(): Float {
        return moveEvent?.x ?: -1F
    }

    override fun getY(): Float {
        return moveEvent?.y ?: -1F
    }

    override fun hasNoMotionEvent(): Boolean {
        return moveEvent == null
    }

    override val event: MotionEvent?
        get() = moveEvent

    override fun toString(): String {
        return "ScrollStartEvent: leftMargin=${getX()} | topMargin=${getY()} | distanceTraveledX=$distanceTraveledX | distanceTraveledY=$distanceTraveledY"
    }
}