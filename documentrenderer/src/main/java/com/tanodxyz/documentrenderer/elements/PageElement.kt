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
import kotlin.math.abs
import kotlin.math.roundToInt

open class PageElement(
    val page: DocumentPage? = null,
    var rawWidth: Int = DEFAULT_WIDTH,
    var rawHeight: Int = DEFAULT_HEIGHT,
    var x: Int = 0,
    var y: Int = 0,
    var widthMatchParent: Boolean = false,
    var heightMatchParent: Boolean = false
) : IElement {

    var debug = true
    open var type = PAGE_ELEMENT

    @VisibleForTesting
    val elementBoundsRelativeToPage = RectF()

    @VisibleForTesting
    val elementBoundsRelativeToOrigin = RectF()

    val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
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

    open fun getBoundsRelativeToPage(drawFromOrigin: Boolean = false): RectF {
        val pageBounds = page!!.pageBounds



        if (drawFromOrigin) {
            val scaleDownX =
                page.documentRenderView.toCurrentScale(x.div(DocumentRenderView.PAGE_SNAPSHOT_SCALE_DOWN_FACTOR))
            val scaleDownY =
                page.documentRenderView.toCurrentScale(y.div(DocumentRenderView.PAGE_SNAPSHOT_SCALE_DOWN_FACTOR))
            val scaleDownWidth =
                getWidth().div(DocumentRenderView.PAGE_SNAPSHOT_SCALE_DOWN_FACTOR)
            val scaledDownHeight =
                getHeight().div(DocumentRenderView.PAGE_SNAPSHOT_SCALE_DOWN_FACTOR)

            val left = scaleDownX
            val top = scaleDownY
            val right =
                if (widthMatchParent) {
                    (left + scaleDownWidth) - scaleDownX.times(2)
                } else {
                    (left + page.documentRenderView.toCurrentScale(
                        getWidth()
                            .div(DocumentRenderView.PAGE_SNAPSHOT_SCALE_DOWN_FACTOR)
                    ))
                }

            val bottom = if (heightMatchParent) {
                (top + scaledDownHeight) - scaleDownY.times(2)
            } else {
                (top + page.documentRenderView.toCurrentScale(
                    getHeight().div(DocumentRenderView.PAGE_SNAPSHOT_SCALE_DOWN_FACTOR)
                ))
            }
            elementBoundsRelativeToOrigin.left = left
            elementBoundsRelativeToOrigin.right = right
            elementBoundsRelativeToOrigin.top = top
            elementBoundsRelativeToOrigin.bottom = bottom
            return elementBoundsRelativeToOrigin
        } else {
            val scaledX = page.documentRenderView.toCurrentScale(x)
            val scaledY = page.documentRenderView.toCurrentScale(y)
            val left = pageBounds.left + scaledX
            val top = pageBounds.top + scaledY

            val right =
                if (widthMatchParent) {
                    (left + getWidth()) - scaledX.times(2)
                } else {
                    (left + page.documentRenderView.toCurrentScale(getWidth()))
                }
            val bottom = if (heightMatchParent) {
                (top + getHeight()) - scaledY.times(2)
            } else {
                (top + page.documentRenderView.toCurrentScale(getHeight()))
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

    open fun recycle() {}

    companion object {
        const val DEFAULT_WIDTH = 64
        const val DEFAULT_HEIGHT = 64
        private const val PAGE_ELEMENT = "pageElement"
        val TAG = PAGE_ELEMENT
    }
}