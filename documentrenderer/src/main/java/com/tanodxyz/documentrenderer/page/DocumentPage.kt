package com.tanodxyz.documentrenderer.page

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.Size
import com.tanodxyz.documentrenderer.elements.IElement
import com.tanodxyz.documentrenderer.events.IMotionEventMarker
import com.tanodxyz.documentrenderer.events.IEventHandler
import com.tanodxyz.documentrenderer.getHeight
import com.tanodxyz.documentrenderer.getWidth
import java.io.Serializable

data class DocumentPage(
    val uniqueId: Int = -1,
    val elements: MutableList<IElement> = mutableListOf(),
    val originalSize: Size = Size(
        0,
        0
    ),
    val pageBounds: RectF = RectF(0F, 0F, 0F, 0F),
    val documentPageRequestHandler: DocumentRenderView.DocumentPageRequestHandler
) : Serializable,IEventHandler {

    internal var modifiedSize: Size = originalSize

    fun getWidth(): Float {
        return pageBounds.getWidth()
    }

    fun getHeight(): Float {
        return pageBounds.getHeight()
    }

    fun draw(canvas: Canvas, pageViewState: PageViewState) {
    }

    override fun onEvent(event: IMotionEventMarker?) {
    }

    fun resetPageBounds() {
        pageBounds.top = 0F
        pageBounds.left = 0F
        pageBounds.right = 0F
        pageBounds.bottom = 0F
    }
}