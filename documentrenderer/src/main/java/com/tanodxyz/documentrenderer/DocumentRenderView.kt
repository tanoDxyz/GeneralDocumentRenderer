package com.tanodxyz.documentrenderer

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.tanodxyz.documentrenderer.document.Document
import com.tanodxyz.documentrenderer.elements.IElement
import com.tanodxyz.documentrenderer.events.*
import com.tanodxyz.documentrenderer.page.DocumentPage
import com.tanodxyz.documentrenderer.page.PageViewState
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * when pass first is completed
 * check for refactoring of the view.
 *
 * //todo when on size is called off
 * // and document is loaded some time later let's say after 10 secs.
 * // if document has one page.
 * // it will be placed at the top of the screen which is an error.
 * // so go for it.
 */
open class DocumentRenderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), View.OnTouchListener,
    TouchEventsManager.TouchEventsListener, AnimationManager.AnimationListener {

    private lateinit var viewSize: Size
    protected var canShowPageCountBox: Boolean = true
    protected var scrollHandle: ScrollHandle? = null
    private var buzyStateIndicator: IElement? = null
    private var currentPageForImmediateTouchEvent: Int = 0
    /*protected*/ lateinit var document: Document
    protected var buzyTokensCounter = 0

    protected var touchEventMgr: TouchEventsManager
    protected var enableAntialiasing = true

    protected val antialiasFilter =
        PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    protected val pagePaint = Paint(Paint.ANTI_ALIAS_FLAG)


    protected var contentDrawOffsetX = 0F
    protected var contentDrawOffsetY = 0F

    protected var currentPage = 1


    protected var zoom = MINIMUM_ZOOM
    protected var animationManager: AnimationManager
    protected var executor = Executors.newCachedThreadPool()
    protected var _handler = Handler(Looper.getMainLooper())
    var pageNumberDisplayBoxTextColor = Color.WHITE
    var pageNumberDisplayBoxBackgroundColor = Color.parseColor("#343434")
    var pageNumberDisplayBoxTextSize = resources.spToPx(12)
    var pageNumberDisplayBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = pageNumberDisplayBoxTextSize
        style = Paint.Style.FILL
        strokeCap = Paint.Cap.ROUND
    }
    var pageNumberDisplayBoxXAndYMargin = resources.dpToPx(16)
    var _16Dp = resources.dpToPx(16)
    private var pageNumberBoxBackgroundRectangle = RectF(0F, 0F, 0F, 0F)
    private val minZoom = DEFAULT_MIN_SCALE
    private val midZoom = DEFAULT_MID_SCALE
    private val maxZoom = DEFAULT_MAX_SCALE
    val documentPageRequestHandler = DocumentPageRequestHandler()

    init {
        this.setWillNotDraw(false)
        animationManager = AnimationManager(this.context, this)
        this.setOnTouchListener(this)
        touchEventMgr = TouchEventsManager(this.context)
        touchEventMgr.registerListener(this)

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewSize = Size(w, h)
        if (isInEditMode || !isInitalized()) {
            return
        }
        recalculatePageSizesAndSetDefaultXYOffsets(w, h) {
            gotoPageIfApplicable()
        }
    }

    private fun gotoPageIfApplicable() {
        if (currentPage > 1) {
            jumpToPage(currentPage - 1, false)
        }
    }

    @Synchronized
    fun buzy() {
        println("b98u: buz called off ")
        ++buzyTokensCounter
        redraw()
    }

    @Synchronized
    fun free() {
        if (buzyTokensCounter > 0) {
            --buzyTokensCounter
            redraw()
        }
        println("b98u: free called off is $buzyTokensCounter")
    }

    fun isFree(): Boolean {
        return buzyTokensCounter <= 0
    }

    fun stopFling() {
        animationManager.stopFling()
    }

    open fun setPositionOffset(progress: Float, moveHandle: Boolean) {
        if (document.swipeVertical) {
            val docLen = document.getDocLen(zoom) - height
            val maximumScrollbarHeight = scrollHandle!!.getScrollerTotalLength()
            val maximumScrollbarHeightScaled = document.toCurrentScale(maximumScrollbarHeight, zoom)
            val rationBetweenContentLengthAndMaxScroll = docLen.div(maximumScrollbarHeightScaled)
            val contentDrawOffsetY =
                rationBetweenContentLengthAndMaxScroll * -1 * toCurrentScale(progress)
            moveTo(contentDrawOffsetX, contentDrawOffsetY, moveHandle)
        } else {
            val docLen = document.getDocLen(zoom) - width
            val maximumScrollbarWidth = scrollHandle!!.getScrollerTotalLength()
            val maximumScrollbarWidthScaled = document.toCurrentScale(maximumScrollbarWidth, zoom)
            val rationBetweenContentLengthAndMaxScroll = docLen.div(maximumScrollbarWidthScaled)
            val contentDrawOffsetX =
                rationBetweenContentLengthAndMaxScroll * -1 * toCurrentScale(progress)
            moveTo(contentDrawOffsetX, contentDrawOffsetY, moveHandle)
        }

    }

    fun getPositionOffset(): Float {
        return if (document.swipeVertical) {
            val docLen = document.getDocLen(zoom) - height
            val maximumScrollBarHeight = scrollHandle!!.getScrollerTotalLength()
            val maximumScrollBarHeightScaled = document.toCurrentScale(maximumScrollBarHeight, zoom)
            val contentLengthInContext = docLen
            val ratio = maximumScrollBarHeightScaled.div(toCurrentScale(contentLengthInContext))
            abs(ratio * (contentDrawOffsetY))
        } else {
            val docLen = document.getDocLen(zoom) - width
            val maximumScrollBarWidth = scrollHandle!!.getScrollerTotalLength()
            val maximumScrollBarWidthScaled = document.toCurrentScale(maximumScrollBarWidth, zoom)
            val contentLengthInContext = docLen
            val ratio = maximumScrollBarWidthScaled.div(toCurrentScale(contentLengthInContext))
            abs(ratio * contentDrawOffsetX)
        }
    }

    fun addScrollHandle(scrollHandle: ScrollHandle) {
        if (this.scrollHandle != null) {
            this.scrollHandle?.detach()
        }
        scrollHandle.attachTo(this)
        this.scrollHandle = scrollHandle
    }

    private fun recalculatePageSizesAndSetDefaultXYOffsets(
        width: Int,
        height: Int,
        callback: (() -> Unit)? = null
    ) {
        println("b98u: onSIAZE")
        animationManager.stopAll()
        buzy()
        executor.submit {
            println("b98u: exe in size")
            document.recalculatePageSizesAndIndexes(Size(width, height))
            println("b98u: yes reclaimed")
            _handler.post {
                free()
                setDefaultContentDrawOffsets()
                callback?.invoke()
            }
        }
    }

    private fun setDefaultContentDrawOffsets() {
        if (document.swipeVertical) {
            // whenever size changes set X
            val scaledPageWidth: Float = toCurrentScale(document.getMaxPageWidth())
            if (scaledPageWidth < width) {
                contentDrawOffsetX = width.div(2) - scaledPageWidth.div(2)
            }
            // whenever size changes set Y
            val documentHeight = document.getDocLen(zoom)
            if (documentHeight < height) {
                contentDrawOffsetY = (height - documentHeight).div(2)
            }
        } else {
            val scaledPageHeight = toCurrentScale(document.getMaxPageHeight().toFloat())
            if (scaledPageHeight < height) {
                contentDrawOffsetY = height.div(2) - scaledPageHeight.div(2)
            }
            val contentWidth: Float = document.getDocLen(zoom)
            if (contentWidth < width) { // whole document width visible on screen
                contentDrawOffsetX = (width - contentWidth).div(2)
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        bundle.putParcelable("viewState", ViewState(currentPage, zoom))
        super.onSaveInstanceState()
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var superState: Parcelable? = null
        var viewState: ViewState? = null
        if (state is Bundle) {
            viewState = state.getParcelable<ViewState>("viewState")!!
            superState = state.getParcelable("superState")!!
        }
        super.onRestoreInstanceState(superState)
        viewState?.apply {
            this@DocumentRenderView.zoom = this.zoomLevel
            this@DocumentRenderView.currentPage = currentPage
        }
    }


    override fun getCurrentX(): Float {
        return contentDrawOffsetX
    }

    override fun getCurrentY(): Float {
        return contentDrawOffsetY
    }

    override fun zoomCenteredTo(zoom: Float, pivot: PointF) {
        val dzoom = zoom / this.zoom
        zoomTo(zoom)
        var baseX: Float = contentDrawOffsetX * dzoom
        var baseY: Float = contentDrawOffsetY * dzoom
        baseX += pivot.x - pivot.x * dzoom
        baseY += pivot.y - pivot.y * dzoom
        moveTo(baseX, baseY)
    }


    override fun getCurrentZoom(): Float {
        return zoom
    }

    override fun getMinZoom(): Float {
        return minZoom
    }

    override fun getMidZoom(): Float {
        return midZoom
    }

    override fun getMaxZoom(): Float {
        return maxZoom
    }

    override fun isZooming(): Boolean {
        return zoom != MINIMUM_ZOOM
    }

    /**
     * Checks if whole document can be displayed on screen, doesn't include zoom
     *
     * @return true if whole document can displayed at once, false otherwise
     */
    open fun documentFitsView(): Boolean {
        val len: Float = document.getDocLen(1F)
        return if (document.swipeVertical) {
            len < height
        } else {
            len < width
        }
    }

    fun zoomTo(zoom: Float) {
        this.zoom = zoom
    }

    fun changeSwipeMode(swipeVertical: Boolean) {
        document.swipeVertical = swipeVertical
        animationManager.stopAll()
        recalculatePageSizesAndSetDefaultXYOffsets(width, height) {
            val jumpToPage = {
                jumpToPage(currentPage - 1, withAnimation = false)
            }
            if (scrollHandle == null) {
                jumpToPage.invoke()
            }
            scrollHandle?.apply {
                detach()
                attachTo(this@DocumentRenderView) {
                    jumpToPage.invoke()
                }
            }
        }
    }

    fun jumpToPage(pageNumber: Int, withAnimation: Boolean = false) {
        println("oiu: finding it for $pageNumber")

        if (document.swipeVertical) {
            var yOffset = findYForVisiblePage(pageNumber)
            if (yOffset > 0) {
                yOffset *= -1
            }
            if (withAnimation) {
                animationManager.startPageFlingAnimation(yOffset)
            } else {
                moveTo(contentDrawOffsetX, yOffset)
            }
        } else {
            var xOffset = findXForVisiblePage(pageNumber)
            if (xOffset > 0) {
                xOffset *= -1
            }
            if (withAnimation) {
                animationManager.startPageFlingAnimation(xOffset)
            } else {
                moveTo(xOffset, contentDrawOffsetY)
            }
        }
    }

    fun changeSwipeMode() {
        changeSwipeMode(!document.swipeVertical)
    }

    fun dispatchEventToThePagesInFocus(iEvent: IEvent) {
        findAllVisiblePagesOnScreen().forEach { visiblePage ->
            visiblePage.apply {
                if (pageBounds.contains(
                        iEvent.getX(),
                        iEvent.getY()
                    ) || iEvent.hasNoMotionEvent()
                ) {
                    onEvent(iEvent)
                }
            }
        }
    }

    fun findAllVisiblePagesOnScreen(): List<DocumentPage> {
        val visiblePages = mutableListOf<DocumentPage>()
        val documentPages = document.getDocumentPages()
        var forwardPagesScanIndex = (currentPage - 1)
        while (forwardPagesScanIndex < documentPages.count()) {
            val documentPage = documentPages[forwardPagesScanIndex]
            val pageViewState = getPageViewState(documentPage.pageBounds)
            if (pageViewState == PageViewState.VISIBLE
                || pageViewState == PageViewState.PARTIALLY_VISIBLE
            ) {
                visiblePages.add(documentPage)
                ++forwardPagesScanIndex
            } else {
                break
            }
        }
        var backwardPagesScanIndex = (currentPage - 2)
        while (backwardPagesScanIndex >= 0) {
            val documentPage = documentPages[backwardPagesScanIndex]
            val pageViewState = getPageViewState(documentPage.pageBounds)
            if (pageViewState == PageViewState.VISIBLE
                || pageViewState == PageViewState.PARTIALLY_VISIBLE
            ) {
                visiblePages.add(documentPage)
                --backwardPagesScanIndex
            } else {
                break
            }
        }
        return visiblePages
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        dispatchEventToThePagesInFocus(GenericMotionEvent(event))
        return touchEventMgr.onTouchEvent(event)
    }

    override fun onScrollStart(
        downEvent: MotionEvent?, moveEvent: MotionEvent?,
        movementDirections: TouchEventsManager.MovementDirections?,
        distanceX: Float,
        distanceY: Float,
        absoluteX: Float,
        absoluteY: Float
    ) {
        dispatchEventToThePagesInFocus(
            ScrollStartEvent(
                downEvent,
                moveEvent,
                movementDirections,
                distanceX,
                distanceY,
                absoluteX,
                absoluteY
            )
        )
        moveTo(absoluteX, absoluteY)
    }

    override fun onScrollEnd(motionEvent: MotionEvent?) {
        dispatchEventToThePagesInFocus(ScrollEndEvent(motionEvent))
        hideScrollHandleAndPageCountBox()
    }

    override fun flingFinished(motionEvent: MotionEvent?) {
        dispatchEventToThePagesInFocus(FlingEndEvent(motionEvent))
        hideScrollHandleAndPageCountBox()
    }

    override fun onDoubleTap(e: MotionEvent?) {
        dispatchEventToThePagesInFocus(DoubleTapEvent(e))
    }

    override fun onDoubleTapEvent(e: MotionEvent?) {
        dispatchEventToThePagesInFocus(DoubleTapCompleteEvent(e))
    }

    override fun onScaleBegin() {
        dispatchEventToThePagesInFocus(ScaleBeginEvent(null))
    }

    override fun onLongPress(e: MotionEvent?) {
        dispatchEventToThePagesInFocus(LongPressEvent(e))
    }

    override fun onScaleEnd() {
        dispatchEventToThePagesInFocus(ScaleEndEvent(null))
    }

    override fun onShowPress(e: MotionEvent?) {
        dispatchEventToThePagesInFocus(ShowPressEvent(e))
    }

    override fun onSingleTapConfirmed(e: MotionEvent?) {
        dispatchEventToThePagesInFocus(SingleTapConfirmedEvent(e))
    }

    override fun onSingleTapUp(e: MotionEvent?) {
        dispatchEventToThePagesInFocus(SingleTapUpEvent(e))
    }

    open fun hideScrollHandleAndPageCountBox() {
        if (!animationManager.isFlinging()) {
            scrollHandle?.apply {
                postDelayed({
                    canShowPageCountBox = false
                    hide()
                }, SCROLL_HANDLE_AND_PAGE_DISPLAY_BOX_HIDE_DELAY_MILLISECS)
            }
        }
    }

    override fun computeScroll() {
        super.computeScroll()
        if (isInEditMode) {
            return
        }
        animationManager.computeScrollOffset()
    }

    override fun onFling(
        downEvent: MotionEvent?,
        moveEvent: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (document.pageFling && !document.swipeVertical) {
            pageFling(velocityX, velocityY)
            return true
        }
        val minY: Float
        val minX: Float
        if (document.swipeVertical) {
            minY = -(getRenderedDocLen(zoom))
            minX = -(toCurrentScale(document.getMaxPageWidth()))
        } else {
            minX = -(document.getDocLen(getCurrentZoom()) - width)
            minY = -(toCurrentScale(document.getMaxPageHeight()) - height)
        }
        dispatchEventToThePagesInFocus(FlingStartEvent(downEvent, moveEvent, velocityX, velocityY))
        animationManager.startFlingAnimation(
            contentDrawOffsetX.toInt(),
            contentDrawOffsetY.toInt(),
            velocityX.toInt(),
            velocityY.toInt(),
            minX.toInt(),
            0,
            minY.toInt(),
            0
        )

        return true
    }

    override fun onDownEvent() {
        animationManager.stopFling()
        currentPageForImmediateTouchEvent = currentPage
    }


    override fun zoomCenteredRelativeTo(dr: Float, pointF: PointF) {
        zoomCenteredTo(zoom * dr, pointF)
    }

    fun getPageViewState(pageBounds: RectF): PageViewState {
        val viewBounds = RectF(0F, 0F, width.toFloat(), height.toFloat())
        val viewBoundsRelativeToPageBounds =
            RectF(pageBounds.left, pageBounds.top, width.toFloat(), height.toFloat())
        val pageIsTotallyVisible = viewBounds.contains(pageBounds)
        var pageIsPartiallyVisible = false
        if (!pageIsTotallyVisible) {
            val pageAndViewIntersected = viewBoundsRelativeToPageBounds.intersects(
                pageBounds.left,
                pageBounds.top,
                pageBounds.right,
                pageBounds.bottom
            )
            pageIsPartiallyVisible = if (pageAndViewIntersected) {
                if (document.swipeVertical) {
                    if (pageBounds.top < viewBounds.top) {
                        pageBounds.bottom >= viewBounds.top
                    } else {
                        pageBounds.top <= viewBounds.bottom
                    }
                } else {
                    if (pageBounds.left < viewBounds.left) {
                        pageBounds.right >= viewBounds.left
                    } else {
                        pageBounds.left <= viewBounds.right
                    }
                }
            } else {
                false
            }
        }
        val pageViewState =
            if (pageIsTotallyVisible) PageViewState.VISIBLE
            else if (pageIsPartiallyVisible) PageViewState.PARTIALLY_VISIBLE else PageViewState.INVISIBLE
        return pageViewState
    }

    fun getCurrentPage(): DocumentPage? {
        return document.getPage(currentPage - 1)
    }

    open fun checkDoPageFling(velocityX: Float, velocityY: Float): Boolean {
        val absX = abs(velocityX)
        val absY = abs(velocityY)
        return if (document.swipeVertical) absY > absX else absX > absY
    }

    /**
     * page number starting from zero
     */
    fun findXForVisiblePage(pageNo: Int): Float {
        val contentDrawX = 0
        var totalPagesDrawnLength = 0F
        var pageX = 0F
        var pageY = 0F
        var pageEnd = 0F
        var pageBottom = 0F
        var scaledPageStart = 0F
        var rightMarginToSubtract = 0F
        var pageWidthToDraw = 0
        var scaledFitPageWidth = 0F
        var scaledPageY = 0F
        var bottomMarginToSubtract = 0F
        var pageHeightToDraw = 0
        var scaledFitPageHeight = 0F
        val documentPages = document.getDocumentPages()
        val targetPageBounds = RectF(0F, 0F, 0F, 0F)
        var pageVisibilityOffsetX = 0F
        var previousPageVisibilityOffset = 0F
        for (i: Int in 0..pageNo) {
            val page = documentPages[i]
            val previousPage: DocumentPage? = if (i == 0) null else documentPages[i]
            //page x
            scaledPageStart = (toCurrentScale(totalPagesDrawnLength))
            pageX =
                contentDrawX + scaledPageStart + document.pageMargins.left
            // pageY
            scaledPageY = toCurrentScale((document.getMaxPageHeight() - page.size.height).div(2))
            pageY =
                (contentDrawOffsetY + scaledPageY) + document.pageMargins.top

            bottomMarginToSubtract =
                document.pageMargins.top + document.pageMargins.bottom

            pageHeightToDraw = page.size.height
            scaledFitPageHeight = toCurrentScale(pageHeightToDraw)
            pageBottom = (pageY + scaledFitPageHeight) - bottomMarginToSubtract
            // pageEnd
//
            rightMarginToSubtract =
                document.pageMargins.left + document.pageMargins.right

            pageWidthToDraw = page.size.width
            scaledFitPageWidth = toCurrentScale(pageWidthToDraw)
            pageEnd = (pageX + scaledFitPageWidth) - rightMarginToSubtract

            targetPageBounds.apply {
                left = pageX
                right = pageEnd
                top = pageY
                bottom = pageBottom
            }
            totalPagesDrawnLength += /*targetPageBounds.getWidth()*/page.size.width

            if (previousPage != null) {
                val previuosX = contentDrawX + scaledPageStart + document.pageMargins.left
                val previousEnd =
                    (previuosX + toCurrentScale(previousPage.size.width)) - rightMarginToSubtract
                val previousPageWidth = Pair(previuosX, previousEnd).getWidth()
                pageVisibilityOffsetX =
                    ((previousPageWidth + (document.pageMargins.left + document.pageMargins.right)
                            + previousPageVisibilityOffset))
                previousPageVisibilityOffset = pageVisibilityOffsetX
            }
        }
        //todo @remove
//        drawnContentLength = totalPagesDrawnLength
        return pageVisibilityOffsetX
    }

    fun findYForVisiblePage(pageNo: Int): Float {
        val contentDrawY = 0
        var totalPagesDrawnLength = 0F
        var pageX = 0F
        var pageY = 0F
        var pageEnd = 0F
        var pageBottom = 0F
        var scaledPageStart = 0F
        var rightMarginToSubtract = 0F
        var pageWidthToDraw = 0
        var scaledFitPageWidth = 0F
        var scaledPageY = 0F
        var bottomMarginToSubtract = 0F
        var pageHeightToDraw = 0
        var scaledFitPageHeight = 0F
        val documentPages = document.getDocumentPages()
        val targetPageBounds = RectF(0F, 0F, 0F, 0F)
        var pageVisibilityOffsetY = 0F
        var previousPageVisibilityOffset = 0F
        for (i: Int in 0..pageNo) {
            val page = documentPages[i]
            val previousPage: DocumentPage? = if (i == 0) null else documentPages[i]
            scaledPageStart =
                (toCurrentScale(document.getMaxPageWidth() - page.size.width).div(2))
            pageX =
                contentDrawOffsetX + scaledPageStart + document.pageMargins.left;

            rightMarginToSubtract =
                document.pageMargins.left + document.pageMargins.right
            pageWidthToDraw = page.size.width
            scaledFitPageWidth = toCurrentScale(pageWidthToDraw)
            pageEnd = (pageX + scaledFitPageWidth) - rightMarginToSubtract
            // page Y
            scaledPageY = (toCurrentScale(
                totalPagesDrawnLength
            ))
            pageY =
                contentDrawY + document.pageMargins.top + scaledPageY
            // page bottom
            bottomMarginToSubtract =
                document.pageMargins.top + document.pageMargins.bottom

            pageHeightToDraw = page.size.height
            scaledFitPageHeight = toCurrentScale(pageHeightToDraw)
            pageBottom = (pageY + scaledFitPageHeight) - bottomMarginToSubtract

            targetPageBounds.apply {
                left = pageX
                right = pageEnd
                top = pageY
                bottom = pageBottom
            }
            totalPagesDrawnLength += /*targetPageBounds.getHeight()*/page.size.height
            if (previousPage != null) {
                val previousY = contentDrawY + document.pageMargins.top + scaledPageY
                val previousBottom =
                    (previousY + toCurrentScale(previousPage.size.height)) - bottomMarginToSubtract
                val previousPageHeight = Pair(previousY, previousBottom).getHeight()
                pageVisibilityOffsetY =
                    ((previousPageHeight + (document.pageMargins.top + document.pageMargins.bottom)
                            + previousPageVisibilityOffset))
                previousPageVisibilityOffset = pageVisibilityOffsetY
            }
        }
        return pageVisibilityOffsetY
    }

    /**
     * page number starting from zero
     */
    fun findPageBoundsFor(pageNo: Int, contentDrawX: Float): RectF {
        var totalPagesDrawnLength = 0F
        var pageX = 0F
        var pageY = 0F
        var pageEnd = 0F
        var pageBottom = 0F
        var scaledPageStart = 0F
        var rightMarginToSubtract = 0F
        var pageWidthToDraw = 0
        var scaledFitPageWidth = 0F
        var scaledPageY = 0F
        var bottomMarginToSubtract = 0F
        var pageHeightToDraw = 0
        var scaledFitPageHeight = 0F
        val documentPages = document.getDocumentPages()
        val targetPageBounds = RectF(0F, 0F, 0F, 0F)
        for (i: Int in 0..pageNo) {
            val page = documentPages[i]
            //page x
            scaledPageStart = (toCurrentScale(totalPagesDrawnLength))
            pageX =
                contentDrawX + scaledPageStart + document.pageMargins.left
            // pageY
            scaledPageY = toCurrentScale((document.getMaxPageHeight() - page.size.height).div(2))
            pageY =
                (contentDrawOffsetY + scaledPageY) + document.pageMargins.top

            bottomMarginToSubtract =
                document.pageMargins.top + document.pageMargins.bottom

            pageHeightToDraw = page.size.height
            scaledFitPageHeight = toCurrentScale(pageHeightToDraw)
            pageBottom = (pageY + scaledFitPageHeight) - bottomMarginToSubtract
            // pageEnd
//
            rightMarginToSubtract =
                document.pageMargins.left + document.pageMargins.right

            pageWidthToDraw = page.size.width
            scaledFitPageWidth = toCurrentScale(pageWidthToDraw)
            pageEnd = (pageX + scaledFitPageWidth) - rightMarginToSubtract

            targetPageBounds.apply {
                left = pageX
                right = pageEnd
                top = pageY
                bottom = pageBottom
            }
            totalPagesDrawnLength += page.size.width
        }

        return targetPageBounds
    }

    private fun pageFling(velocityX: Float, velocityY: Float) {
        var minX: Float = contentDrawOffsetX
        var minY: Float = contentDrawOffsetY
        var maxX: Float = contentDrawOffsetX
        var maxY: Float = contentDrawOffsetY
        val targetPageBoundsAccordingToX =
            findPageBoundsFor(currentPageForImmediateTouchEvent - 1, contentDrawOffsetX)
        if (!document.swipeVertical) {
            val rightMarginsToAccountFor = (document.pageMargins.right)
            val leftMarginToAccountFor = (document.pageMargins.left)
            if (targetPageBoundsAccordingToX.left < 0) {
                if (targetPageBoundsAccordingToX.right <= width) {
                    minX += -(targetPageBoundsAccordingToX.right + rightMarginsToAccountFor)
                    maxX += abs(targetPageBoundsAccordingToX.left) + leftMarginToAccountFor
                } else {
                    minX += -((targetPageBoundsAccordingToX.right - width) + rightMarginsToAccountFor)
                    maxX += abs(targetPageBoundsAccordingToX.left) + leftMarginToAccountFor
                }
            } else {
                if (targetPageBoundsAccordingToX.right > width) {
                    minX += -((targetPageBoundsAccordingToX.right - width) + rightMarginsToAccountFor)
                    maxX += abs(width - targetPageBoundsAccordingToX.left) + leftMarginToAccountFor
                } else {
                    minX += -(targetPageBoundsAccordingToX.width() + rightMarginsToAccountFor)
                    maxX += targetPageBoundsAccordingToX.width() + leftMarginToAccountFor
                }
            }
            if (targetPageBoundsAccordingToX.top < 0) {
                if (targetPageBoundsAccordingToX.bottom <= height) {
                    minY = contentDrawOffsetY;
                    maxY += abs(targetPageBoundsAccordingToX.top)
                } else {
                    minY += -(targetPageBoundsAccordingToX.bottom - height)
                    maxY += abs(targetPageBoundsAccordingToX.top)
                }
            } else {
                if (targetPageBoundsAccordingToX.bottom > height) {
                    minY += -(targetPageBoundsAccordingToX.bottom - height)
                    maxY = contentDrawOffsetY
                }
            }
        }


        animationManager.startFlingAnimation(
            contentDrawOffsetX.roundToInt(),
            contentDrawOffsetY.roundToInt(),
            velocityX.toInt(),
            velocityY.toInt(),
            minX.toInt(),
            maxX.toInt(),
            minY.toInt(),
            maxY.toInt()
        )
    }

    open fun toCurrentScale(size: Number): Float {
        return size.toFloat() * zoom
    }

    fun setBuzyStateIndicator(iElement: IElement) {
        this.buzyStateIndicator = iElement
    }

    open fun getRenderedDocLen(zoom: Float): Float {
        return document.getTotalContentLength() * zoom
    }

    override fun moveTo(absX: Float, absY: Float, moveHandle: Boolean) {
        if (document.swipeVertical) {
            val documentHeight = getRenderedDocLen(zoom)
            contentDrawOffsetY = if (documentHeight < height) {
                (height - documentHeight).div(2)
            } else {
                val contentEnd = absY + documentHeight + document.pageMargins.bottom
                if (absY > 0) {
                    0F
                } else {
                    if (contentEnd < height) {
                        val delta = height - contentEnd
                        absY + delta
                    } else {
                        absY
                    }
                }
            }
//            x
            var offsetX = absX
            val scaledPageWidth: Float = toCurrentScale(document.getMaxPageWidth())
            if (scaledPageWidth < width) {
                offsetX = width.div(2) - scaledPageWidth.div(2)
            } else {
                if (offsetX > 0) {
                    offsetX = 0f
                } else if (offsetX + scaledPageWidth < width) {
                    offsetX = width - scaledPageWidth
                }
            }
//
            contentDrawOffsetX = offsetX
        } else {

            // Check Y offset
            var offsetY = absY
            val scaledPageHeight = toCurrentScale(document.getMaxPageHeight())
            if (scaledPageHeight < height) {
                offsetY = height.div(2) - scaledPageHeight.div(2)
            } else {
                if (offsetY > 0) {
                    offsetY = 0f
                } else if (offsetY + scaledPageHeight < height) {
                    offsetY = height - scaledPageHeight
                }
            }

            // Check X offset
            var offsetX = absX
            val contentWidth: Float = document.getDocLen(zoom)
            if (contentWidth < width) { // whole document width visible on screen
                offsetX = (width - contentWidth).div(2)
            } else {
                if (offsetX > 0) { // left visible
                    offsetX = 0f
                } else if (offsetX + contentWidth < width) { // right visible
                    offsetX = -contentWidth + width
                }
            }
            contentDrawOffsetY = offsetY
            contentDrawOffsetX = offsetX
        }
        if (moveHandle && scrollHandle != null) {
            scrollHandle!!.scroll(getPositionOffset())
        }
        canShowPageCountBox = true
        redraw()
    }

    override fun redraw() {
        invalidate()
    }

    override fun isSwipeVertical(): Boolean {
        return document.swipeVertical
    }


    fun loadDocument(document: Document, callback: (() -> Unit)? = null) {
        this.document = document
        pagePaint.apply {
            style = Paint.Style.FILL_AND_STROKE
            color = if (document.nightMode) Color.BLACK else document.pageBackColor
        }
        buzy()
        executor.submit {
            println("b98u: yes one ")
            this.document.setup(Size(width, height))
            _handler.post {
                println("b98u: yew two")
                free()
                redraw()
                callback?.invoke()
            }
        }
    }

    fun isInitalized(): Boolean {
        return this::document.isInitialized
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (isInEditMode || !isInitalized()) {
            return
        }

        canvas?.let { drawBackground(it) }

        if (buzyTokensCounter > 0 && buzyStateIndicator != null) {
            animationManager.stopAll()
            buzyStateIndicator!!.draw(canvas!!)
            touchEventMgr.enabled = false
            postInvalidateDelayed(REFRESH_RATE_IN_CASE_VIEW_BUZY)
            return
        } else {
            touchEventMgr.enabled = true
        }

        canvas?.apply {
            drawFilter = if (enableAntialiasing && drawFilter == null) {
                antialiasFilter
            } else {
                null
            }
            var drawnContentLength = 0F
            val documentPages = document.getPagesToBeDrawn(currentPage, viewSize)

//            if(currentPage == 0 || currentPage == 1) {
//            for (i: Int in 0 until 12) {
//                val page = documentPages[i]
//                drawPageBackground(page, drawnContentLength)
//                currentPage = calculateCurrentPage(page)
//                val shouldDrawPage = page.shouldDrawPage(i)
//                println("sanjo: should draw $shouldDrawPage $i")
//                if (shouldDrawPage) {
//                    // draw page.
////                    visiblePagesOnScreen.add(i)
//                } else {
//                    // recycle page
//                }
//                page.draw(this, shouldDrawPage)
//            }

//            documentPages. {
//                val documentPage = documentPages[i]
//                val pageViewState = getPageViewState(documentPage.pageBounds)
//                drawPageBackground(documentPage,0F)
//                documentPage.draw(canvas,true)
//                currentPage = calculateCurrentPage(documentPage)
//            }
            println("9ui: -------------------")
            println("9ui: count is ${documentPages.count()}")
            documentPages.forEach { documentPage ->
                println("9ui: got ${documentPage.uniquieID}")
                val pageViewState = getPageViewState(documentPage.pageBounds)
                drawPageBackground(documentPage, 0F)
                documentPage.draw(canvas, pageViewState.isPagePartiallyOrCompletelyVisible())
                currentPage = calculateCurrentPage(documentPage)
            }
            println("9ui: --------------------")
//                if(backwardPageScan) {
//                    val documentPage = documentPages[backwardIndex]
//                    drawPageBackground(documentPage,0F)
////                    currentPage = calculateCurrentPage(documentPage)
//                    --backwardIndex
//                    backwardPageScan = getPageViewState(documentPage.pageBounds).isPagePartiallyOrCompletelyVisible() && backwardIndex > -1
//                }
//            }

//            }
//            for (i: Int in documentPages.indices) {
//                val page = documentPages[i]
//                drawPageBackground(page, drawnContentLength)
//                currentPage = calculateCurrentPage(
//                    i,
//                    page.pageBounds
//                )
//                drawnContentLength += if (document.swipeVertical) {
//                    page.size.height
//                } else {
//                    page.size.width
//                }
//                val shouldDrawPage = page.shouldDrawPage(i)
//                println("sanjo: should draw $shouldDrawPage $i")
//                if (shouldDrawPage) {
//                    // draw page.
////                    visiblePagesOnScreen.add(i)
//                } else {
//                    // recycle page
//                }
//                page.draw(this, shouldDrawPage)
//            }
            if (canShowPageCountBox) {
                this.drawPageNumber()
            }
        }
    }

    fun calculateCurrentPage(page: DocumentPage): Int {
        val pageViewState = getPageViewState(page.pageBounds)
        return if (pageViewState == PageViewState.VISIBLE) {
            page.uniquieID + 1
        } else if (document.swipeVertical) {
            if (page.pageBounds.top < (height.div(2F)) /*&& page.pageBounds.bottom > 0*/) {
                page.uniquieID + 1
            } else {
                currentPage
            }
        } else {
            if (page.pageBounds.left < (width.div(2F)) /*&& page.pageBounds.right > 0*/) {
                page.uniquieID + 1
            } else {
                currentPage
            }
        }
    }

    open fun Canvas.drawPageNumber() {

        val textToDisplay = "$currentPage / ${document.getPagesCount()}"
        val textBounds = Rect()
        pageNumberDisplayBoxPaint.getTextBounds(textToDisplay, 0, textToDisplay.count(), textBounds)
        val drawX = pageNumberDisplayBoxXAndYMargin
        val drawY = if (scrollHandle == null || document.swipeVertical) {
            height - (pageNumberDisplayBoxXAndYMargin + textBounds.height() + _16Dp)
        } else {
            height - (scrollHandle!!.scrollBarHeight + scrollHandle!!.marginUsed + pageNumberDisplayBoxXAndYMargin + textBounds.height() + _16Dp)
        }

        pageNumberDisplayBoxPaint.color = pageNumberDisplayBoxBackgroundColor
        pageNumberDisplayBoxPaint.alpha = 127

        pageNumberBoxBackgroundRectangle.apply {
            left = drawX
            top = drawY
            right = left + textBounds.width() + _16Dp
            bottom = top + textBounds.height() + _16Dp
        }

        drawRoundRect(pageNumberBoxBackgroundRectangle, 100F, 100F, pageNumberDisplayBoxPaint)


        pageNumberDisplayBoxPaint.color = pageNumberDisplayBoxTextColor
        pageNumberDisplayBoxPaint.alpha = 255

        val textDrawX = pageNumberBoxBackgroundRectangle.left + _16Dp.div(2)
        val textDrawY = pageNumberBoxBackgroundRectangle.top + _16Dp
        drawText(textToDisplay, textDrawX, textDrawY, pageNumberDisplayBoxPaint)
    }

    fun calculateCurrentPage(index: Int, pageBounds: RectF): Int {
        val pageIndex: Int
        if (getPageViewState(
                pageBounds
            ) == PageViewState.VISIBLE
        ) {
            pageIndex = index + 1
        } else if (document.swipeVertical) {
            pageIndex = if (pageBounds.top < (height.div(2F))) {
                index + 1
            } else {
                currentPage
            }
        } else {
            pageIndex = if (pageBounds.left < (width.div(2F))) {
                index + 1
            } else {
                currentPage
            }
        }
        return pageIndex
    }


    open fun DocumentPage.shouldDrawPage(): Boolean {
        return getPageViewState(this.pageBounds).isPagePartiallyOrCompletelyVisible()
    }

    open fun Canvas.drawPageBackground(
        page: DocumentPage,
        totalPagesDrawnLength: Float
    ) {
        var pageX = 0F
        var pageY = 0F
        var pageEnd = 0F
        var pageBottom = 0F
        var scaledPageStart = 0F
        var rightMarginToSubtract = 0F
        var pageWidthToDraw = 0
        var scaledFitPageWidth = 0F
        var scaledPageY = 0F
        var bottomMarginToSubtract = 0F
        var pageHeightToDraw = 0
        var scaledFitPageHeight = 0F
        if (document.swipeVertical) {
            // page x
            scaledPageStart =
                (toCurrentScale(document.getMaxPageWidth() - page.size.width).div(2))
            pageX =
                contentDrawOffsetX + scaledPageStart + document.pageMargins.left;

            rightMarginToSubtract =
                document.pageMargins.left + document.pageMargins.right
            pageWidthToDraw = page.size.width
            scaledFitPageWidth = toCurrentScale(pageWidthToDraw)
            pageEnd = (pageX + scaledFitPageWidth) - rightMarginToSubtract
            // page Y
            scaledPageY = (toCurrentScale(
                totalPagesDrawnLength
            ))
            pageY = contentDrawOffsetY + document.pageMargins.top + toCurrentScale(
                document.pageIndexes.get(page.uniquieID).y
            )
//                contentDrawOffsetY + document.pageMargins.top + scaledPageY
            // page bottom
            bottomMarginToSubtract =
                document.pageMargins.top + document.pageMargins.bottom

            pageHeightToDraw = page.size.height
            scaledFitPageHeight = toCurrentScale(pageHeightToDraw)
            pageBottom = (pageY + scaledFitPageHeight) - bottomMarginToSubtract
        } else {
            //page x
            scaledPageStart = (toCurrentScale(totalPagesDrawnLength))
            pageX =
                contentDrawOffsetX + scaledPageStart + document.pageMargins.left
            // pageY
            scaledPageY = toCurrentScale((document.getMaxPageHeight() - page.size.height).div(2))
            pageY =
                (contentDrawOffsetY + scaledPageY) + document.pageMargins.top

            bottomMarginToSubtract =
                document.pageMargins.top + document.pageMargins.bottom

            pageHeightToDraw = page.size.height
            scaledFitPageHeight = toCurrentScale(pageHeightToDraw)
            pageBottom = (pageY + scaledFitPageHeight) - bottomMarginToSubtract
            // pageEnd
//
            rightMarginToSubtract =
                document.pageMargins.left + document.pageMargins.right

            pageWidthToDraw = page.size.width
            scaledFitPageWidth = toCurrentScale(pageWidthToDraw)
            pageEnd = (pageX + scaledFitPageWidth) - rightMarginToSubtract
        }
        page.pageBounds.apply {
            left = pageX
            right = pageEnd
            top = pageY
            bottom = pageBottom
        }
        if (document.pageCorners > 0) {
            drawRoundRect(
                RectF(pageX, pageY, pageEnd, pageBottom),
                document.pageCorners,
                document.pageCorners,
                pagePaint
            )
        } else {
            drawRect(RectF(pageX, pageY, pageEnd, pageBottom), pagePaint)
        }
    }


    open fun drawBackground(canvas: Canvas) {
        val bg = background
        if (bg == null) {
            canvas.drawColor(if (document.nightMode) Color.BLACK else Color.LTGRAY)
        } else {
            bg.draw(canvas)
        }
    }

    override fun resetZoomWithAnimation() {
        zoomWithAnimation(minZoom)
    }

    override fun zoomWithAnimation(centerX: Float, centerY: Float, scale: Float) {
        animationManager.startZoomAnimation(centerX, centerY, zoom, scale)
    }

    override fun zoomWithAnimation(scale: Float) {
        animationManager.startZoomAnimation(
            (width.div(2)).toFloat(),
            (height.div(2)).toFloat(), zoom, scale
        )
    }

    inner class DocumentPageRequestHandler : PageRequests {
        override fun redraw() {
            redraw()
        }

        override fun enableDisableScroll(enable: Boolean) {
            touchEventMgr.scrollingEnabled = enable
        }

        override fun stopFling() {
            stopFling()
        }

        override fun enableDisableFling(enable: Boolean) {
            touchEventMgr.flingingEnabled = enable
        }

        override fun enableDisableScale(enable: Boolean) {
            touchEventMgr.scalingEnabled = enable
        }

        override fun getCurrentX(): Float {
            return contentDrawOffsetX
        }

        override fun getCurrentY(): Float {
            return contentDrawOffsetY
        }

        override fun setCurrentX(x: Float) {
            contentDrawOffsetX = x
        }

        override fun setCurrentY(y: Float) {
            contentDrawOffsetY = y
        }

        override fun getPageCount(): Int {
            return document.getPagesCount()
        }

        override fun jumpToPage(pageNumber: Int, withAnimation: Boolean) {
            this@DocumentRenderView.jumpToPage(pageNumber, withAnimation)
        }
    }

    companion object {
        val DEFAULT_MAX_SCALE = 3.0f // todo change this
        val DEFAULT_MID_SCALE = 1.75f //todo and this to change the double tap zoom levels
        val DEFAULT_MIN_SCALE = 1.0f
        var MAXIMUM_ZOOM = 3.0f
        var MINIMUM_ZOOM = 1.0F
        var REFRESH_RATE_IN_CASE_VIEW_BUZY = 10L
        var SCROLL_HANDLE_AND_PAGE_DISPLAY_BOX_HIDE_DELAY_MILLISECS = 1000L
    }
}