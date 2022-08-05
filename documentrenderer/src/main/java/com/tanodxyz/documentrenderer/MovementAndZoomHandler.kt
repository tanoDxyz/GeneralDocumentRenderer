package com.tanodxyz.documentrenderer

import android.graphics.PointF

interface MovementAndZoomHandler {
    fun moveTo(offsetX: Float, offsetY: Float)
    fun moveRelative(deltaX: Float, deltaY: Float)
    fun scrollTo(deltaX: Float, deltaY: Float, scrollDirection: DragPinchManager.ScrollDirection)
    fun getCurrentX(): Float
    fun getCurrentY(): Float
    fun zoomCenteredTo(zoom: Float, pivot: PointF)
    fun onScrollStart(direction: DragPinchManager.ScrollDirection)
    fun onScrollEnd()
}