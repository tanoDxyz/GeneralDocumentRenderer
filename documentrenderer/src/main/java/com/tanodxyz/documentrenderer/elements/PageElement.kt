package com.tanodxyz.documentrenderer.elements

import android.graphics.PointF
import android.graphics.RectF
import android.util.SparseArray
import com.tanodxyz.documentrenderer.Thread
import com.tanodxyz.documentrenderer.events.IMotionEventMarker
import com.tanodxyz.documentrenderer.pagesizecalculator.PageSizeCalculator

interface PageElement:IElement {
    fun onEvent(iMotionEventMarker: IMotionEventMarker?): Boolean
    fun SparseArray<Any>?.getLeftAndTop(): PointF
    fun getWidth(): Int
    fun getHeight(): Int
    fun contentWidthInCaseOfWrapContent():Int
    fun contentHeightInCaseOfWrapContent():Int

    fun SparseArray<Any>?.shouldDrawSnapShot(): Boolean

    fun SparseArray<Any>?.shouldReDrawWithNewPageBounds(): Boolean

    fun getScaledMargins(drawSnapShot: Boolean): RectF

    fun getBoundsRelativeToPage(
        drawSnapShot: Boolean = false
    ): RectF

    fun recycle()

    @Thread(description = "will be called on worker thread.")
    fun pageMeasurementDone(pageSizeCalculator: PageSizeCalculator)

    fun SparseArray<Any>?.textSizeRelativeToSnap(textSizePixels: Float): Float

    fun isEventOccurredWithInBounds(
        eventMarker: IMotionEventMarker?,
        checkBasedOnLastDrawCallType: Boolean = false
    ): Boolean

    fun resetBounds()

    interface OnClickListener {
        fun onClick(eventMarker: IMotionEventMarker?, pageElementImpl: PageElementImpl)
    }

    interface OnLongPressListener {
        fun onLongPress(eventMarker: IMotionEventMarker?, pageElementImpl: PageElementImpl)
    }
}