package com.tanodxyz.documentrenderer.page

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.util.SparseArray
import android.view.MotionEvent
import com.tanodxyz.documentrenderer.*
import com.tanodxyz.documentrenderer.elements.PageElement
import com.tanodxyz.documentrenderer.elements.PageElementImpl
import com.tanodxyz.documentrenderer.elements.PageSnapShotElementImplImpl
import com.tanodxyz.documentrenderer.events.*
import com.tanodxyz.documentrenderer.pagesizecalculator.PageSizeCalculator
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.Serializable

open class DocumentPage(
    val uniqueId: Int = -1,
    val elements: MutableList<PageElement> = mutableListOf(),
    val originalSize: Size = Size(
        0,
        0
    ),
    val pageBounds: RectF = RectF(0F, 0F, 0F, 0F)
) : Serializable, IEventHandler {
    var argsToElements = SparseArray<Any>()
    var modifiedSize: Size = originalSize
    var snapScaleDownFactor = 1f
    var drawPageSnapShot = true
    lateinit var documentRenderView: DocumentRenderView
    protected var pageSnapShotElementImpl: PageSnapshotElementImpl = PageSnapShotElementImplImpl(this)
    open fun getWidth(): Float {
        return pageBounds.getWidth()
    }

    open fun getHeight(): Float {
        return pageBounds.getHeight()
    }

    open fun setPageSnapShotImpl(pageSnapshotElement: PageSnapshotElementImpl) {
        this.pageSnapShotElementImpl = pageSnapshotElement
    }

    open fun redraw() {
        if (this::documentRenderView.isInitialized) {
            documentRenderView.redraw()
        }
    }

    open fun draw(view: DocumentRenderView, canvas: Canvas, pageViewState: ObjectViewState) {
        this.documentRenderView = view
        if (drawPageSnapShot) {
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
        } else {
            dispatchDrawCallToIndividualElements(canvas, argsToElements)
        }
    }


    protected fun Canvas.dispatchDrawCallToIndividualElements() {
        argsToElements[RE_DRAW_WITH_NEW_PAGE_BOUNDS] = true
        dispatchDrawCallToIndividualElements(this, argsToElements)
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
        pageBounds.reset()
        elements.forEach { it.resetBounds() }
    }

    open fun onMeasurementDone(pageSizeCalculator: PageSizeCalculator) {
        elements.forEach { it.pageMeasurementDone(pageSizeCalculator) }
    }

    fun getSnapShot(scaleDown: Boolean, callback: (Bitmap?) -> Unit) {
        pageSnapShotElementImpl.getBitmap(scaleDown, callback)
    }

    fun saveSnapShot(filePath: String, scaleDown: Boolean, callback: (Boolean, String) -> Unit) {
        getSnapShot(scaleDown) { snap ->
            if (snap == null) {
                callback(false, "Failed to create snap!")
            } else {
                try {
                    val fos = FileOutputStream(filePath)
                    snap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    fos.close()
                    snap.recycleSafely()
                    callback(true, "Done!")
                } catch (e: FileNotFoundException) {
                    callback(false, "File not found! -> ${e.message}")
                } catch (e: IOException) {
                    callback(false, "Error accessing file -> ${e.message}")
                }
            }
        }
    }


    companion object {
        const val RE_DRAW_WITH_NEW_PAGE_BOUNDS = 0xcafe
        const val RE_DRAW_WITH_RELATIVE_TO_ORIGIN_SNAPSHOT_ = 0xbc
    }
}