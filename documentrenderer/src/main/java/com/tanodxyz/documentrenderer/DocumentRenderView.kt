package com.tanodxyz.documentrenderer

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.view.marginEnd
import com.tanodxyz.documentrenderer.document.Document
import com.tanodxyz.documentrenderer.page.DocumentPage
import kotlin.concurrent.thread

//todo we need logic for correctly defining page sizes.
//todo there must be a page with varialbe length
//todo an executor with lifecycle of View and at this instant set view to loading stuff.
//todo save index and data on view destruction
open class DocumentRenderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), View.OnTouchListener,
    TouchEventsManager.TouchEventsListener, AnimationManager.AnimationListener {

    private lateinit var document: Document
    private var ccx: Paint = Paint()


    var topAnimation: Boolean = false
    var bottomAnimation: Boolean = false
    lateinit var touchEventMgr: TouchEventsManager
    var enableAntialiasing = true
    var nightMode = false
    var pageBackColor = Color.WHITE


    val antialiasFilter =
        PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    val pagePaint = Paint(Paint.ANTI_ALIAS_FLAG)


    var contentDrawOffsetX = 0F
    var contentDrawOffsetY = 0F

    var currentPage = 0
    var contentLength = 0f


    var zoom = MINIMUM_ZOOM
    lateinit var animationManager: AnimationManager

    ///////////////////////////////////////////////////////////////////////////////////////////

    private val minZoom = DEFAULT_MIN_SCALE
    private val midZoom = DEFAULT_MID_SCALE
    private val maxZoom = DEFAULT_MAX_SCALE

    ///////////////////////////////////////////////////////////////////////////////////////////

    init {
        ccx.color = Color.RED
        ccx.style = Paint.Style.FILL_AND_STROKE
        ccx.textSize = 50F
        animationManager = AnimationManager(this.context, this)
        // todo these values will be parsed from attributes


        pagePaint.apply {
            style = Paint.Style.FILL_AND_STROKE
            color = if (nightMode) Color.BLACK else pageBackColor
        }

        setOnTouchListener(this)
        touchEventMgr = TouchEventsManager(this.context)
        touchEventMgr.registerListener(this)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (isInEditMode) {
            return
        }
        //todo check how many pages are visible ......
        thread(start = true) {
            while (true) {
                Thread.sleep(2000)
                println(
                    "Bakko: visible pages are ${
                        document.getDocumentPages().filter { it.isVisible(false) }.count()
                    }"
                )
            }
        }
        animationManager.stopAll()
        document.recalculatePageSizes(Size(w, h))
        if (document.swipeVertical) {
            // whenever size changes set X
            val scaledPageWidth: Float = toCurrentScale(document.getMaxPageWidth())
            if (scaledPageWidth < width) {
                contentDrawOffsetX = width / 2 - scaledPageWidth / 2
            }
            // whenever size changes set Y
            val documentHeight = getDocLen(zoom)
            if (documentHeight < height) {
                contentDrawOffsetY = (height - documentHeight) / 2
            }

        } else {
            val scaledPageHeight = toCurrentScale(document.getMaxPageHeight().toFloat())
            if (scaledPageHeight < height) {
                contentDrawOffsetY = height / 2 - scaledPageHeight / 2
            }
            val contentWidth: Float = getDocLen(zoom)
            if (contentWidth < width) { // whole document width visible on screen
                contentDrawOffsetX = (width - contentWidth) / 2
            }
        }

    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return touchEventMgr.onTouchEvent(event)
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


    fun zoomTo(zoom: Float) {
        this.zoom = zoom
    }

    override fun onScrollStart(
        movementDirections: TouchEventsManager.MovementDirections?,
        distanceX: Float,
        distanceY: Float,
        absoluteX: Float,
        absoluteY: Float
    ) {
//        if (pdfView.isZooming() || pdfView.isSwipeEnabled()) {
//            pdfView.moveRelativeTo(-distanceX, -distanceY)
//        }
//        if (!scaling || pdfView.doRenderDuringScale()) {
//            pdfView.loadPageByOffset()
//        }
        moveTo(absoluteX, absoluteY)
    }

    override fun onScrollEnd() {
//        pdfView.loadPages()
//        hideHandle()
        if (!animationManager.isFlinging()) {
            performPageSnap()
        }
    }

    override fun performPageSnap() {
        if (!document.pageSnap || document.haveNoPages()) {
            return
        }
        //todo on background

//        findFocusPage(contentDrawOffsetX,contentDrawOffsetY)
//        val centerPage: Int = findFocusPage(currentXOffset, currentYOffset)
//        val edge: SnapEdge = findSnapEdge(centerPage)
//        if (edge === SnapEdge.NONE) {
//            return
//        }
//
//        val offset: Float = snapOffsetForPage(centerPage, edge)
//        if (swipeVertical) {
//            animationManager.startYAnimation(currentYOffset, -offset)
//        } else {
//            animationManager.startXAnimation(currentXOffset, -offset)
//        }
    }

    open fun findFocusedPage(xOffset: Float, yOffset: Float): Int {
        var pageOffset = -1
        val currOffset: Float = if (document.swipeVertical) yOffset else xOffset
        val length: Float =
            if (document.swipeVertical) height.toFloat() else width.toFloat()
        // make sure first and last page can be found
        // make sure first and last page can be found
        if (currOffset > -1) {
            pageOffset = 0
        } else if (currOffset < -getDocLen(zoom) + length + 1) {
            pageOffset = document.getPagesCount() - 1
        }
        val center = currOffset - length / 2f
        // find page in center so
        return -1;
    }
//    open fun findFocusPage(xOffset: Float, yOffset: Float): Int {
//        val currOffset = if (document.swipeVertical) yOffset else xOffset
//        val length = if (document.swipeVertical) height.toFloat() else width.toFloat()
//        // make sure first and last page can be found
//        if (currOffset > -1) {
//            return 0
//        } else if (currOffset < -getDocLen(zoom) + length + 1) {
//            return document.getPagesCount() - 1
//        }
//        // else find page in center
//        val center = currOffset - length / 2f
//        return getPageAtOffset(-center, zoom)
//    }

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
        var minY = 0F
        var minX = 0F
        if (document.swipeVertical) {
            minY = -(getRenderedDocLen(zoom))
            minX = -(toCurrentScale(document.getMaxPageWidth()))
        } else {
            minX = -(getDocLen(getCurrentZoom()) /*- width*/)
            minY = -(toCurrentScale(document.getMaxPageHeight()) /*- height*/)
        }
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

    override fun onStopFling() {
        animationManager.stopFling()
    }

    override fun zoomCenteredRelativeTo(dr: Float, pointF: PointF) {
        zoomCenteredTo(zoom * dr, pointF)
    }

    open fun toCurrentScale(size: Number): Float {
        return size.toFloat() * zoom
    }

    open fun Number.shouldSubtractExtraBottomMargin(startPosition: Float): Boolean {
        return (toCurrentScale(this.toFloat()) + startPosition) >= height
    }

    open fun Number.shouldSubtractExtraRightMargin(startPosition: Float): Boolean {
        return (toCurrentScale(this.toFloat()) + startPosition) >= width
    }

    open fun getRenderedDocLen(zoom: Float): Float {
        return contentLength * zoom
    }

    fun getDocLen(zoom: Float): Float {
        return document.getContentHeight() * zoom
    }

    override fun moveTo(absX: Float, absY: Float) {
        if (document.swipeVertical) {
            val documentHeight = getRenderedDocLen(zoom)
            contentDrawOffsetY = if (documentHeight < height) {
                (height - documentHeight) / 2
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
                offsetX = width / 2 - scaledPageWidth / 2
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
                offsetY = height / 2 - scaledPageHeight / 2
            } else {
                if (offsetY > 0) {
                    offsetY = 0f
                } else if (offsetY + scaledPageHeight < height) {
                    offsetY = height - scaledPageHeight
                }
            }

            // Check X offset

            // Check X offset
            var offsetX = absX
            val contentWidth: Float = getDocLen(zoom)
            if (contentWidth < width) { // whole document width visible on screen
                offsetX = (width - contentWidth) / 2
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
        redraw()
    }

    override fun redraw() {
        invalidate()
    }


    fun loadDocument(document: Document) {
        this.document = document
        this.document.setup(Size(width, height))
        redraw()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (isInEditMode) {
            return
        }
        canvas?.apply {
            drawFilter = if (enableAntialiasing && drawFilter == null) {
                antialiasFilter
            } else {
                null
            }
            drawBackground(this)

            contentLength = 0F
            val documentPages = document.getDocumentPages()
            for (i: Int in documentPages.indices) {
                val page = documentPages[i]
                drawPageBackground( page, contentLength)
                drawText("Page No $i", page.pageBounds.left, page.pageBounds.top + 100, ccx)
                drawCircle(page.pageBounds.right, page.pageBounds.top + 100, 30F, ccx)
                contentLength += if (document.swipeVertical) {
                    page.size.height
                } else {
                    page.size.width
                }
            }
            // centered line // red one.
            drawLine(0F, (height / 2F), width.toFloat(), (height / 2F), ccx)
        }
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
        var shouldSubtractExtraRightMargin = false
        var rightMarginToSubtract = 0F
        var pageWidthToDraw = 0
        var scaledFitPageWidth = 0F
        var scaledPageY = 0F
        var shouldSubtractExtraBottomMargin = false
        var bottomMarginToSubtract = 0F
        var pageHeightToDraw = 0
        var scaledFitPageHeight = 0F
        if (document.swipeVertical) {
            // page x
            scaledPageStart =
                (toCurrentScale(document.getMaxPageWidth() - page.size.width) / 2)
            pageX =
                contentDrawOffsetX + scaledPageStart + document.pageMargins.left;
            // pageEnd
//            shouldSubtractExtraRightMargin =
//                page.size.width.shouldSubtractExtraRightMargin(pageX)
            rightMarginToSubtract = /*if (shouldSubtractExtraRightMargin) {*/
                document.pageMargins.left + document.pageMargins.right
//            } else {
//                document.pageMargins.right
//            }
            pageWidthToDraw = page.size.width
            scaledFitPageWidth = toCurrentScale(pageWidthToDraw)
            pageEnd = (pageX + scaledFitPageWidth) - rightMarginToSubtract
            // page Y
            scaledPageY = (toCurrentScale(
                totalPagesDrawnLength
            ))
            pageY =
                contentDrawOffsetY + document.pageMargins.top + scaledPageY
            // page bottom
//            shouldSubtractExtraBottomMargin =
//                page.size.height.shouldSubtractExtraBottomMargin(pageY)
            bottomMarginToSubtract = /*if (shouldSubtractExtraBottomMargin) {*/
                document.pageMargins.top + document.pageMargins.bottom
           /* } else {
                document.pageMargins.bottom
            }*/
            pageHeightToDraw = page.size.height
            scaledFitPageHeight = toCurrentScale(pageHeightToDraw)
            pageBottom = (pageY + scaledFitPageHeight) - bottomMarginToSubtract

        } else {
            //page x
            scaledPageStart = (toCurrentScale(totalPagesDrawnLength))
            pageX =
                contentDrawOffsetX + scaledPageStart + document.pageMargins.left
            // pageY
            scaledPageY = toCurrentScale((document.getMaxPageHeight() - page.size.height) / 2)
            pageY =
                (contentDrawOffsetY + scaledPageY) + document.pageMargins.top
            //pageBottom
//            shouldSubtractExtraBottomMargin =
//                page.size.height.shouldSubtractExtraBottomMargin(pageY)
            bottomMarginToSubtract = /*if (shouldSubtractExtraBottomMargin) {*/
                document.pageMargins.top + document.pageMargins.bottom
            /*} else {
                document.pageMargins.bottom
            }*/
            pageHeightToDraw = page.size.height
            scaledFitPageHeight = toCurrentScale(pageHeightToDraw)
            pageBottom = (pageY + scaledFitPageHeight) - bottomMarginToSubtract
            // pageEnd
//            shouldSubtractExtraRightMargin =
//                page.size.width.shouldSubtractExtraRightMargin(pageX)
            rightMarginToSubtract = /*if (shouldSubtractExtraRightMargin) {*/
                document.pageMargins.left + document.pageMargins.right
            /*} else {
                document.pageMargins.right
            }*/
            pageWidthToDraw = page.size.width
            scaledFitPageWidth = toCurrentScale(pageWidthToDraw)
            pageEnd = (pageX + scaledFitPageWidth) - rightMarginToSubtract
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
        page.pageBounds.apply {
            left = pageX
            top = pageY
            right = pageEnd
            bottom = pageBottom
        }
        val pageVisibleOnScreen =
            DocumentPage.isPageVisibleOnScreen(
                document.swipeVertical,
                page.pageBounds,
                viewSize = Size(width, height)
            )
        page.pageVisibility = pageVisibleOnScreen
    }

    open fun drawBackground(canvas: Canvas) {
        val bg = background
        if (bg == null) {
            canvas.drawColor(if (nightMode) Color.BLACK else Color.LTGRAY)
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
            (width / 2).toFloat(),
            (height / 2).toFloat(), zoom, scale
        )
    }

    companion object {
        val DEFAULT_MAX_SCALE = 3.0f // todo change this
        val DEFAULT_MID_SCALE = 1.75f //todo and this to change the double tap zoom levels
        val DEFAULT_MIN_SCALE = 1.0f
        var MAXIMUM_ZOOM = 10f
        var MINIMUM_ZOOM = 1.0F
    }
}