package com.tanodxyz.documentrenderer.elements

import android.graphics.Canvas
import android.graphics.RectF
import com.tanodxyz.documentrenderer.events.IMotionEventMarker

interface IElement {
    fun draw(canvas: Canvas){}
}