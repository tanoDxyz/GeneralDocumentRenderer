package com.tanodxyz.documentrenderer.elements

import android.graphics.RectF
import android.util.SparseArray
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.events.IEventHandler

/**
 * An [IElement] that can be drawn to [com.tanodxyz.documentrenderer.page.DocumentPage] and can also
 * receives [com.tanodxyz.documentrenderer.events.IMotionEventMarker] events.
 */
interface InteractiveElement : IElement, IEventHandler {
    /**
     * Content width is the total width of the element scale/zoom inclusive.
     * @param args is the set of properties passed.
     * for complete set of such properties check [com.tanodxyz.documentrenderer.page.DocumentPage]
     */
    fun getContentWidth(args: SparseArray<Any>?): Float

    /**
     * Content height is the total height of the element scale/zoom inclusive.
     * @param args is the set of properties passed.
     * for complete set of such properties check [com.tanodxyz.documentrenderer.page.DocumentPage]
     */
    fun getContentHeight(args: SparseArray<Any>?): Float

    /**
     * Scaled margins as it's name indicates is the scaled/zoom version of margins applied to an
     * element.
     * @param drawSnapShot indicates whether [com.tanodxyz.documentrenderer.page.PageSnapshotElement] is
     * being drawn by page.
     *
     * @return a rectangle containing margins in the following format. [left,top,right,bottom]
     */
    fun getScaledMargins(drawSnapShot: Boolean): RectF

    /**
     * Usually Content Bounds for each element is calculated internally by [InteractiveElement] in
     * a relative manner to the [com.tanodxyz.documentrenderer.page.DocumentPage] but if [InteractiveElement]s Content Bounds are
     * set via this method they will be used as it is and no further calculations will be performed.
     */
    fun setContentBounds(bounds: RectF)

    /**
     * @return the bounding box of the element. [left,top,right,bottom]
     */
    fun getContentBounds(drawSnapShot: Boolean): RectF

    /**
     * Reset the state of the element as if it is created just now.
     */
    fun reset()
    fun recycle()

    /**
     * When page measurement is done and by that we means [com.tanodxyz.documentrenderer.page.DocumentPage] bounds are calculated.
     * this method will be called.
     * rarely user may want to resize their [InteractiveElement].
     * #### it is called on non-UI thread.
     */
    fun pageMeasurementDone(documentRenderView: DocumentRenderView){}

    /**
     * When the [InteractiveElement] is scaled.
     * @param currentZoom current scale/zoom leve.
     */
    fun onScale(currentZoom: Float){}
}