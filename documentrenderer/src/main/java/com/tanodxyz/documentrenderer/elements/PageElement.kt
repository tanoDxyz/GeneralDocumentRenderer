package com.tanodxyz.documentrenderer.elements

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
    protected val elementBounds = RectF()

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

    open fun getBoundsRelativeToPage(fromOrigin: Boolean = false): RectF {
        val pageBounds = page!!.pageBounds
        val left = pageBounds.left + page.documentRenderView.toCurrentScale(layoutParams.x)
        val top = pageBounds.top + page.documentRenderView.toCurrentScale(layoutParams.y)

        val right =
            if (layoutParams.widthMatchParent) {
                left + (layoutParams.getWidth()) // we are not scaling this because we are already using width from pageBounds. pageBounds are already scaled
            } else {
                left + page.documentRenderView.toCurrentScale(layoutParams.getWidth())
            }
        val bottom = if (layoutParams.heightMatchParent) {
            top + (layoutParams.getHeight())// we are not scaling this because we are already using height from pageBounds. pageBounds are already scaled
        } else {
            top + page.documentRenderView.toCurrentScale(layoutParams.getHeight())
        }
        elementBounds.left = left
        elementBounds.top = top
        elementBounds.right = right
        elementBounds.bottom = bottom
        return elementBounds
    }

    open class LayoutParams(
        private val page: DocumentPage?,
        var rawWidth: Int = 0,
        var rawHeight: Int = 0,
        var widthMatchParent: Boolean = false,
        var heightMatchParent: Boolean = false
    ) {
        var x = 0F
        var y = 0F
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
                "Bounds = $elementBounds ," +
                " width = ${layoutParams.getWidth()} ," +
                " height = ${layoutParams.getHeight()} , " +
                "x = ${layoutParams.x} , y = ${layoutParams.y} "
    }

    companion object {
        private const val PAGE_ELEMENT = "pageElement"
        val TAG = PAGE_ELEMENT
    }

    fun requestRedraw() {
        page?.documentRenderView?.redraw()
    }
}