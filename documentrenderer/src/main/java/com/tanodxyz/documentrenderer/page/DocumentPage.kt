package com.tanodxyz.documentrenderer.page

import android.graphics.Canvas
import android.graphics.RectF
import android.util.SparseArray
import android.view.MotionEvent
import com.tanodxyz.documentrenderer.*
import com.tanodxyz.documentrenderer.elements.PageElement
import com.tanodxyz.documentrenderer.elements.PageSnapShotElement
import com.tanodxyz.documentrenderer.events.*
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
) : Serializable, IEventHandler {
    internal var argsToElements = SparseArray<Any>()
    internal var modifiedSize: Size = originalSize
    protected var pageSnapShotElement: PageSnapShotElement = PageSnapShotElement(this)
    fun getWidth(): Float {
        return pageBounds.getWidth()
    }

    fun getHeight(): Float {
        return pageBounds.getHeight()
    }

    fun draw(canvas: Canvas, pageViewState: ObjectViewState) {
        if (pageViewState.isObjectPartiallyOrCompletelyVisible()) {
            if (documentRenderView.isScaling) {
                if (pageSnapShotElement.isEmpty()) {
                    pageSnapShotElement.preparePageSnapShot()
                    canvas.dispatchDrawCallToIndividualElements()
                } else {
                    pageSnapShotElement.draw(canvas)
                }
            } else {
                canvas.dispatchDrawCallToIndividualElements()
            }
        } else {
            pageSnapShotElement.recycle()
        }
    }


    private fun Canvas.dispatchDrawCallToIndividualElements() {
        argsToElements[RE_DRAW_WITH_NEW_PAGE_BOUNDS] = true
        elements.forEach { iElement -> iElement.draw(this, argsToElements) }
    }

    override fun onEvent(event: IMotionEventMarker?) {
        event?.apply {
            if (this is GenericMotionEvent && !this.hasNoMotionEvent()) {
                if (this.motionEvent?.action == MotionEvent.ACTION_DOWN) {
                    pageSnapShotElement.preparePageSnapShot()
                } else if (this.motionEvent?.action == MotionEvent.ACTION_UP) {
                    documentRenderView.redraw()
                }
            }
        }
        elements.forEach { iElement -> iElement.onEvent(event) }
    }

    fun resetPageBounds() {
        pageBounds.top = 0F
        pageBounds.left = 0F
        pageBounds.right = 0F
        pageBounds.bottom = 0F
    }

    companion object {
        const val RE_DRAW_WITH_NEW_PAGE_BOUNDS = 0xcafe
        const val RE_DRAW_WITH_RELATIVE_TO_ORIGIN__SNAPSHOT__ = 0xbc
    }
}