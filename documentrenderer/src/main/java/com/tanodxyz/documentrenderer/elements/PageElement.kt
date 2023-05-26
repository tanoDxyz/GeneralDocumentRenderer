package com.tanodxyz.documentrenderer.elements

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import android.util.SparseArray
import androidx.annotation.VisibleForTesting
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.DocumentRenderView.Companion.PAGE_SNAPSHOT_SCALE_DOWN_FACTOR
import com.tanodxyz.documentrenderer.events.IMotionEventMarker
import com.tanodxyz.documentrenderer.getHeight
import com.tanodxyz.documentrenderer.getWidth
import com.tanodxyz.documentrenderer.page.DocumentPage
import kotlin.math.roundToInt

open class PageElement(
    var page: DocumentPage,
    val layoutParams: LayoutParams = LayoutParams()
) : IElement {
    constructor(
        page: DocumentPage,
        width: Int,
        height: Int,
        leftMargin: Int,
        topMargin: Int,
        rightMargin: Int,
        bottomMargin: Int,
        startEndSymmetric: Boolean,
        topBottomSymmetric: Boolean
    ) : this(
        page = page,
        LayoutParams(width, height, leftMargin, topMargin, rightMargin, bottomMargin,startEndSymmetric,topBottomSymmetric)
    )

    constructor(page: DocumentPage, width: Int, height: Int) : this(
        page,
        LayoutParams(desiredWidth = width, desiredHeight = height)
    )

    open var type = PAGE_ELEMENT
    var debug = false

    @VisibleForTesting
    val elementBoundsRelativeToPage = RectF()

    @VisibleForTesting
    val elementBoundsRelativeToOrigin = RectF()

    open fun onEvent(iMotionEventMarker: IMotionEventMarker?): Boolean {
        if (debug) {
            iMotionEventMarker?.apply {
                if (getBoundsRelativeToPage().contains(getX(), getY())) Log.i(
                    PageElement.TAG,
                    "onEvent: Touched elementType = $type , eventType = $this"
                )
            }
            Log.i(
                PageElement.TAG,
                "onEvent: elementType = $type , eventType = $iMotionEventMarker"
            )
        }

        return false
    }

    open fun SparseArray<Any>?.getLeftAndTop(): PointF {
        val drawFromOrigin = this.shouldDrawFromOrigin()
        val boundsRelativeToPage = getBoundsRelativeToPage(drawFromOrigin)
        return PointF(boundsRelativeToPage.left, boundsRelativeToPage.top)
    }

    fun getWidth(): Int {
        return if (layoutParams.IsWidthMatchParent() || layoutParams.startEndSymmetric) {
            page.pageBounds.getWidth().roundToInt()
        } else {
            page.documentRenderView.toCurrentScale(layoutParams.desiredWidth).roundToInt()
        }
    }


    fun getHeight(): Int {
        return if (layoutParams.isHeightMatchParent() || layoutParams.topBottomSymmetric) {
            page.pageBounds.getHeight().roundToInt()
        } else {
            page.documentRenderView.toCurrentScale(layoutParams.desiredHeight).roundToInt()
        }
    }

    fun SparseArray<Any>?.shouldDrawFromOrigin(): Boolean {
        return this != null && this[DocumentPage.RE_DRAW_WITH_RELATIVE_TO_ORIGIN__SNAPSHOT__] == true
    }

    fun SparseArray<Any>?.shouldReDrawWithNewPageBounds(): Boolean {
        return this != null && this[DocumentPage.RE_DRAW_WITH_NEW_PAGE_BOUNDS] == true
    }


    open fun getBoundsRelativeToPage(drawFromOrigin: Boolean = false): RectF {
        var boundsRelative: RectF? = null
        layoutParams.apply {
            var left = 0F
            var top = 0f
            var right = 0f
            var bottom = 0f
            val pageBounds = page.pageBounds

            if (drawFromOrigin) {
                val scaledDownHeight =
                    getHeight().div(PAGE_SNAPSHOT_SCALE_DOWN_FACTOR)
                val scaleDownWidth =
                    getWidth().div(PAGE_SNAPSHOT_SCALE_DOWN_FACTOR)
                val scaledDownLeftMargin =
                    page.documentRenderView.toCurrentScale(leftMargin.div(PAGE_SNAPSHOT_SCALE_DOWN_FACTOR))
                val scaledDownTopMargin =
                    page.documentRenderView.toCurrentScale(topMargin.div(PAGE_SNAPSHOT_SCALE_DOWN_FACTOR))
                val scaledDownRightMargin =
                    page.documentRenderView.toCurrentScale(rightMargin.div(PAGE_SNAPSHOT_SCALE_DOWN_FACTOR))
                val scaledDownBottomMargin =
                    page.documentRenderView.toCurrentScale(bottomMargin.div(PAGE_SNAPSHOT_SCALE_DOWN_FACTOR))

                left = scaledDownLeftMargin
                top = scaledDownTopMargin
                right = if (startEndSymmetric) {
                    (left + scaleDownWidth) - scaledDownLeftMargin.times(2)
                } else {
                    (left + scaleDownWidth) - (scaledDownRightMargin + scaledDownLeftMargin)
                }
                bottom = if (topBottomSymmetric) {
                    (top + scaledDownHeight) - scaledDownTopMargin.times(2)
                } else {
                    (top + scaledDownHeight) - (scaledDownBottomMargin + scaledDownTopMargin)
                }
                elementBoundsRelativeToOrigin.apply {
                    this.left = left
                    this.top = top
                    this.right = right
                    this.bottom = bottom
                }
                boundsRelative = elementBoundsRelativeToOrigin
            } else {
                val scaledMarginLeft = page.documentRenderView.toCurrentScale(leftMargin)
                val scaledMarginTop = page.documentRenderView.toCurrentScale(topMargin)
                val scaledMarginRight = page.documentRenderView.toCurrentScale(rightMargin)
                val scaledMarginBottom = page.documentRenderView.toCurrentScale(bottomMargin)
                left = pageBounds.left + scaledMarginLeft
                top = pageBounds.top + scaledMarginTop

                right = if (startEndSymmetric) {
                    (left + getWidth()) - scaledMarginLeft.times(2)
                } else {
                    (left + getWidth()) - (scaledMarginRight + scaledMarginLeft)
                }
                bottom = if (topBottomSymmetric) {
                    (top + getHeight()) - scaledMarginTop.times(2)
                } else {
                    (top + getHeight()) - (scaledMarginBottom + scaledMarginTop)
                }
                elementBoundsRelativeToPage.apply {
                    this.left = left
                    this.top = top
                    this.right = right
                    this.bottom = bottom
                }
                boundsRelative = elementBoundsRelativeToPage
            }
        }
        return boundsRelative!!
    }

    open fun recycle() {}
    data class LayoutParams(
        var desiredWidth: Int = DEFAULT_WIDTH,
        var desiredHeight: Int = DEFAULT_HEIGHT,
        var leftMargin: Int = 0,
        var topMargin: Int = 0,
        var rightMargin: Int = 0,
        var bottomMargin: Int = 0,
        var startEndSymmetric: Boolean = false,
        var topBottomSymmetric: Boolean = false,
    ) {
        fun IsWidthMatchParent(): Boolean {
            return desiredWidth == MATCH_PARENT
        }

        fun isHeightMatchParent(): Boolean {
            return desiredHeight == MATCH_PARENT
        }
    }


    companion object {
        const val DEFAULT_WIDTH = 64
        const val DEFAULT_HEIGHT = 64
        const val MATCH_PARENT = -1
        private const val PAGE_ELEMENT = "pageElement"
        val TAG = PAGE_ELEMENT
    }
}
