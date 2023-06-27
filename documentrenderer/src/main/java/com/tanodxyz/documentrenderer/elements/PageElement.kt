package com.tanodxyz.documentrenderer.elements

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.SparseArray
import androidx.annotation.VisibleForTesting
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.Size
import com.tanodxyz.documentrenderer.Thread
import com.tanodxyz.documentrenderer.events.IMotionEventMarker
import com.tanodxyz.documentrenderer.events.LongPressEvent
import com.tanodxyz.documentrenderer.events.SingleTapConfirmedEvent
import com.tanodxyz.documentrenderer.page.DocumentPage
import com.tanodxyz.documentrenderer.reset
import java.security.SecureRandom
import kotlin.math.roundToInt

open class PageElement(var page: DocumentPage) : InteractiveElement {
    open var moveable = true
    var debug = false
    protected var mobileModeActivated = false
    var clickListener: OnClickListener? = null
    var longPressListener: OnLongPressListener? = null
    var shouldChangeSizeBasedOnPageSizeCalculator = true
    var mostRecentArgs: SparseArray<Any>? = null
    protected var actualWidth = -1F
    protected var actualHeight = -1F
    var desiredWidth = 0F
    var desiredHeight = 0F
    var margins = RectF(0F, 0F, 0F, 0F) //todo make protected
    protected var paddings = RectF(0F, 0F, 0F, 0F) //todo make protected
    protected val debugPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 8F
    }
    open var type: String = TAG
    @VisibleForTesting
    val elementsContainerBounds = RectF()

    @VisibleForTesting
    val elementContentBounds = RectF()

    open fun getContentWidth(args: SparseArray<Any>?): Float {
        return page.documentRenderView.toCurrentScale(desiredWidth)
    }

    open fun getContentHeight(args: SparseArray<Any>?): Float {
        return page.documentRenderView.toCurrentScale(desiredHeight)
    }

    override fun onEvent(iMotionEventMarker: IMotionEventMarker?): Boolean {
        clickListener?.apply {
            if (isEventOccurredWithInBounds(
                    iMotionEventMarker, true
                ) && iMotionEventMarker is SingleTapConfirmedEvent
            ) {
                onClick(iMotionEventMarker, this@PageElement)
            }
        }

        longPressListener?.apply {
            if (isEventOccurredWithInBounds(
                    iMotionEventMarker, true
                ) && iMotionEventMarker is LongPressEvent
            ) {
                longPressListener?.onLongPress(iMotionEventMarker, this@PageElement)
            }
        }

        if (moveable) {
            handleElementMovement(iMotionEventMarker)
        }

        return false
    }

    open fun handleElementMovement(iMotionEventMarker: IMotionEventMarker?) {
        val elementBounds = this.getContentBoundsRelativeToPage(mostRecentArgs.shouldDrawSnapShot())
        val eventOccurredWithInBounds = isEventOccurredWithInBounds(iMotionEventMarker, true)
        if (iMotionEventMarker is LongPressEvent && eventOccurredWithInBounds) {
            mobileModeActivated = true
            page.documentRenderView.canProcessTouchEvents = false
            page.clearSnapShot()
        }

        if (!eventOccurredWithInBounds && iMotionEventMarker is SingleTapConfirmedEvent) {
            mobileModeActivated = false
            page.documentRenderView.canProcessTouchEvents = true
            page.clearSnapShot()
        }

        if (mobileModeActivated && eventOccurredWithInBounds) {
            val deltaX = iMotionEventMarker!!.getX() - elementBounds.centerX()
            val deltaY = iMotionEventMarker!!.getY() - elementBounds.centerY()
            margins.left += deltaX
            margins.top += deltaY
        }
    }

    override fun reset() {
        elementsContainerBounds.reset()
        elementContentBounds.reset()
    }

    override fun recycle() {
    }

    @Thread(description = "will be called on worker thread.")
    override fun pageMeasurementDone(documentRenderView: DocumentRenderView) {
        if (shouldChangeSizeBasedOnPageSizeCalculator) {
            val size = Size(
                getContentWidth(mostRecentArgs).roundToInt(),
                getContentHeight(mostRecentArgs).roundToInt()
            )
            val elementSizeRelativePage =
                documentRenderView.pageSizeCalculator!!.calculateElementSizeRelative(
                    size
                )
            actualWidth = elementSizeRelativePage.width.toFloat()
            actualHeight = elementSizeRelativePage.height.toFloat()
        }
    }

    override fun draw(canvas: Canvas, args: SparseArray<Any>?) {
        this.mostRecentArgs = args
        if (debug) {
            this.getContentBoundsRelativeToPage(args.shouldDrawSnapShot()).apply {
                debugPaint.color = Color.RED
                canvas.drawRect(this, debugPaint)
            }
        }
        showIndicationIfMobileModeActive(canvas, args)
    }

    open fun showIndicationIfMobileModeActive(canvas: Canvas, args: SparseArray<Any>?) {
        if (mobileModeActivated) {
            if (page.pageViewState().isObjectPartiallyOrCompletelyVisible()) {
                this.getContentBoundsRelativeToPage(args.shouldDrawSnapShot()).apply {
                    debugPaint.color = colors[secureRandom.nextInt(colors.size)]
                    canvas.drawRect(this, debugPaint)
                }
                page.documentRenderView.postInvalidateDelayed(1000)
            }
        }
    }

    fun SparseArray<Any>?.shouldDrawSnapShot(): Boolean {
        return this != null && this[DocumentPage.RE_DRAW_WITH_RELATIVE_TO_ORIGIN_SNAPSHOT_] == true
    }

    fun SparseArray<Any>?.forceCalculate(): Boolean {
        return this != null && this[DocumentPage.FORCE_CALCULATE] == true
    }

    fun SparseArray<Any>?.addFlag(flag: Int, value: Boolean = true): SparseArray<Any> {
        val arr =
            this ?: SparseArray<Any>(1)

        arr.put(flag, value)
        return arr
    }


    open fun getContentBoundsRelativeToPage(drawSnapShot: Boolean): RectF {
        val scaledMargins = getScaledMargins(drawSnapShot)

        var left = 0F
        var top = 0F
        var right = 0F
        var bottom = 0F
        if (drawSnapShot) {
            left = scaledMargins.left
            top = scaledMargins.top
            val scaledDownHeight = getContentHeight(mostRecentArgs).div(page.snapScaleDownFactor)
            val scaleDownWidth = getContentWidth(mostRecentArgs).div(page.snapScaleDownFactor)
            if (scaledMargins.right > 0) {
                left -= scaledMargins.right
            }
            if (scaledMargins.bottom > 0) {
                top -= scaledMargins.bottom
            }
            right = (left + scaleDownWidth)
            bottom = (top + scaledDownHeight)
        } else {
            left = page.pageBounds.left + scaledMargins.left
            top = page.pageBounds.top + scaledMargins.top
            if (scaledMargins.right > 0) {
                left -= scaledMargins.right
            }
            if (scaledMargins.bottom > 0) {
                top -= scaledMargins.bottom
            }
            right = (left + getContentWidth(mostRecentArgs))
            bottom = (top + getContentHeight(mostRecentArgs))
        }
        elementsContainerBounds.apply {
            this.left = left
            this.right = right
            this.bottom = bottom
            this.top = top
        }
        return elementsContainerBounds
    }

    open fun getScaledMargins(drawSnapShot: Boolean): RectF {
        val scaledMargins = RectF()
        val lm = margins.left
        val tm = margins.top
        val rm = margins.right
        val bm = margins.bottom
        var slm = 0F
        var stm = 0F
        var srm = 0F
        var sbm = 0F
        if (drawSnapShot) {
            slm = page.documentRenderView.toCurrentScale(lm.div(page.snapScaleDownFactor))
            stm = page.documentRenderView.toCurrentScale(tm.div(page.snapScaleDownFactor))
            srm = page.documentRenderView.toCurrentScale(rm.div(page.snapScaleDownFactor))
            sbm = page.documentRenderView.toCurrentScale(bm.div(page.snapScaleDownFactor))
        } else {
            slm = page.documentRenderView.toCurrentScale(lm)
            stm = page.documentRenderView.toCurrentScale(tm)
            srm = page.documentRenderView.toCurrentScale(rm)
            sbm = page.documentRenderView.toCurrentScale(bm)
        }
        scaledMargins.apply {
            left = slm
            top = stm
            right = srm
            bottom = sbm
        }
        return scaledMargins
    }

    open fun getScaledPaddings(drawSnapShot: Boolean): RectF {
        val scaledPaddings = RectF()
        val lp = paddings.left
        val tp = paddings.top
        val rp = paddings.right
        val bp = paddings.bottom
        var slp = 0F
        var stp = 0F
        var srp = 0F
        var sbp = 0F
        if (drawSnapShot) {
            slp = page.documentRenderView.toCurrentScale(lp.div(page.snapScaleDownFactor))
            stp = page.documentRenderView.toCurrentScale(tp.div(page.snapScaleDownFactor))
            srp = page.documentRenderView.toCurrentScale(rp.div(page.snapScaleDownFactor))
            sbp = page.documentRenderView.toCurrentScale(bp.div(page.snapScaleDownFactor))
        } else {
            slp = page.documentRenderView.toCurrentScale(lp)
            stp = page.documentRenderView.toCurrentScale(tp)
            srp = page.documentRenderView.toCurrentScale(rp)
            sbp = page.documentRenderView.toCurrentScale(bp)
        }
        scaledPaddings.apply {
            left = slp
            top = stp
            right = srp
            bottom = sbp
        }
        return scaledPaddings
    }

    fun SparseArray<Any>?.textSizeRelativeToSnap(textSizePixels: Float): Float {
        return page.documentRenderView.toCurrentScale(
            if (shouldDrawSnapShot()) textSizePixels.div(
                page.snapScaleDownFactor
            ) else textSizePixels
        )
    }

    fun SparseArray<Any>?.propertyRelativeToSnap(value: Float): Float {
        return page.documentRenderView.toCurrentScale(
            if (shouldDrawSnapShot()) value.div(
                page.snapScaleDownFactor
            ) else value
        )
    }

    fun isEventOccurredWithInBounds(
        eventMarker: IMotionEventMarker?, checkBasedOnLastDrawCallType: Boolean
    ): Boolean {
        if (eventMarker == null || eventMarker.hasNoMotionEvent()) {
            return false
        }
        val boundRelativeToPage = if (checkBasedOnLastDrawCallType && mostRecentArgs != null) {
            this.getContentBoundsRelativeToPage(mostRecentArgs.shouldDrawSnapShot())
        } else {
            this.getContentBoundsRelativeToPage(false)
        }
        return (boundRelativeToPage.contains(eventMarker.getX(), eventMarker.getY()))
    }

    fun SparseArray<Any>?.getLeftAndTop(content: Boolean): PointF {
        val drawSnapShot = this.shouldDrawSnapShot()
        val boundsRelativeToPage =
            if (content) this@PageElement.getContentBoundsRelativeToPage(drawSnapShot) else this@PageElement.getContentBoundsRelativeToPage(
                drawSnapShot
            )
        return PointF(boundsRelativeToPage.left, boundsRelativeToPage.top)
    }

    interface OnClickListener {
        fun onClick(eventMarker: IMotionEventMarker?, pageElement: PageElement)
    }

    interface OnLongPressListener {
        fun onLongPress(eventMarker: IMotionEventMarker?, pageElement: PageElement)
    }

    companion object {
        const val DEFAULT_WIDTH = 200F
        const val DEFAULT_HEIGHT = 100F
        private const val PAGE_ELEMENT = "pageElement"
        val TAG = PAGE_ELEMENT
        val colors = mutableListOf<Int>().apply {
            add(Color.GREEN)
            add(Color.RED)
            add(Color.MAGENTA)
            add(Color.GRAY)
            add(Color.DKGRAY)
            add(Color.CYAN)
            add(Color.YELLOW)
        }
        val secureRandom = SecureRandom()
    }

}