package com.tanodxyz.documentrenderer.elements

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.Log
import android.util.SparseArray
import androidx.annotation.VisibleForTesting
import com.tanodxyz.documentrenderer.Size
import com.tanodxyz.documentrenderer.Thread
import com.tanodxyz.documentrenderer.events.IMotionEventMarker
import com.tanodxyz.documentrenderer.events.LongPressEvent
import com.tanodxyz.documentrenderer.events.SingleTapConfirmedEvent
import com.tanodxyz.documentrenderer.getHeight
import com.tanodxyz.documentrenderer.getWidth
import com.tanodxyz.documentrenderer.page.DocumentPage
import com.tanodxyz.documentrenderer.pagesizecalculator.PageSizeCalculator
import com.tanodxyz.documentrenderer.reset
import kotlin.math.roundToInt

open class PageElementImpl(
    var page: DocumentPage,
    val layoutParams: LayoutParams = LayoutParams()
) : PageElement {
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
        LayoutParams(
            width,
            height,
            leftMargin,
            topMargin,
            rightMargin,
            bottomMargin,
            startEndSymmetric,
            topBottomSymmetric
        )
    )

    constructor(page: DocumentPage, width: Int, height: Int) : this(
        page,
        LayoutParams(desiredWidth = width, desiredHeight = height)
    )

    private var mostRecentArgs: SparseArray<Any>? = null
    open var type = PAGE_ELEMENT
    var debug = true

    @VisibleForTesting
    val elementBoundsRelativeToPage = RectF()

    @VisibleForTesting
    val elementBoundsRelativeToOrigin = RectF()

    var clickListener: PageElement.OnClickListener? = null

    var longPressListener: PageElement.OnLongPressListener? = null

    val debugPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 8F
    }

    override fun onEvent(iMotionEventMarker: IMotionEventMarker?): Boolean {
        if (debug) {
            iMotionEventMarker?.apply {
                if (getBoundsRelativeToPage().contains(getX(), getY())) Log.i(
                    PageElementImpl.TAG,
                    "onEvent: Touched elementType = $type , eventType = $this"
                )
            }
            Log.i(
                PageElementImpl.TAG,
                "onEvent: elementType = $type , eventType = $iMotionEventMarker"
            )
        }
        // simple click listener
        clickListener?.apply {
            if (isEventOccurredWithInBounds(
                    iMotionEventMarker,
                    true
                ) && iMotionEventMarker is SingleTapConfirmedEvent
            ) {
                onClick(iMotionEventMarker, this@PageElementImpl)
            }
        }

        longPressListener?.apply {
            if (isEventOccurredWithInBounds(
                    iMotionEventMarker,
                    true
                ) && iMotionEventMarker is LongPressEvent
            ) {
                longPressListener?.onLongPress(iMotionEventMarker, this@PageElementImpl)
            }
        }

        return false
    }

    open fun calculateElementSizeRelativeToPageSize(pageSizeCalculator: PageSizeCalculator?) {
        if (pageSizeCalculator != null) {
            layoutParams.apply {
                val relativeSize =
                    page.documentRenderView.pageSizeCalculator!!.calculateElementSizeRelative(
                        Size(
                            desiredWidth,
                            desiredHeight
                        )
                    )
                desiredWidth = relativeSize.width
                desiredHeight = relativeSize.height
            }
        }
    }

    override fun SparseArray<Any>?.getLeftAndTop(): PointF {
        val drawSnapShot = this.shouldDrawSnapShot()
        val boundsRelativeToPage = getBoundsRelativeToPage(drawSnapShot)
        return PointF(boundsRelativeToPage.left, boundsRelativeToPage.top)
    }

    override fun getWidth(): Int {
        return if (layoutParams.isWidthMatchParent() || layoutParams.startEndSymmetric) {
            page.pageBounds.getWidth().roundToInt()
        } else if (layoutParams.isWidthWrapContent()) {
            contentWidthInCaseOfWrapContent()
        } else {
            page.documentRenderView.toCurrentScale(layoutParams.desiredWidth).roundToInt()
        }
    }

    override fun contentWidthInCaseOfWrapContent():Int {
        return 1
    }

    override fun contentHeightInCaseOfWrapContent():Int {
        return 1
    }

    override fun getHeight(): Int {
        return if (layoutParams.isHeightMatchParent() || layoutParams.topBottomSymmetric) {
            page.pageBounds.getHeight().roundToInt()
        } else if (layoutParams.isHeightWrapContent()) {
            contentHeightInCaseOfWrapContent()
        } else {
            page.documentRenderView.toCurrentScale(layoutParams.desiredHeight).roundToInt()
        }
    }

    val actualWidth: Float get() = getBoundsRelativeToPage(false).getWidth()
    val actualHeight: Float get() = getBoundsRelativeToPage(false).getHeight()

    override fun SparseArray<Any>?.shouldDrawSnapShot(): Boolean {
        return this != null && this[DocumentPage.RE_DRAW_WITH_RELATIVE_TO_ORIGIN_SNAPSHOT_] == true
    }

    override fun SparseArray<Any>?.shouldReDrawWithNewPageBounds(): Boolean {
        return this != null && this[DocumentPage.RE_DRAW_WITH_NEW_PAGE_BOUNDS] == true
    }


    override fun getScaledMargins(drawSnapShot: Boolean):RectF {
        val margins = RectF()
        val lm = layoutParams.leftMargin
        val tm = layoutParams.topMargin
        val rm = layoutParams.rightMargin
        val bm = layoutParams.bottomMargin
        var slm = 0F
        var stm = 0F
        var srm = 0F
        var sbm = 0F
        if(drawSnapShot) {
            slm =
                page.documentRenderView.toCurrentScale(lm.div(page.snapScaleDownFactor))
            stm =
                page.documentRenderView.toCurrentScale(tm.div(page.snapScaleDownFactor))
            srm =
                page.documentRenderView.toCurrentScale(rm.div(page.snapScaleDownFactor))
            sbm =
                page.documentRenderView.toCurrentScale(bm.div(page.snapScaleDownFactor))
            margins.apply {

            }
        } else {
            slm = page.documentRenderView.toCurrentScale(lm)
            stm = page.documentRenderView.toCurrentScale(tm)
            srm = page.documentRenderView.toCurrentScale(rm)
            sbm = page.documentRenderView.toCurrentScale(bm)
        }
        margins.apply {
            left = slm
            top = stm
            right = srm
            bottom = sbm
        }
        return margins
    }
    override fun getBoundsRelativeToPage(
        drawSnapShot: Boolean
    ): RectF {
        var boundsRelative: RectF? = null
        layoutParams.apply {
            var left = 0F
            var top = 0f
            var right = 0f
            var bottom = 0f
            val pageBounds = page.pageBounds

            if (drawSnapShot) {
                val scaledDownHeight =
                    getHeight().div(page.snapScaleDownFactor)
                val scaleDownWidth =
                    getWidth().div(page.snapScaleDownFactor)
                val scaledDownLeftMargin =
                    page.documentRenderView.toCurrentScale(leftMargin.div(page.snapScaleDownFactor))
                val scaledDownTopMargin =
                    page.documentRenderView.toCurrentScale(topMargin.div(page.snapScaleDownFactor))
                val scaledDownRightMargin =
                    page.documentRenderView.toCurrentScale(rightMargin.div(page.snapScaleDownFactor))
                val scaledDownBottomMargin =
                    page.documentRenderView.toCurrentScale(bottomMargin.div(page.snapScaleDownFactor))

                left = scaledDownLeftMargin
                top = scaledDownTopMargin
                right = if (startEndSymmetric) {
                    (left + scaleDownWidth) - scaledDownLeftMargin.times(2)
                } else {
                    if (isWidthMatchParent() || isWidthWrapContent()) {
                        (left + scaleDownWidth) - (scaledDownRightMargin + scaledDownLeftMargin)
                    } else {
                        (left + scaleDownWidth) - (scaledDownRightMargin)
                    }
                }
                bottom = if (topBottomSymmetric) {
                    (top + scaledDownHeight) - scaledDownTopMargin.times(2)
                } else {
                    (top + scaledDownHeight) - (scaledDownBottomMargin)
                    if (isHeightMatchParent() || isHeightWrapContent()) {
                        (top + scaledDownHeight) - (scaledDownBottomMargin + scaledDownTopMargin)
                    } else {
                        (top + scaledDownHeight) - (scaledDownBottomMargin)
                    }
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
                    if (isWidthMatchParent() || isWidthWrapContent()) {
                        (left + getWidth()) - (scaledMarginRight + scaledMarginLeft)
                    } else {
                        (left + getWidth()) - (scaledMarginRight)
                    }
                }
                bottom = if (topBottomSymmetric) {
                    (top + getHeight()) - scaledMarginTop.times(2)
                } else {
                    if (isHeightMatchParent() || isHeightWrapContent()) {
                        (top + getHeight()) - (scaledMarginBottom + scaledMarginTop)
                    } else {
                        (top + getHeight()) - (scaledMarginBottom)
                    }
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

    override fun recycle() {}

    override fun resetBounds() {
        elementBoundsRelativeToPage.reset()
        elementBoundsRelativeToOrigin.reset()
    }

    @Thread(description = "will be called on worker thread.")
    override fun pageMeasurementDone(pageSizeCalculator: PageSizeCalculator) {
        val shouldMakeChangeToElementWidth =
            !layoutParams.isWidthMatchParent() && (!layoutParams.startEndSymmetric) && (!layoutParams.isWidthWrapContent())
        val shouldMakeChangeToElementHeight =
            !layoutParams.isHeightMatchParent() && (!layoutParams.topBottomSymmetric) && (!layoutParams.isHeightWrapContent())

        var elementSizeRelativePage = Size(0, 0)
        if (shouldMakeChangeToElementWidth || shouldMakeChangeToElementHeight) {
            elementSizeRelativePage = pageSizeCalculator.calculateElementSizeRelative(
                Size(
                    layoutParams.desiredWidth,
                    layoutParams.desiredHeight
                )
            )
        }
        if (shouldMakeChangeToElementWidth) {
            layoutParams.desiredWidth = elementSizeRelativePage.width
        }

        if (shouldMakeChangeToElementHeight) {
            layoutParams.desiredHeight = elementSizeRelativePage.height
        }
    }

    override fun draw(canvas: Canvas, args: SparseArray<Any>?) {
        super.draw(canvas, args)
        this.mostRecentArgs = args
        if (debug) {
            getBoundsRelativeToPage(args.shouldDrawSnapShot()).apply {
                canvas.drawRect(this, debugPaint)
            }
        }
    }

    data class LayoutParams(
        var desiredWidth: Int = DEFAULT_WIDTH,
        var desiredHeight: Int = DEFAULT_HEIGHT,
        var leftMargin: Int = 0,
        var topMargin: Int = 0,
        var rightMargin: Int = 0,
        var bottomMargin: Int = 0,
        var startEndSymmetric: Boolean = false,
        var topBottomSymmetric: Boolean = false
    ) {
        fun isWidthMatchParent(): Boolean {
            return desiredWidth == MATCH_PARENT
        }

        fun isHeightMatchParent(): Boolean {
            return desiredHeight == MATCH_PARENT
        }

        fun isWidthWrapContent(): Boolean {
            return desiredWidth == WRAP_CONTENT
        }

        fun isHeightWrapContent(): Boolean {
            return desiredHeight == WRAP_CONTENT
        }
    }

    override fun SparseArray<Any>?.textSizeRelativeToSnap(textSizePixels: Float): Float {
        return page.documentRenderView.toCurrentScale(
            if (shouldDrawSnapShot()) textSizePixels.div(
                page.snapScaleDownFactor
            ) else textSizePixels
        )
    }

    override fun isEventOccurredWithInBounds(
        eventMarker: IMotionEventMarker?,
        checkBasedOnLastDrawCallType: Boolean
    ): Boolean {
        if (eventMarker == null || eventMarker.hasNoMotionEvent()) {
            return false
        }
        val boundRelativeToPage =
            if (checkBasedOnLastDrawCallType && mostRecentArgs != null) {
                getBoundsRelativeToPage(mostRecentArgs.shouldDrawSnapShot())
            } else {
                getBoundsRelativeToPage(false)
            }
        return (boundRelativeToPage.contains(eventMarker.getX(), eventMarker.getY()))
    }


    companion object {
        const val DEFAULT_WIDTH = 64
        const val DEFAULT_HEIGHT = 64
        const val MATCH_PARENT = -1
        const val WRAP_CONTENT = -2
        private const val PAGE_ELEMENT = "pageElement"
        val TAG = PAGE_ELEMENT
    }

}
