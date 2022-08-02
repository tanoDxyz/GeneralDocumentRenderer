package com.tanodxyz.documentrenderer

import android.graphics.PointF

interface MovementAndZoomHandler {
    fun moveTo(offsetX: Float, offsetY: Float)
    fun getCurrentX(): Float
    fun getCurrentY(): Float
    fun zoomCenteredTo(zoom: Float, pivot: PointF)
}