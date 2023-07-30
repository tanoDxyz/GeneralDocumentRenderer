package com.tanodxyz.documentrenderer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PaintFlagsDrawFilter
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.widget.FrameLayout
import com.tanodxyz.documentrenderer.document.Document
import com.tanodxyz.documentrenderer.elements.IElement
import com.tanodxyz.documentrenderer.elements.InteractiveElement
import com.tanodxyz.documentrenderer.events.DoubleTapCompleteEvent
import com.tanodxyz.documentrenderer.events.DoubleTapEvent
import com.tanodxyz.documentrenderer.events.FlingEndEvent
import com.tanodxyz.documentrenderer.events.FlingStartEvent
import com.tanodxyz.documentrenderer.events.GenericMotionEvent
import com.tanodxyz.documentrenderer.events.IMotionEventMarker
import com.tanodxyz.documentrenderer.events.LongPressEvent
import com.tanodxyz.documentrenderer.events.ScaleBeginEvent
import com.tanodxyz.documentrenderer.events.ScaleEndEvent
import com.tanodxyz.documentrenderer.events.ScrollEndEvent
import com.tanodxyz.documentrenderer.events.ScrollStartEvent
import com.tanodxyz.documentrenderer.events.ShowPressEvent
import com.tanodxyz.documentrenderer.events.SingleTapConfirmedEvent
import com.tanodxyz.documentrenderer.events.SingleTapUpEvent
import com.tanodxyz.documentrenderer.extensions.ScrollHandle
import com.tanodxyz.documentrenderer.extensions.ViewExtension
import com.tanodxyz.documentrenderer.page.DocumentPage
import com.tanodxyz.documentrenderer.page.ObjectViewState
import com.tanodxyz.documentrenderer.pagesizecalculator.PageSizeCalculator
import org.jetbrains.annotations.TestOnly
import java.util.BitSet
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Typical usage of the view is.
 *
 * `val renderView = findViewById(...)`
 *
 *  `val document = Document(context)`
 *
 *  `val pages = List<DocumentPage>(10000)`
 *
 *  `document.addPages(pages)`
 *
 *  `renderView.loadDocument(document)`
 *>
 *  The View works as follows.
 *
 *  [Document] is a long strip of blocks ( [DocumentPage]).
 *
 *  the view works as a scrollable window and this windows can be moved via flings and scrolls.
 *
 */
open class DocumentRenderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr),
    View.OnTouchListener,
    TouchEventsManager.TouchEventsListener, AnimationManager.AnimationListener {

    /**
     * current saved state which will be restored if for some reason is view recreated.
     */
    protected var viewState: SavedState? = null

    /**
     * If any configuration changes occurs the method [DocumentRenderView.onConfigurationChanged] will be triggered.
     */
    protected var layoutChangeListener =
        OnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                onConfigurationChanged()
            }
        }

    /**
     * View extensions that are attached to the view.
     * @see [ViewExtension]
     */
    protected var viewExtensions:MutableList<ViewExtension> = mutableListOf()

    /**
     * View size for current running configuration
     */
    private lateinit var viewSize: Size
    protected var canShowPageCountBox: Boolean = true

    /**
     * Scroll attached to View.
     */
    protected var scroller: ScrollHandle? = null

    /**
     * when view is doing some job or processing document and not able to render document at the instant
     * this busyIndicator will be drawn.
     * view will not process any touch events in the mean time.
     */
    protected var busyIndicator: IElement? = null
    protected var currentPageForImmediateTouchEvent: Int = 0
    internal lateinit var document: Document

    /**
     * Counts number of times [busy] was called
     */
    protected var busyTokensCounter = 0
    internal var eventsIdentityHelper = EventsIdentityHelper()
    protected var touchEventMgr: TouchEventsManager
    protected var isViewInitialized = false
    protected val antialiasFilter =
        PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    protected val pagePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * it is the x value from where view will start drawing document pages.
     */
    protected var contentDrawOffsetX = 0F

    /**
     * it is the x value from where view will start drawing document pages.
     */

    protected var contentDrawOffsetY = 0F

    /**
     * It is the current page visible on screen
     */
    protected var currentPage = 1

    /**
     * current scale or zoom level
     */
    protected var zoom = MINIMUM_ZOOM
    protected var animationManager: AnimationManager
    protected var _handler: Handler? = null
    var displayMetrics: DisplayMetrics? = null
    var threadPoolExecutor: ThreadPoolExecutor? = null
    var enableAntialiasing = true
    var pageNumberDisplayBoxTextColor = Color.WHITE
    var pageNumberDisplayBoxBackgroundColor = Color.parseColor("#343434")
    var pageNumberDisplayBoxTextSize = resources.spToPx(12)
    var pageNumberDisplayBoxXAndYMargin = resources.dpToPx(16)
    var pageNumberDisplayBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = pageNumberDisplayBoxTextSize
        style = Paint.Style.FILL
        strokeCap = Paint.Cap.ROUND
    }
    var cache = CacheManager(CACHE_FACTOR)
    var onViewStateListener: OnViewStateListener? = null
    var renderListener: RenderListener? = null

    /**
     * Setting it to false will make the [DocumentRenderView] not to receive any touch events and hence no
     * scrolling, flinging or scaling but individual pages will receive those touch events and the view will be redrawn.
     * it is useful when you are interacting with an element in any page and don't want the parent or [DocumentRenderView] to scroll or move.
     */
    var canProcessTouchEvents = true
    var idleStateCallback: IdleStateCallback? = null
    val _16Dp = resources.dpToPx(16)

    /**
     * ***this flag indicates that events should be dispatched to the pages who generated it.***
     * setting it to false means that any pages that [DocumentRenderView] has drawn - the event will be dispatched to all of them and it will be the page's responsibility to
     * check if they want to use that particular event.
     * it is necessary to set it to false . for example if the view is scaling and user start scrolling as well in that case if
     * this flag is true the pages that were not visible but are now due to user's double scroll and scale gesture will not receive
     * scale gesture.
     *
     * `it is one use case of keeping it to false but it is up to you`
     */
    var eventsDispatchedToPropagatingPageOnly = false
    private var pageNumberBoxBackgroundRectangle = RectF(0F, 0F, 0F, 0F)
    private val minZoom = DEFAULT_MIN_SCALE
    private val midZoom = DEFAULT_MID_SCALE
    private val maxZoom = DEFAULT_MAX_SCALE

    init {
        threadPoolExecutor = Executors.newCachedThreadPool() as ThreadPoolExecutor
        _handler = Handler(Looper.getMainLooper())
        this.setWillNotDraw(false)
        animationManager = AnimationManager(this.context, this)
        this.setOnTouchListener(this)
        touchEventMgr = TouchEventsManager(this.context)
        touchEventMgr.registerListener(this)
        displayMetrics = resources.displayMetrics
        this.addOnLayoutChangeListener(layoutChangeListener)
    }

    final override fun addOnLayoutChangeListener(listener: OnLayoutChangeListener?) {
        super.addOnLayoutChangeListener(listener)
    }

    fun putInCache(blob: CacheManager.Blob) {
        cache.offer(blob)

    }

    fun retrieveFromCache(key: String): CacheManager.Blob? {
        return cache.get(key)
    }

    open fun onConfigurationChanged() {
    }

    @SuppressLint("ClickableViewAccessibility")
    final override fun setOnTouchListener(l: OnTouchListener?) {
        super.setOnTouchListener(l)
    }

    fun getDocument(): Document {
        return document
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewSize = Size(w, h)
        if (isInEditMode || !isInitialized()) {
            return
        }
        recalculatePageSizesAndSetDefaultXYOffsets() {
            gotoPageIfApplicable()
        }
    }

    private fun gotoPageIfApplicable() {
        if (currentPage > 1) {
            val pagesCount = document.getPagesCount() -1
            val jumpPage = if((currentPage -1) > pagesCount) {
                pagesCount
            } else {
                currentPage - 1
            }
            jumpToPage0(jumpPage, false)
        }
    }

    val isScaling: Boolean get() = eventsIdentityHelper.isScaling()
    val isScrolling: Boolean get() = eventsIdentityHelper.isScrolling()
    val isFlinging: Boolean get() = eventsIdentityHelper.isFlinging()
    val pageSizeCalculator: PageSizeCalculator? get() = document.pageSizeCalculator

    /**
     * If you are doing something long-running and needs to show progress bar with some text.
     * call the following method.
     * when you are done with the long-running process and is free now.
     * call [free].
     * it can be called multiple times.
     *
     * ### For each [busy] call corresponding [free] call is necessary.
     */
    @Synchronized
    fun busy() {
        ++busyTokensCounter
        redraw()
    }

    /**
     * Releases View from busy state.
     */
    @Synchronized
    fun free() {
        if (busyTokensCounter > 0) {
            --busyTokensCounter
            redraw()
        }
    }

    @Synchronized
    fun isFree(): Boolean {
        return busyTokensCounter <= 0
    }

    fun stopFling() {
        animationManager.stopFling()
    }

    fun stopScroll() {
        enableDisableScroll = false
    }

    var enableDisableFling: Boolean
        get() = touchEventMgr.flingingEnabled
        set(value) {
            touchEventMgr.flingingEnabled = value
        }
    var enableDisableScroll: Boolean
        get() = touchEventMgr.scrollingEnabled
        set(value) {
            touchEventMgr.scrollingEnabled = value
        }

    open fun setPositionOffset(progress: Float, moveHandle: Boolean) {
        if (document.swipeVertical) {
            val docLen = document.getDocLen(zoom) - height
            val maximumScrollbarHeight = scroller!!.getScrollerTotalLength()
            val maximumScrollbarHeightScaled = document.toCurrentScale(maximumScrollbarHeight, zoom)
            val rationBetweenContentLengthAndMaxScroll = docLen.div(maximumScrollbarHeightScaled)
            val contentDrawOffsetY =
                rationBetweenContentLengthAndMaxScroll * -1 * toCurrentScale(progress)

            val maxPages = document.getPagesCount()
            val page =
                (maxPages.div(maximumScrollbarHeightScaled) * toCurrentScale(progress)).roundToInt()
            currentPage = if (page <= 0) 1 else page
            moveTo(contentDrawOffsetX, contentDrawOffsetY, moveHandle)

        } else {
            val docLen = document.getDocLen(zoom) - width
            val maximumScrollbarWidth = scroller!!.getScrollerTotalLength()
            val maximumScrollbarWidthScaled = document.toCurrentScale(maximumScrollbarWidth, zoom)
            val rationBetweenContentLengthAndMaxScroll = docLen.div(maximumScrollbarWidthScaled)
            val contentDrawOffsetX =
                rationBetweenContentLengthAndMaxScroll * -1 * toCurrentScale(progress)
            val maxPages = document.getPagesCount()
            val page =
                (maxPages.div(maximumScrollbarWidthScaled) * toCurrentScale(progress)).roundToInt()
            currentPage = if (page <= 0) 1 else page
            moveTo(contentDrawOffsetX, contentDrawOffsetY, moveHandle)
        }
    }

    fun getPositionOffset(): Float {
        return if (document.swipeVertical) {
            val docLen = document.getDocLen(zoom) - height
            val maximumScrollBarHeight = scroller!!.getScrollerTotalLength()
            val maximumScrollBarHeightScaled = document.toCurrentScale(maximumScrollBarHeight, zoom)
            val ratio = maximumScrollBarHeightScaled.div(toCurrentScale(docLen))
            abs(ratio * (contentDrawOffsetY))
        } else {
            val docLen = document.getDocLen(zoom) - width
            val maximumScrollBarWidth = scroller!!.getScrollerTotalLength()
            val maximumScrollBarWidthScaled = document.toCurrentScale(maximumScrollBarWidth, zoom)
            val ratio = maximumScrollBarWidthScaled.div(toCurrentScale(docLen))
            abs(ratio * contentDrawOffsetX)
        }
    }

    fun setScrollHandler(scrollHandle: ScrollHandle) {
        if (this.scroller != null) {
            this.scroller?.detach(this)
        }
        scrollHandle.attachTo(this)
        this.scroller = scrollHandle
    }

    fun attachViewExtension(viewExtension: ViewExtension) {
        if(this.viewExtensions.contains(viewExtension).not()) {
            viewExtension.attachTo(this)
            this.viewExtensions.add(viewExtension)
        }
    }

    fun removeViewExtension(viewExtension: ViewExtension) {
        if(viewExtensions.remove(viewExtension)) {
            viewExtension.detach(this)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        recycle()
    }

    fun recycle() {
        _handler?.removeCallbacksAndMessages(null)
        _handler = null
        scroller = null
        threadPoolExecutor?.shutdownNow()
        threadPoolExecutor = null
        contentDrawOffsetY = 0F
        contentDrawOffsetX = 0F
        cache.recycle()
    }

    protected fun recalculatePageSizesAndSetDefaultXYOffsets(
        callback: (() -> Unit)? = null
    ) {
        animationManager.stopAll()
        busy()
        threadPoolExecutor?.submit {
            document.recalculatePageSizesAndIndexes(this)
            _handler?.post {
                free()
                setDefaultContentDrawOffsets()
                callback?.invoke()
            }
        }
    }

    protected fun setDefaultContentDrawOffsets() {
        if (document.swipeVertical) {
            // whenever modifiedSize changes set X
            val scaledPageWidth: Float = toCurrentScale(document.getMaxPageWidth())
            contentDrawOffsetX = if (scaledPageWidth <= width) {
                width.div(2) - scaledPageWidth.div(2)
            } else {
                -(scaledPageWidth.div(2) - width.div(2))
            }
            // whenever modifiedSize changes set Y
            val documentHeight = document.getDocLen(zoom)
            if (documentHeight < height) {
                contentDrawOffsetY = (height - documentHeight).div(2)
            }
        } else {
            val scaledPageHeight = toCurrentScale(document.getMaxPageHeight().toFloat())
            contentDrawOffsetY = if (scaledPageHeight < height) {
                height.div(2) - scaledPageHeight.div(2)
            } else {
                -(scaledPageHeight.div(2) - height.div(2))
            }
            val contentWidth: Float = document.getDocLen(zoom)
            if (contentWidth < width) { // whole document widthSpec visible on screen
                contentDrawOffsetX = (width - contentWidth).div(2)
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        bundle.putParcelable("viewState", SavedState(currentPage, zoom))
        super.onSaveInstanceState()
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var superState: Parcelable? = null
        if (state is Bundle) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                viewState = state.getParcelable("viewState", SavedState::class.java)!!
                superState = state.getParcelable("superState", Parcelable::class.java)!!
            } else {
                viewState = state.getParcelable("viewState")!!
                superState = state.getParcelable("superState")!!
            }
        }
        super.onRestoreInstanceState(superState)
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
     * @return true if whole document can be displayed at once, false otherwise
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
        recalculatePageSizesAndSetDefaultXYOffsets {
            val jumpToPage = {
                jumpToPage0(currentPage - 1, withAnimation = false)
            }
            if (scroller == null) {
                jumpToPage.invoke()
            }
            scroller?.apply {
                detach(this@DocumentRenderView)
                attachTo(this@DocumentRenderView) {
                    jumpToPage.invoke()
                }
            }
        }
    }

    fun jumpToPage(pageNumber: Int, withAnimation: Boolean = false) {
        jumpToPage0(pageNumber - 1, withAnimation)
    }

    /**
     * accepts zero based page indexing.
     */
    protected fun jumpToPage0(pageNumber: Int, withAnimation: Boolean = false) {
        currentPage = pageNumber
        if (document.swipeVertical) {
            val pageHeight = toCurrentScale(document.getPage(pageNumber)!!.modifiedSize.height)
            var yOffset = toCurrentScale(document.pageIndexes[pageNumber].y)
            if (pageHeight < height) {
                val screenCenterY = (viewSize.height.div(2) - pageHeight.div(2))
                if (screenCenterY + pageHeight < height) {
                    yOffset -= screenCenterY
                }
            }
            yOffset *= -1
            if (withAnimation) {
                animationManager.startPageFlingAnimation(yOffset)
            } else {
                moveTo(contentDrawOffsetX, yOffset)
            }
        } else {
            val pageWidth = toCurrentScale(document.getPage(pageNumber)!!.modifiedSize.width)
            var xOffset = toCurrentScale(document.pageIndexes[pageNumber].x)
            if (pageWidth < width) {
                val screenCenterX = (viewSize.width.div(2) - pageWidth.div(2))
                if (screenCenterX + pageWidth < width) {
                    xOffset -= screenCenterX
                }
            }
            xOffset *= -1
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

    /**
     * Programmatically you can send events to pages that are in focus.
     *
     * ***Focused pages are those which will be drawn on next View's draw call***
     */
    fun dispatchEventToThePagesInFocus(iMotionEventMarker: IMotionEventMarker) {
        if (canViewReceiveTouchEvents()) {
            eventsIdentityHelper.feed(iMotionEventMarker)
            findAllVisiblePagesOnScreen().forEach { visiblePage ->
                visiblePage.apply {
                    if (eventsDispatchedToPropagatingPageOnly) {
                        if (pageBounds.contains(
                                iMotionEventMarker.getX(),
                                iMotionEventMarker.getY()
                            ) || iMotionEventMarker.hasNoMotionEvent()
                        ) {
                            onEvent(iMotionEventMarker)
                        }
                    } else {
                        onEvent(iMotionEventMarker)
                    }
                }
            }
        }
    }

    /**
     * @return list of all pages that are either visible or partially visible.
     */
    fun findAllVisiblePagesOnScreen(): List<DocumentPage> {
        val visiblePages = mutableListOf<DocumentPage>()
        val documentPages = document.getDocumentPages()
        if (!document.isEmpty()) {
            var forwardPagesScanIndex = (currentPage - 1)
            while (forwardPagesScanIndex < documentPages.count()) {
                val documentPage = documentPages[forwardPagesScanIndex]
                val pageViewState = getPageViewState(documentPage.pageBounds)
                if (pageViewState == ObjectViewState.VISIBLE
                    || pageViewState == ObjectViewState.PARTIALLY_VISIBLE
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
                if (pageViewState == ObjectViewState.VISIBLE
                    || pageViewState == ObjectViewState.PARTIALLY_VISIBLE
                ) {
                    visiblePages.add(documentPage)
                    --backwardPagesScanIndex
                } else {
                    break
                }
            }
        }
        return visiblePages
    }

    override fun performClick(): Boolean {
        super.performClick()
        return false
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        dispatchEventToThePagesInFocus(GenericMotionEvent(event))
        // touch events will be dispatched to pages and elements and since view can't process
        // touch events as {canProcessTouchEvents is false} - no scroll or fling will be observed.
        if (!canProcessTouchEvents) {
            redraw()
        }
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
        if (canProcessTouchEvents) {
            moveTo(absoluteX, absoluteY)
        }
    }

    override fun onScrollEnd(motionEvent: MotionEvent?) {
        dispatchEventToThePagesInFocus(ScrollEndEvent(motionEvent))
        if (canProcessTouchEvents) {
            hideScrollHandleAndPageCountBox()
        }
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

    override fun canViewReceiveTouchEvents(): Boolean {
        return isInitialized()
    }

    override fun onSingleTapConfirmed(e: MotionEvent?) {
        dispatchEventToThePagesInFocus(SingleTapConfirmedEvent(e))
    }

    override fun onSingleTapUp(e: MotionEvent?) {
        dispatchEventToThePagesInFocus(SingleTapUpEvent(e))
    }

    open fun hideScrollHandleAndPageCountBox() {
        if (!animationManager.isFlinging()) {
            postDelayed({
                canShowPageCountBox = false
                scroller?.hide()
            }, SCROLL_HANDLE_AND_PAGE_DISPLAY_BOX_HIDE_DELAY_MILLI_SECONDS)

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
        if (canProcessTouchEvents) {
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
            dispatchEventToThePagesInFocus(
                FlingStartEvent(
                    downEvent,
                    moveEvent,
                    velocityX,
                    velocityY
                )
            )
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
        }
        return true
    }

    override fun onDownEvent() {
        animationManager.stopFling()
        currentPageForImmediateTouchEvent = currentPage
    }

    override fun zoomCenteredRelativeTo(dr: Float, pointF: PointF) {
        if (canProcessTouchEvents) {
            zoomCenteredTo(zoom * dr, pointF)
        }
    }

    /**
     * @param pageBounds of [DocumentPage]
     * @return the current view state of [DocumentPage] in the form of [ObjectViewState]
     */
    fun getPageViewState(pageBounds: RectF): ObjectViewState {
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
        return if (pageIsTotallyVisible) ObjectViewState.VISIBLE
        else if (pageIsPartiallyVisible) ObjectViewState.PARTIALLY_VISIBLE else ObjectViewState.INVISIBLE
    }

    fun getCurrentPage(): DocumentPage? {
        return document.getPage(currentPage - 1)
    }

    fun getCurrentPageIndex(): Int {
        return currentPage
    }

    protected fun findPageBoundsFor(pageNo: Int, contentDrawX: Float): RectF {
        val normalizedPageNo = if (pageNo <= 0) 0 else pageNo
        val pageX: Float
        val pageY: Float
        val pageEnd: Float
        val pageBottom: Float
        val pageWidthToDraw: Int
        val scaledPageY: Float
        val pageHeightToDraw: Int
        val documentPages = document.getDocumentPages()
        val targetPageBounds = RectF(0F, 0F, 0F, 0F)
        val page = documentPages[normalizedPageNo]
        //page leftMargin
        val scaledPageStart = (toCurrentScale(document.pageIndexes[normalizedPageNo].x))
        pageX =
            contentDrawX + scaledPageStart + document.pageMargins.left
        // pageY
        scaledPageY =
            toCurrentScale((document.getMaxPageHeight() - page.modifiedSize.height).div(2))
        pageY =
            (contentDrawOffsetY + scaledPageY) + document.pageMargins.top

        val bottomMarginToSubtract: Float = document.pageMargins.top + document.pageMargins.bottom

        pageHeightToDraw = page.modifiedSize.height
        val scaledFitPageHeight: Float = toCurrentScale(pageHeightToDraw)
        pageBottom = (pageY + scaledFitPageHeight) - bottomMarginToSubtract
        // pageEnd
//
        val rightMarginToSubtract: Float = document.pageMargins.left + document.pageMargins.right

        pageWidthToDraw = page.modifiedSize.width
        val scaledFitPageWidth: Float = toCurrentScale(pageWidthToDraw)
        pageEnd = (pageX + scaledFitPageWidth) - rightMarginToSubtract

        targetPageBounds.apply {
            left = pageX
            right = pageEnd
            top = pageY
            bottom = pageBottom
        }

        return targetPageBounds
    }

    @TestOnly
    fun __pageFling(velocityX: Float, velocityY: Float) {
        pageFling(velocityX, velocityY)
    }

    protected fun pageFling(velocityX: Float, velocityY: Float) {
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
                    minY = contentDrawOffsetY
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

    /**
     * This is widely used method and implies that provided [size] will be scaled to match [DocumentRenderView]s
     * zoom/scale level.
     * @param size number for example width,height or anything that needs to be scaled appropriately.
     *
     */
    open fun toCurrentScale(size: Number): Float {
        return size.toFloat() * zoom
    }

    /**
     * An element that will be shown when [busy] method is called.
     *
     * ***Remember unless [free] is called [IElement.draw] will be called continuously with some delay.***
     */
    open fun setBusyStateIndicator(iElement: IElement) {
        this.busyIndicator = iElement
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
//            leftMargin
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
            if (contentWidth < width) { // whole document widthSpec visible on screen
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
        if (moveHandle && scroller != null) {
            scroller!!.scroll(getPositionOffset())
        }
        canShowPageCountBox = true
        redraw()
    }

    override fun redraw() {
        if (isMainThread()) {
            invalidate()
        } else {
            postInvalidate()
        }
    }

    override fun isSwipeVertical(): Boolean {
        return document.swipeVertical
    }

    @Thread(description = "does not matter on which thread it is called")
    fun loadDocument(document: Document, callback: (() -> Unit)? = null) {
        this.document = document
        pagePaint.apply {
            style = Paint.Style.FILL_AND_STROKE
            color = if (document.nightMode) Color.BLACK else document.pageBackColor
        }
        busy()
        threadPoolExecutor?.submit {
            this.document.setup(this)
            _handler?.post {
                free()
                setDefaultContentDrawOffsets()
                redraw()
                callback?.invoke()
            }
        }
    }

    fun getPageCount(): Int = document.getPagesCount()

    private fun isInitialized(): Boolean {
        return this::document.isInitialized
    }

    /**
     * called when any layout pass is complete but called only once for each configuration the view is in.
     */
    open fun onViewInitialized() {
        viewState?.apply {
            this@DocumentRenderView.zoom = this.zoomLevel
            this@DocumentRenderView.currentPage = currentPage
            gotoPageIfApplicable()
        }
        onViewStateListener?.onStateChanged(ViewState.INITIALIZED)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (BuildConfig.DEBUG) {
            idleStateCallback?.renderViewState(isFree())
        }
        if (isInEditMode || !isInitialized()) {
            return
        }

        if (!isViewInitialized && width > 0 && height > 0) {
            isViewInitialized = true
            onViewInitialized()
        }

        drawBackground(canvas)

        synchronized(this) {
            if (busyTokensCounter > 0 && busyIndicator != null) {
                animationManager.stopAll()
                busyIndicator!!.draw(canvas)
                canProcessTouchEvents = false
                postInvalidateDelayed(REFRESH_RATE_IN_CASE_VIEW_BUSY)
                onViewStateListener?.onStateChanged(ViewState.BUSY)
                return
            } else {
                canProcessTouchEvents = true
            }
        }

        canvas.apply {
            drawFilter = if (enableAntialiasing && drawFilter == null) {
                antialiasFilter
            } else {
                null
            }

            val documentPages = document.getPagesToBeDrawn(
                currentPage,
                if (document.swipeVertical) viewSize.height.shl(1) else viewSize.width.shl(1)
            )

            if (documentPages.isEmpty()) {
                drawDocumentIsEmptyMessage(canvas)
                currentPage = 0
                canProcessTouchEvents = false
            } else {
                canProcessTouchEvents = true
            }

            documentPages.forEach { documentPage ->
                val pageViewState = getPageViewState(documentPage.pageBounds)
                renderListener?.onPageGoingToBeRendered(documentPage)
                drawPageBackground(documentPage)
                renderListener?.onPageBackgroundRendered(documentPage)
                documentPage.draw(this@DocumentRenderView, canvas, pageViewState)
                renderListener?.onPageRendered(documentPage)
                currentPage = calculateCurrentPage(documentPage)
            }

            if (canShowPageCountBox) {
                this.drawPageNumber()
            }
        }
    }

    open fun drawDocumentIsEmptyMessage(canvas: Canvas) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (document.nightMode) {
                Color.WHITE
            } else {
                Color.BLACK
            }
            val txtToDraw = "Document Has No Pages!"
            textSize = resources.spToPx(12)
            val textWidth = measureText(txtToDraw)
            val textDrawX = canvas.width.div(2) - textWidth.div(2)
            val textBounds = Rect()
            this.getTextBounds(txtToDraw, 0, txtToDraw.length, textBounds)
            val textDrawY = canvas.height.div(2) - textBounds.height().div(2)
            canvas.drawText(txtToDraw, textDrawX, textDrawY.toFloat(), this)
        }
    }

    protected fun calculateCurrentPage(page: DocumentPage): Int {
        val pageViewState = getPageViewState(page.pageBounds)
        return if (pageViewState == ObjectViewState.VISIBLE && page.uniqueId == document.getPagesCount() - 1) {
            page.uniqueId + 1
        } else if (document.swipeVertical) {
            if (page.pageBounds.top < (height.div(2F))) {
                page.uniqueId + 1
            } else {
                currentPage
            }
        } else {
            if (page.pageBounds.left < (width.div(2F))) {
                page.uniqueId + 1
            } else {
                currentPage
            }
        }
    }

    protected fun Canvas.drawPageNumber() {
        val textToDisplay = "$currentPage / ${document.getPagesCount()}"
        val textBounds = Rect()
        pageNumberDisplayBoxPaint.getTextBounds(textToDisplay, 0, textToDisplay.count(), textBounds)
        val drawX = pageNumberDisplayBoxXAndYMargin
        val drawY = if (scroller == null || document.swipeVertical) {
            height - (pageNumberDisplayBoxXAndYMargin + textBounds.height() + _16Dp)
        } else {
            height - (scroller!!.scrollButtonHeight + scroller!!.marginUsed
                    + pageNumberDisplayBoxXAndYMargin + textBounds.height() + _16Dp)
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

    open fun Canvas.drawPageBackground(
        page: DocumentPage
    ) {
        val pageX: Float
        val pageY: Float
        val pageEnd: Float
        val pageBottom: Float
        val scaledPageStart: Float
        val rightMarginToSubtract: Float
        val pageWidthToDraw: Int
        val scaledFitPageWidth: Float
        val scaledPageY: Float
        val bottomMarginToSubtract: Float
        val pageHeightToDraw: Int
        val scaledFitPageHeight: Float
        if (document.swipeVertical) {
            // page leftMargin
            scaledPageStart =
                (toCurrentScale(document.getMaxPageWidth() - page.modifiedSize.width).div(2))
            pageX =
                contentDrawOffsetX + scaledPageStart + document.pageMargins.left
            rightMarginToSubtract =
                document.pageMargins.left + document.pageMargins.right
            pageWidthToDraw = page.modifiedSize.width
            scaledFitPageWidth = toCurrentScale(pageWidthToDraw)
            pageEnd = (pageX + scaledFitPageWidth) - rightMarginToSubtract
            // page Y

            pageY = contentDrawOffsetY + document.pageMargins.top + toCurrentScale(
                document.pageIndexes[page.uniqueId].y
            )
//                contentDrawOffsetY + document.pageMargins.top + scaledPageY
            // page bottom
            bottomMarginToSubtract =
                document.pageMargins.top + document.pageMargins.bottom

            pageHeightToDraw = page.modifiedSize.height
            scaledFitPageHeight = toCurrentScale(pageHeightToDraw)
            pageBottom = (pageY + scaledFitPageHeight) - bottomMarginToSubtract
        } else {
            //page leftMargin
            pageX =
                contentDrawOffsetX + toCurrentScale(document.pageIndexes[page.uniqueId].x) + document.pageMargins.left
            // pageY
            scaledPageY =
                toCurrentScale((document.getMaxPageHeight() - page.modifiedSize.height).div(2))
            pageY =
                (contentDrawOffsetY + scaledPageY) + document.pageMargins.top

            bottomMarginToSubtract =
                document.pageMargins.top + document.pageMargins.bottom

            pageHeightToDraw = page.modifiedSize.height
            scaledFitPageHeight = toCurrentScale(pageHeightToDraw)
            pageBottom = (pageY + scaledFitPageHeight) - bottomMarginToSubtract
            // pageEnd
//
            rightMarginToSubtract =
                document.pageMargins.left + document.pageMargins.right

            pageWidthToDraw = page.modifiedSize.width
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

    fun nightMode(night: Boolean) {
        document.nightMode = night
        if (document.nightMode) pagePaint.color = Color.BLACK else pagePaint.color =
            document.pageBackColor
        redraw()
    }

    fun isNightMode(): Boolean = document.nightMode

    open fun drawBackground(canvas: Canvas) {
        val bg = background
        if (bg == null) {
            canvas.drawColor(Color.LTGRAY)
        } else {
            bg.draw(canvas)
        }
    }

    override fun resetZoomWithAnimation() {
        if (canProcessTouchEvents) {
            zoomWithAnimation(minZoom)
        }
    }

    override fun zoomWithAnimation(centerX: Float, centerY: Float, scale: Float) {
        if (canProcessTouchEvents) {
            animationManager.startZoomAnimation(centerX, centerY, zoom, scale)
        }
    }

    override fun zoomWithAnimation(scale: Float) {
        if (canProcessTouchEvents) {
            animationManager.startZoomAnimation(
                (width.div(2)).toFloat(),
                (height.div(2)).toFloat(), zoom, scale
            )
        }
    }

    interface IdleStateCallback {
        @TestOnly
        fun renderViewState(idle: Boolean)
    }

    companion object {
        var MINIMUM_ZOOM = 1.0F
        var MAXIMUM_ZOOM = 5f

        /**
         * It is used to create LRU cache for storing blobs or other data.
         * it is calculated as follows.
         * `maxMemoryAvailableToProcessInKbs / CACHE_FACTOR`
         */
        var CACHE_FACTOR = 1
        val DEFAULT_MAX_SCALE = MAXIMUM_ZOOM
        val DEFAULT_MID_SCALE = MAXIMUM_ZOOM.div(2)
        val DEFAULT_MIN_SCALE = MINIMUM_ZOOM

        var REFRESH_RATE_IN_CASE_VIEW_BUSY = 10L // milliseconds
        var SCROLL_HANDLE_AND_PAGE_DISPLAY_BOX_HIDE_DELAY_MILLI_SECONDS = 1000L
    }

    class EventsIdentityHelper {
        private val bitSet = BitSet(3)
        fun feed(iMotionEventMarker: IMotionEventMarker?) {
            if (iMotionEventMarker is ScaleBeginEvent) {
                bitSet.set(0)
            }
            if (iMotionEventMarker is ScaleEndEvent) {
                bitSet.clear(0)
            }

            if (iMotionEventMarker is ScrollStartEvent) {
                bitSet.set(1)
            }
            if (iMotionEventMarker is ScrollEndEvent) {
                bitSet.clear(1)
            }

            if (iMotionEventMarker is FlingStartEvent) {
                bitSet.set(2)
            }

            if (iMotionEventMarker is FlingEndEvent) {
                bitSet.clear(2)
            }

            if (iMotionEventMarker is GenericMotionEvent && iMotionEventMarker.hasNoMotionEvent()
                    .not() && (iMotionEventMarker.motionEvent?.action == MotionEvent.ACTION_CANCEL
                        || iMotionEventMarker.motionEvent?.action == MotionEvent.ACTION_UP
                        || iMotionEventMarker.motionEvent?.action == MotionEvent.ACTION_POINTER_UP)
            ) {
                bitSet.clear(0)
                bitSet.clear(1)
            }
        }

        fun isScaling(): Boolean {
            return bitSet.get(0)
        }

        fun isScrolling(): Boolean {
            return bitSet.get(1)
        }

        fun isFlinging(): Boolean {
            return bitSet.get(2)
        }
    }

    enum class ViewState {
        BUSY, INITIALIZED
    }

    interface OnViewStateListener {
        fun onStateChanged(state: ViewState)
    }

    interface RenderListener {
        fun onPageGoingToBeRendered(page: DocumentPage)
        fun onPageBackgroundRendered(page: DocumentPage)
        fun onPageRendered(page: DocumentPage)
        fun onPageElementRendered(element: InteractiveElement)
    }

}