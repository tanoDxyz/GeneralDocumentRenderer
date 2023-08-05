package com.tanodxyz.documentrenderer.elements

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.SparseArray
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.Size
import com.tanodxyz.documentrenderer.Thread
import com.tanodxyz.documentrenderer.events.DoubleTapCompleteEvent
import com.tanodxyz.documentrenderer.events.DoubleTapEvent
import com.tanodxyz.documentrenderer.events.FlingEndEvent
import com.tanodxyz.documentrenderer.events.FlingStartEvent
import com.tanodxyz.documentrenderer.events.IMotionEventMarker
import com.tanodxyz.documentrenderer.events.LongPressEvent
import com.tanodxyz.documentrenderer.events.ScaleBeginEvent
import com.tanodxyz.documentrenderer.events.ScaleEndEvent
import com.tanodxyz.documentrenderer.events.ScaleEvent
import com.tanodxyz.documentrenderer.events.ScrollEndEvent
import com.tanodxyz.documentrenderer.events.ScrollStartEvent
import com.tanodxyz.documentrenderer.events.ShowPressEvent
import com.tanodxyz.documentrenderer.events.SingleTapConfirmedEvent
import com.tanodxyz.documentrenderer.events.SingleTapUpEvent
import com.tanodxyz.documentrenderer.page.DocumentPage
import com.tanodxyz.documentrenderer.reset
import java.security.SecureRandom
import kotlin.math.roundToInt

/**
 * simple implementation of [InteractiveElement].
 * it supports different predefined event listeners.
 *
 */
open class PageElement(var page: DocumentPage) : InteractiveElement {
    /**
     * It will make the element movable on long press.
     */
    open var movable = false

    /**
     * rectangle is drawn around content bounds.
     */
    var debug = false
    protected var mobileModeActivated = false
    protected var usePreCalculatedBounds: Boolean = false
    protected var actualWidth = -1F
    protected var actualHeight = -1F
    var clickListener: OnClickListener? = null
    var longPressListener: OnLongPressListener? = null
    var doubleTapCompleteListener: OnDoubleTapCompleteListener? = null
    var doubleTapListener: OnDoubleTapListener? = null
    var onFlingStartListener: OnFlingStartListener? = null
    var onFlingEndListener: OnFlingEndListener? = null
    var onScaleBeginListener: OnScaleBeginListener? = null
    var onScaleListener: OnScaleListener? = null
    var onScaleEndListener: OnScaleEndListener? = null
    var onScrollStartListener: OnScrollStartListener? = null
    var onScrollEndListener: OnScrollEndListener? = null
    var onShowPressListener: OnShowPressListener? = null
    var onSingleTapUpListener: OnSingleTapUpListener? = null
    var shouldChangeSizeBasedOnPageSizeCalculator = true
    var mostRecentArgs: SparseArray<Any>? = null
    open var type: String = TAG
    val elementContentBounds = RectF()
    var desiredWidth = 0F

    /**
     * Equal margins will be applied to element from all sides.
     * only Left and Top margins are used for this purpose.
     * use it when you know that width is equal to parent [DocumentRenderView.getWidth] and
     * height respectively.
     */
    var symmetric  = false
    var desiredHeight = 0F
    var margins = RectF(0F, 0F, 0F, 0F)
    var paddings = RectF(0F, 0F, 0F, 0F)
    protected val debugPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 8F
    }

    override fun getContentWidth(args: SparseArray<Any>?): Float {
        return page.documentRenderView.toCurrentScale(desiredWidth)
    }

    override fun getContentHeight(args: SparseArray<Any>?): Float {
        return page.documentRenderView.toCurrentScale(desiredHeight)
    }

    private fun dispatchCallbacksToListenersIfAttached(iMotionEventMarker: IMotionEventMarker?) {
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

        doubleTapCompleteListener?.apply {
            if (isEventOccurredWithInBounds(
                    iMotionEventMarker, true
                ) && iMotionEventMarker is DoubleTapCompleteEvent
            ) {
                doubleTapCompleteListener?.onDoubleTapComplete(iMotionEventMarker, this@PageElement)
            }
        }

        doubleTapListener?.apply {
            if (isEventOccurredWithInBounds(
                    iMotionEventMarker, true
                ) && iMotionEventMarker is DoubleTapEvent
            ) {
                doubleTapListener?.onDoubleTap(iMotionEventMarker, this@PageElement)
            }
        }

        onFlingStartListener?.apply {
            if (isEventOccurredWithInBounds(
                    iMotionEventMarker, true
                ) && iMotionEventMarker is FlingStartEvent
            ) {
                onFlingStartListener?.onFlingStart(iMotionEventMarker, this@PageElement)
            }
        }

        onFlingEndListener?.apply {
            if (isEventOccurredWithInBounds(
                    iMotionEventMarker, true
                ) && iMotionEventMarker is FlingEndEvent
            ) {
                onFlingEndListener?.onFlingEnd(iMotionEventMarker, this@PageElement)
            }
        }

        onScaleBeginListener?.apply {
            if (isEventOccurredWithInBounds(
                    iMotionEventMarker, true
                ) && iMotionEventMarker is ScaleBeginEvent
            ) {
                onScaleBeginListener?.onScaleBegin(iMotionEventMarker, this@PageElement)
            }
        }

        onScaleEndListener?.apply {
            if (isEventOccurredWithInBounds(
                    iMotionEventMarker, true
                ) && iMotionEventMarker is ScaleEndEvent
            ) {
                onScaleEndListener?.onScaleEnd(iMotionEventMarker, this@PageElement)
            }
        }

        onScaleListener?.apply {
            if (isEventOccurredWithInBounds(
                    iMotionEventMarker, true
                ) && iMotionEventMarker is ScaleEvent
            ) {
                onScaleListener?.onScale(iMotionEventMarker, this@PageElement)
            }
        }

        onScrollStartListener?.apply {
            if (isEventOccurredWithInBounds(
                    iMotionEventMarker, true
                ) && iMotionEventMarker is ScrollStartEvent
            ) {
                onScrollStartListener?.onScrollStart(iMotionEventMarker, this@PageElement)
            }
        }

        onScaleEndListener?.apply {
            if (isEventOccurredWithInBounds(
                    iMotionEventMarker, true
                ) && iMotionEventMarker is ScrollEndEvent
            ) {
                onScrollEndListener?.onScrollEnd(iMotionEventMarker, this@PageElement)
            }
        }
        onShowPressListener?.apply {
            if (isEventOccurredWithInBounds(
                    iMotionEventMarker, true
                ) && iMotionEventMarker is ShowPressEvent
            ) {
                onShowPressListener?.onShowPress(iMotionEventMarker, this@PageElement)
            }
        }

        onSingleTapUpListener?.apply {
            if (isEventOccurredWithInBounds(
                    iMotionEventMarker, true
                ) && iMotionEventMarker is SingleTapUpEvent
            ) {
                onSingleTapUpListener?.onSingleTapUp(iMotionEventMarker, this@PageElement)
            }
        }
    }

    override fun onEvent(event: IMotionEventMarker?): Boolean {
        dispatchCallbacksToListenersIfAttached(event)
        if (movable) {
            handleElementMovement(event)
        }

        return false
    }

    open fun handleElementMovement(iMotionEventMarker: IMotionEventMarker?) {
        val elementBounds = this.getContentBounds(mostRecentArgs.shouldDrawSnapShot())
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
            val deltaY = iMotionEventMarker.getY() - elementBounds.centerY()
            margins.left += deltaX
            margins.top += deltaY
        }
    }

    override fun reset() {
        elementContentBounds.reset()
        elementContentBounds.reset()
        usePreCalculatedBounds = false
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
            this.getContentBounds(args.shouldDrawSnapShot()).apply {
                debugPaint.color = Color.RED
                canvas.drawRect(this, debugPaint)
            }
        }
        showIndicationIfMobileModeActive(canvas, args)
    }

    open fun showIndicationIfMobileModeActive(canvas: Canvas, args: SparseArray<Any>?) {
        if (mobileModeActivated) {
            if (page.pageViewState().isObjectPartiallyOrCompletelyVisible()) {
                this.getContentBounds(args.shouldDrawSnapShot()).apply {
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

    override fun setContentBounds(bounds: RectF) {
        elementContentBounds.left = bounds.left
        elementContentBounds.top = bounds.top
        elementContentBounds.right = bounds.right
        elementContentBounds.bottom = bounds.bottom
        usePreCalculatedBounds = true
    }

    override fun getContentBounds(drawSnapShot: Boolean): RectF {
        if(usePreCalculatedBounds) {
            return elementContentBounds
        }
        val scaledMargins = getScaledMargins(drawSnapShot)

        var left: Float
        var top: Float
        val right: Float
        val bottom: Float
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
            right = if(symmetric) {
                (left + scaleDownWidth) - scaledMargins.left.times(2)
            } else {
                (left + scaleDownWidth)
            }

            bottom = if(symmetric) {
                (top + scaledDownHeight) - scaledMargins.top.times(2)
            } else {
                (top + scaledDownHeight)
            }
        } else {
            left = page.pageBounds.left + scaledMargins.left
            top = page.pageBounds.top + scaledMargins.top
            if (scaledMargins.right > 0) {
                left -= scaledMargins.right
            }
            if (scaledMargins.bottom > 0) {
                top -= scaledMargins.bottom
            }
            right = if(symmetric) {
                (left + getContentWidth(mostRecentArgs)) - (scaledMargins.left.times(2))
            } else {
                (left + getContentWidth(mostRecentArgs))
            }

            bottom = if(symmetric) {
                (top + getContentHeight(mostRecentArgs)) - (scaledMargins.top.times(2))
            } else {
                (top + getContentHeight(mostRecentArgs))
            }
        }
        elementContentBounds.apply {
            this.left = left
            this.right = right
            this.bottom = bottom
            this.top = top
        }
        return elementContentBounds
    }

    override fun getScaledMargins(drawSnapShot: Boolean): RectF {
        val scaledMargins = RectF()
        val lm = margins.left
        val tm = margins.top
        val rm = margins.right
        val bm = margins.bottom
        val slm: Float
        val stm: Float
        val srm: Float
        val sbm: Float
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
        val slp: Float
        val stp: Float
        val srp: Float
        val sbp: Float
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
            this.getContentBounds(mostRecentArgs.shouldDrawSnapShot())
        } else {
            this.getContentBounds(false)
        }
        return (boundRelativeToPage.contains(eventMarker.getX(), eventMarker.getY()))
    }

    fun SparseArray<Any>?.getLeftAndTop(): PointF {
        val drawSnapShot = this.shouldDrawSnapShot()
        val boundsRelativeToPage =
            this@PageElement.getContentBounds(
                drawSnapShot
            )
        return PointF(boundsRelativeToPage.left, boundsRelativeToPage.top)
    }

    interface OnClickListener {
        fun onClick(eventMarker: SingleTapConfirmedEvent?, pageElement: PageElement)
    }

    interface OnLongPressListener {
        fun onLongPress(eventMarker: LongPressEvent?, pageElement: PageElement)
    }

    interface OnDoubleTapCompleteListener {
        fun onDoubleTapComplete(eventMarker: DoubleTapCompleteEvent?, pageElement: PageElement)
    }

    interface OnDoubleTapListener {
        fun onDoubleTap(eventMarker: DoubleTapEvent?, pageElement: PageElement)
    }

    interface OnFlingStartListener {
        fun onFlingStart(eventMarker: FlingStartEvent?, pageElement: PageElement)
    }

    interface OnFlingEndListener {
        fun onFlingEnd(eventMarker: FlingEndEvent?, pageElement: PageElement)
    }

    interface OnScaleBeginListener {
        fun onScaleBegin(eventMarker: ScaleBeginEvent?, pageElement: PageElement)
    }

    interface OnScaleListener {
        fun onScale(eventMarker: ScaleEvent?, pageElement: PageElement)
    }

    interface OnScaleEndListener {
        fun onScaleEnd(eventMarker: ScaleEndEvent?, PageElement: PageElement)
    }

    interface OnScrollStartListener {
        fun onScrollStart(eventMarker: ScrollStartEvent?, pageElement: PageElement)
    }

    interface OnScrollEndListener {
        fun onScrollEnd(eventMarker: ScrollEndEvent?, pageElement: PageElement)
    }

    interface OnShowPressListener {
        fun onShowPress(eventMarker: ShowPressEvent?, pageElement: PageElement)
    }

    interface OnSingleTapUpListener {
        fun onSingleTapUp(eventMarker: SingleTapUpEvent?, pageElement: PageElement)
    }

    companion object {
        const val DEFAULT_WIDTH = 200
        const val DEFAULT_HEIGHT = 100
        private const val PAGE_ELEMENT = "pageElement"
        const val TAG = PAGE_ELEMENT
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