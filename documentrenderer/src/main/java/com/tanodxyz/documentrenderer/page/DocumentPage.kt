package com.tanodxyz.documentrenderer.page

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.tanodxyz.documentrenderer.*
import com.tanodxyz.documentrenderer.elements.IElement
import com.tanodxyz.documentrenderer.elements.PageElement
import com.tanodxyz.documentrenderer.events.IMotionEventMarker
import com.tanodxyz.documentrenderer.events.IEventHandler
import java.io.Serializable

data class DocumentPage(
    val uniqueId: Int = -1,
    val elements: MutableList<PageElement> = mutableListOf(),
    val originalSize: Size = Size(
        0,
        0
    ),
    val pageBounds: RectF = RectF(0F, 0F, 0F, 0F),
    val documentRenderView: DocumentRenderView
) : Serializable,IEventHandler {

    internal var modifiedSize: Size = originalSize

    fun getWidth(): Float {
        return pageBounds.getWidth()
    }

    fun getHeight(): Float {
        return pageBounds.getHeight()
    }

    fun draw(canvas: Canvas, pageViewState: PageViewState) {
        if(pageViewState.isPagePartiallyOrCompletelyVisible()) {
            elements.forEach { iElement -> iElement.draw(canvas) }
        }
    }

    override fun onEvent(event: IMotionEventMarker?) {
        elements.forEach { iElement-> iElement.onEvent(event) }
    }

    fun resetPageBounds() {
        pageBounds.top = 0F
        pageBounds.left = 0F
        pageBounds.right = 0F
        pageBounds.bottom = 0F
    }
}