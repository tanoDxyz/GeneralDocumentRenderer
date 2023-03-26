package com.tanodxyz.documentrenderer.elements

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.Log
import android.util.SparseArray
import androidx.annotation.VisibleForTesting
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.events.*
import com.tanodxyz.documentrenderer.getHeight
import com.tanodxyz.documentrenderer.getWidth
import com.tanodxyz.documentrenderer.page.DocumentPage
import com.tanodxyz.documentrenderer.page.ObjectViewState
import kotlin.math.roundToInt

open class PageElement(
    protected val page: DocumentPage? = null,
    /*protected*/
    val layoutParams: LayoutParams = LayoutParams(page),

    ) : IElement {

    var debug = true
    protected open var type = PAGE_ELEMENT

    @VisibleForTesting
    protected val elementBoundsRelativeToPage = RectF()

    @VisibleForTesting
    protected val elementBoundsRelativeToOrigin = RectF()
    val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.MAGENTA
        textSize = 12F
    }

    open fun onEvent(iMotionEventMarker: IMotionEventMarker?): Boolean {
        if (debug) {
            iMotionEventMarker?.apply {
                if (getBoundsRelativeToPage().contains(getX(), getY())) Log.i(
                    TAG,
                    "onEvent: Touched elementType = $type , eventType = $this"
                )
            }
            Log.i(
                TAG,
                "onEvent: elementType = $type , eventType = $iMotionEventMarker"
            )
        }

        return false
    }

    fun SparseArray<Any>?.shouldDrawFromOrigin(): Boolean {
        return this != null && this[DocumentPage.RE_DRAW_WITH_RELATIVE_TO_ORIGIN__SNAPSHOT__] == true
    }

    fun SparseArray<Any>?.shouldReDrawWithNewPageBounds(): Boolean {
        return this != null && this[DocumentPage.RE_DRAW_WITH_NEW_PAGE_BOUNDS] == true
    }

    open fun getElementViewState(): ObjectViewState {
        val boundsRelativeToPage = getBoundsRelativeToPage()
        boundsRelativeToPage.apply {
            val viewWidth = page!!.documentRenderView.width
            val viewHeight = page.documentRenderView.height

            val leftVisible = (left >= 0 && left <= viewWidth)
            val topVisible = (top >= 0 && top < viewHeight)
            val rightVisible = (right > left && right <= viewWidth)
            val bottomVisible = (bottom > top && bottom <= viewHeight)

            return if (leftVisible && rightVisible && bottomVisible && topVisible) {
                ObjectViewState.VISIBLE
            } else if (leftVisible || rightVisible || bottomVisible || topVisible) {
                ObjectViewState.PARTIALLY_VISIBLE
            } else {
                ObjectViewState.INVISIBLE
            }
        }
    }

    open fun getBoundsRelativeToPage(drawFromOrigin: Boolean = false): RectF {
        val pageBounds = page!!.pageBounds
        val scaledX = page.documentRenderView.toCurrentScale(layoutParams.xPadding)
        val scaledY = page.documentRenderView.toCurrentScale(layoutParams.yPadding)


        if (drawFromOrigin) {
            val scaleDownX =
                page.documentRenderView.toCurrentScale(layoutParams.xPadding.div(DocumentRenderView.PAGE_SNAPSHOT_SCALE_DOWN_FACTOR))
            val scaleDownY =
                page.documentRenderView.toCurrentScale(layoutParams.yPadding.div(DocumentRenderView.PAGE_SNAPSHOT_SCALE_DOWN_FACTOR))
            val scaleDownWidth =
                layoutParams.getWidth().div(DocumentRenderView.PAGE_SNAPSHOT_SCALE_DOWN_FACTOR)
            val scaledDownHeight =
                layoutParams.getHeight().div(DocumentRenderView.PAGE_SNAPSHOT_SCALE_DOWN_FACTOR)

            val left = scaleDownX
            val top = scaleDownY
            val right =
                if (layoutParams.widthMatchParent) {
                    (left + scaleDownWidth) - scaleDownX.times(2)// we are not scaling this because we are already using width from pageBounds. pageBounds are already scaled
                } else {
                    (left + page.documentRenderView.toCurrentScale(
                        layoutParams.getWidth()
                            .div(DocumentRenderView.PAGE_SNAPSHOT_SCALE_DOWN_FACTOR)
                    )) - scaleDownX.times(2)
                }

            val bottom = if (layoutParams.heightMatchParent) {
                (top + scaledDownHeight) - scaleDownY.times(2)// we are not scaling this because we are already using height from pageBounds. pageBounds are already scaled
            } else {
                (top + page.documentRenderView.toCurrentScale(
                    layoutParams.getHeight().div(DocumentRenderView.PAGE_SNAPSHOT_SCALE_DOWN_FACTOR)
                )) - scaleDownY.times(2)
            }
            elementBoundsRelativeToOrigin.left = left
            elementBoundsRelativeToOrigin.right = right
            elementBoundsRelativeToOrigin.top = top
            elementBoundsRelativeToOrigin.bottom = bottom
            return elementBoundsRelativeToOrigin
        } else {
            val left = pageBounds.left + scaledX
            val top = pageBounds.top + scaledY

            val right =
                if (layoutParams.widthMatchParent) {
                    (left + layoutParams.getWidth()) - scaledX.times(2)// we are not scaling this because we are already using width from pageBounds. pageBounds are already scaled
                } else {
                    (left + page.documentRenderView.toCurrentScale(layoutParams.getWidth())) - scaledX.times(2)
                }
            val bottom = if (layoutParams.heightMatchParent) {
                (top + layoutParams.getHeight()) - scaledY.times(2)// we are not scaling this because we are already using height from pageBounds. pageBounds are already scaled
            } else {
                (top + page.documentRenderView.toCurrentScale(layoutParams.getHeight())) - scaledY.times(2)
            }
            elementBoundsRelativeToPage.left = left
            elementBoundsRelativeToPage.top = top
            elementBoundsRelativeToPage.right = right
            elementBoundsRelativeToPage.bottom = bottom
            return elementBoundsRelativeToPage
        }
    }

    open fun SparseArray<Any>?.getLeftAndTop(): PointF {
        val drawFromOrigin = this.shouldDrawFromOrigin()
        val boundsRelativeToPage = getBoundsRelativeToPage(drawFromOrigin)
        return PointF(boundsRelativeToPage.left, boundsRelativeToPage.top)
    }

    open class LayoutParams(
        private val page: DocumentPage?,
        var rawWidth: Int = 0,
        var rawHeight: Int = 0,
        var widthMatchParent: Boolean = false,
        var heightMatchParent: Boolean = false
    ) {
        var xPadding = 0F
        var yPadding = 0F
        fun getWidth(): Int {
            return if (widthMatchParent) {
                page!!.pageBounds.getWidth().roundToInt()
            } else {
                rawWidth
            }
        }


        fun getHeight(): Int {
            return if (heightMatchParent) {
                page!!.pageBounds.getHeight().roundToInt()
            } else {
                rawHeight
            }
        }
    }

    open fun recycle() {}

    override fun toString(): String {
        return " type = $type , " +
                "Bounds = $elementBoundsRelativeToPage ," +
                " width = ${layoutParams.getWidth()} ," +
                " height = ${layoutParams.getHeight()} , " +
                "xPadding = ${layoutParams.xPadding} , yPadding = ${layoutParams.yPadding} "
    }

    companion object {
        private const val PAGE_ELEMENT = "pageElement"
        val TAG = PAGE_ELEMENT
    }

    fun requestRedraw() {
        page?.documentRenderView?.redraw()
    }
}