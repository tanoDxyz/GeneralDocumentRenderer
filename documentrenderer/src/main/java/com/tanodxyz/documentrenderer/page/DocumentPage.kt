package com.tanodxyz.documentrenderer.page

import android.graphics.Canvas
import android.graphics.RectF
import android.util.SparseArray
import android.view.MotionEvent
import com.tanodxyz.documentrenderer.*
import com.tanodxyz.documentrenderer.elements.PageElement
import com.tanodxyz.documentrenderer.elements.PageSnapShotElementImpl
import com.tanodxyz.documentrenderer.events.*
import java.io.Serializable

open class DocumentPage(
    val uniqueId: Int = -1,
    val elements: MutableList<PageElement> = mutableListOf(),
    val originalSize: Size = Size(
        0,
        0
    ),
    val pageBounds: RectF = RectF(0F, 0F, 0F, 0F),
    val documentRenderView: DocumentRenderView
) : Serializable, IEventHandler {
    var argsToElements = SparseArray<Any>()
    var modifiedSize: Size = originalSize
    protected var pageSnapShotElementImpl: PageSnapshotElement = PageSnapShotElementImpl(this)
    open fun getWidth(): Float {
        return pageBounds.getWidth()
    }

    open fun getHeight(): Float {
        return pageBounds.getHeight()
    }

    fun setPageSnapShotImpl(pageSnapshotElement: PageSnapshotElement) {
        this.pageSnapShotElementImpl = pageSnapshotElement
    }

    open fun draw(canvas: Canvas, pageViewState: ObjectViewState) {
        if (pageViewState.isObjectPartiallyOrCompletelyVisible()) {
            if (documentRenderView.isScaling) {
                if (pageSnapShotElementImpl.isEmpty()) {
                    pageSnapShotElementImpl.preparePageSnapshot(documentRenderView.getCurrentZoom())
                    canvas.dispatchDrawCallToIndividualElements()
                } else {
                    pageSnapShotElementImpl.draw(canvas)
                }
            } else {
                canvas.dispatchDrawCallToIndividualElements()
            }
        } else {
            pageSnapShotElementImpl.recycle()
        }
    }


    protected fun Canvas.dispatchDrawCallToIndividualElements() {
        argsToElements[RE_DRAW_WITH_NEW_PAGE_BOUNDS] = true
        dispatchDrawCallToIndividualElements(this,argsToElements)
    }

    open fun dispatchDrawCallToIndividualElements(canvas: Canvas, args: SparseArray<Any>) {
        elements.forEach { iElement -> iElement.draw(canvas, args) }
    }

    override fun onEvent(event: IMotionEventMarker?) {
        event?.apply {
            if (this is GenericMotionEvent && !this.hasNoMotionEvent()) {
                if (this.motionEvent?.action == MotionEvent.ACTION_DOWN) {
                    pageSnapShotElementImpl.preparePageSnapshot(documentRenderView.getCurrentZoom())
                } else if (this.motionEvent?.action == MotionEvent.ACTION_UP) {
                    documentRenderView.redraw()
                }
            }
        }
        elements.forEach { iElement -> iElement.onEvent(event) }
    }

    open fun resetPageBounds() {
        pageBounds.top = 0F
        pageBounds.left = 0F
        pageBounds.right = 0F
        pageBounds.bottom = 0F
    }

    companion object {
        const val RE_DRAW_WITH_NEW_PAGE_BOUNDS = 0xcafe
        const val RE_DRAW_WITH_RELATIVE_TO_ORIGIN_SNAPSHOT_ = 0xbc
    }
}