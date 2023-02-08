package com.tanodxyz.documentrenderer.elements

import android.graphics.Canvas
import android.graphics.RectF
import com.tanodxyz.documentrenderer.events.IMotionEventMarker
import com.tanodxyz.documentrenderer.page.DocumentPage

abstract class PageElement : IElement {
    var layoutParams: LayoutParams? = null
    abstract fun draw(canvas: Canvas, page: DocumentPage, args: HashMap<String, Any?>? = null)
    fun onEvent(iMotionEventMarker: IMotionEventMarker) {}
    class LayoutParams(var width: Int = 0, var height: Int = 0) {
        var x: Float = 0F
        var y: Float = 0F
        var bounds: RectF = RectF(0F, 0F, 0F, 0F)
        var padding:RectF = RectF(0F, 0F, 0F, 0F)
    }
}