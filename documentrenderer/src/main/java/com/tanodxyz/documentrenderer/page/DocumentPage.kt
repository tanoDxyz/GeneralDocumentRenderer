package com.tanodxyz.documentrenderer.page

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.util.SparseArray
import android.view.MotionEvent
import com.tanodxyz.documentrenderer.*
import com.tanodxyz.documentrenderer.elements.InteractiveElement
import com.tanodxyz.documentrenderer.elements.PageSnapShotElementImpl
import com.tanodxyz.documentrenderer.events.*
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.Serializable

/**
 * This class encapsulates an individual page properties and behaviours in [com.tanodxyz.documentrenderer.document]
 * >
 * ### Each [DocumentPage] has a unique id which starts from 0.
 * ### elements which are going to be rendered by [DocumentRenderView]
 * ### Bounds on the screen.
 * ### Page size.
 * ### PageSnapShot which is going to be rendered when page is scaled using scale/zoom gesture.
 * when [DocumentPage.drawPageSnapShot] is true and [MotionEvent.ACTION_DOWN] is triggered when [DocumentPage] is touched.
 * [PageSnapshotElement] is created or updated which later will be used upon scaling/scrolling.
 */
open class DocumentPage(
    val uniqueId: Int = -1,
    val elements: MutableList<InteractiveElement> = mutableListOf(),
    val originalSize: Size = Size(
        0,
        0
    ),
    val pageBounds: RectF = RectF(0F, 0F, 0F, 0F)
) : Serializable, IEventHandler {
    var argsToElements = SparseArray<Any>()
    var modifiedSize: Size = originalSize
    var snapScaleDownFactor = 1f
    var drawPageSnapShot = false
    lateinit var documentRenderView: DocumentRenderView
    protected var pageSnapShotElement: PageSnapshotElement = PageSnapShotElementImpl(this)
    open fun getWidth(): Float {
        return pageBounds.getWidth()
    }

    /**
     * When [PageSnapshotElement]
     */
    fun setUseScalingFactorForSnapshot(useScalingForSnapshot:Boolean) {
        pageSnapShotElement.useScalingForSnapshot(useScalingForSnapshot)
    }

    open fun getHeight(): Float {
        return pageBounds.getHeight()
    }

    open fun setPageSnapShotImpl(pageSnapshotElement: PageSnapshotElement) {
        this.pageSnapShotElement = pageSnapshotElement
    }

    open fun redraw() {
        if (this::documentRenderView.isInitialized) {
            documentRenderView.redraw()
        }
    }

    open fun pageViewState():ObjectViewState {
        return documentRenderView.getPageViewState(pageBounds)
    }

    open fun draw(view: DocumentRenderView, canvas: Canvas, pageViewState: ObjectViewState) {
        this.documentRenderView = view
        if (drawPageSnapShot) {
            if (pageViewState.isObjectPartiallyOrCompletelyVisible()) {
                if (documentRenderView.isScaling) {
                    if (pageSnapShotElement.isEmpty()) {
                        pageSnapShotElement.preparePageSnapshot(documentRenderView.getCurrentZoom())
                        canvas.dispatchDrawCallToIndividualElements()
                    } else {
                        pageSnapShotElement.draw(canvas)
                        onScale()
                    }
                } else {
                    canvas.dispatchDrawCallToIndividualElements()
                    if(argsToElements[RE_DRAW_WITH_RELATIVE_TO_ORIGIN_SNAPSHOT_] == true) {
                        pageSnapShotElement.draw(canvas)
                        argsToElements[RE_DRAW_WITH_RELATIVE_TO_ORIGIN_SNAPSHOT_] = false
                    }
                }
            } else {
                pageSnapShotElement.recycle()
            }
        } else {
            dispatchDrawCallToIndividualElements(canvas, argsToElements)
        }
    }

    open fun onScale() {
        elements.forEach { it.onScale(documentRenderView.getCurrentZoom()) }
    }
    open fun updatePageSnapShotForLatestScaleLevel() {
        pageSnapShotElement.preparePageSnapshot(documentRenderView.getCurrentZoom())
    }

    protected fun Canvas.dispatchDrawCallToIndividualElements() {
        argsToElements[RE_DRAW_WITH_NEW_PAGE_BOUNDS] = true
        dispatchDrawCallToIndividualElements(this, argsToElements)
    }

    open fun dispatchDrawCallToIndividualElements(canvas: Canvas, args: SparseArray<Any>) {
        elements.forEach { iElement ->
            iElement.draw(canvas, args)
            documentRenderView.renderListener?.onPageElementRendered(iElement)
        }
    }

    override fun onEvent(event: IMotionEventMarker?): Boolean {
        event?.apply {
            if (this is GenericMotionEvent && !this.hasNoMotionEvent()) {
                if (this.motionEvent?.action == MotionEvent.ACTION_DOWN) {
                    if(drawPageSnapShot) {
                        pageSnapShotElement.preparePageSnapshot(documentRenderView.getCurrentZoom())
                    }
                } else if (this.motionEvent?.action == MotionEvent.ACTION_UP) {
                    documentRenderView.redraw()
                }
            }
        }
        elements.forEach { iElement -> iElement.onEvent(event) }

        return true
    }

    open fun resetPageBounds() {
        pageBounds.reset()
        elements.forEach { it.reset() }
    }

    open fun onMeasurementDone(documentRenderView: DocumentRenderView) {
        this.documentRenderView = documentRenderView
        elements.forEach { it.pageMeasurementDone(this.documentRenderView) }
    }

    fun getSnapShot(scaleDown: Boolean, callback: (Bitmap?) -> Unit) {
        pageSnapShotElement.getBitmap(scaleDown, callback)
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

    open fun recycle() {
        elements.forEach { it.recycle() }
    }

    fun clearSnapShot() {
        pageSnapShotElement.recycle()
    }


    companion object {
        const val RE_DRAW_WITH_NEW_PAGE_BOUNDS = 0xcafe
        const val RE_DRAW_WITH_RELATIVE_TO_ORIGIN_SNAPSHOT_ = 0xbc
        const val FORCE_CALCULATE = 0xbabe
    }
}