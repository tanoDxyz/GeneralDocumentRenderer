package com.tanodxyz.documentrenderer.elements

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import com.tanodxyz.documentrenderer.events.IMotionEventMarker
import com.tanodxyz.documentrenderer.page.DocumentPage

open class PageElement(
    val layoutParams: LayoutParams = LayoutParams(),
    val page: DocumentPage? = null
) : IElement {

    var debug = true
    var type = PAGE_ELEMENT

    private val elementBounds = RectF()

    val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.MAGENTA
        textSize = 12F
    }

    open fun onEvent(iMotionEventMarker: IMotionEventMarker?): Boolean {
        if (debug) {
            iMotionEventMarker?.apply {
                if (getBoundsRelativeToPage().contains(getX(), getY())) Log.i(
                    TAG,
                    "onEvent: elementType = $type"
                )
            }
        }
        return false
    }

    override fun draw(canvas: Canvas) {
        if (page == null) {
            Log.i(TAG, "draw: element will not be drawn -> no host page associated.")
            return
        }
        if (debug) {
            Log.i(TAG, "draw: Drawing Element")
            canvas.apply {
                drawRect(getBoundsRelativeToPage(), paint)
            }
        }
    }

    open fun getBoundsRelativeToPage(): RectF {
        val pageBounds = page!!.pageBounds
        val left = pageBounds.left + page.documentRenderView.toCurrentScale(layoutParams.x)
        val top = pageBounds.top + page.documentRenderView.toCurrentScale(layoutParams.y)
        val right = left + page.documentRenderView.toCurrentScale(layoutParams.width)
        val bottom = top + page.documentRenderView.toCurrentScale(layoutParams.height)
        elementBounds.left = left
        elementBounds.top = top
        elementBounds.right = right
        elementBounds.bottom = bottom
        return elementBounds
    }

    class LayoutParams(var width: Int = 0, var height: Int = 0) {
        var x = 0F
        var y = 0F
    }

    override fun toString(): String {
        return " type = $type , " +
                "Bounds = $elementBounds ," +
                " width = ${layoutParams.width} ," +
                " height = ${layoutParams.height} , " +
                "x = ${layoutParams.x} , y = ${layoutParams.y} "
    }

    companion object {
        private const val PAGE_ELEMENT = "pageElement"
        val TAG = PAGE_ELEMENT
    }
}