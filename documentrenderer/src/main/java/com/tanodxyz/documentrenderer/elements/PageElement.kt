package com.tanodxyz.documentrenderer.elements

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.tanodxyz.documentrenderer.events.IMotionEventMarker
import com.tanodxyz.documentrenderer.page.DocumentPage

open class PageElement(
    var moveable: Boolean = false,
    var resizable: Boolean = false,
    val layoutParams: LayoutParams = LayoutParams(),
    val page: DocumentPage
) : IElement {


    val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.MAGENTA
        textSize = 12F
    }

    open fun onEvent(iMotionEventMarker: IMotionEventMarker?): Boolean {
        return false
    }

    override fun draw(canvas: Canvas) {
        val pageBounds = page.pageBounds
        val left = pageBounds.left + page.documentRenderView.toCurrentScale(layoutParams.x)
        val top = pageBounds.top + page.documentRenderView.toCurrentScale(layoutParams.y)
        val right = left + page.documentRenderView.toCurrentScale(layoutParams.width)
        val bottom = top + page.documentRenderView.toCurrentScale(layoutParams.height)
        
        canvas.apply {
            drawRect(left,top,right,bottom,paint)
        }
    }

    class LayoutParams(var width: Int = 0, var height: Int = 0) {
        var x = 0F
        var y = 0F
        var padding: RectF = RectF(0F, 0F, 0F, 0F)
    }
}