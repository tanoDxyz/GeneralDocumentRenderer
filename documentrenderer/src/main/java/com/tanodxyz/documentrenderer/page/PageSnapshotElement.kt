package com.tanodxyz.documentrenderer.page

import android.graphics.Bitmap
import com.tanodxyz.documentrenderer.elements.PageElement

/**
 * Each [DocumentPage] has an instance of this element.
 * this element is used when [DocumentPage.drawPageSnapShot] is true.
 * basically when user zoom/scale the page - instead of drawing each element on the canvas - a bitmap is
 * drawn and scaled as the user continues scale gesture.
 * when the scale gesture is finished or canceled than all the elements are drawn on canvas.
 *
 * **Drawn or rendered bitmap is the snapshot of all the element for the specific scale or zoom level.**
 *
 */
abstract class PageSnapshotElement(page: DocumentPage) : PageElement(page) {
    /**
     * This is an indication to start preparing/creating snapshot for specific scaleLevel.
     */
    abstract fun preparePageSnapshot(scaleLevel: Float)

    /**
     * @return if the snapshot does not exist or not created.
     */
    abstract fun isEmpty(): Boolean

    /**
     * @param callback will be invoked with the snapshot bitmap if present otherwise null.
     * @param scaleDown if [true] than a proper scaled version of bitmap is created.
     * it is sometime feasible because if page size is too large
     * Underlying implementation may not support it.
     */
    abstract fun getBitmap(scaleDown:Boolean = true,callback: (Bitmap?) -> Unit)

    /**
     * @param useScalingForSnapshot [true] means to create the page snapshot based on some
     * defined scaling algorithm.
     * [false] means to not use any scaling and just use the original page bounds for creating bitmaps.
     */
    abstract fun useScalingForSnapshot(useScalingForSnapshot: Boolean)
}