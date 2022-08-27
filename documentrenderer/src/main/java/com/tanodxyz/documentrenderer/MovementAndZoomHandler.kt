package com.tanodxyz.documentrenderer

import android.graphics.PointF
import android.graphics.RectF

interface MovementAndZoomHandler {
    fun moveTo(offsetX: Float, offsetY: Float)
    fun moveRelative(deltaX: Float, deltaY: Float)
    fun scrollTo(deltaX: Float, deltaY: Float, scrollDirections: DragPinchManager.ScrollDirections)
    fun moveToTopWithAnimation(startY:Float)
    fun getBottomBounds():RectF
    fun getCurrentX(): Float
    fun getCurrentY(): Float
    fun zoomCenteredTo(zoom: Float, pivot: PointF)
    fun setCurrentY(y:Float)
    fun onScrollStart(direction: DragPinchManager.ScrollDirections)
    fun onScrollEnd()
}