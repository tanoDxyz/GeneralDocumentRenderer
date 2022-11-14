package com.tanodxyz.documentrenderer

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.tanodxyz.documentrenderer.document.Document
import com.tanodxyz.documentrenderer.document.Size
import com.tanodxyz.documentrenderer.page.DocumentPage

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

    lateinit var pagePaddings: Rect
    lateinit var pageMargins: RectF

    var pageCorners: Float = 0.0F

    val antialiasFilter =
        PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    val pagePaint = Paint(Paint.ANTI_ALIAS_FLAG)


    var contentDrawOffsetX = 0F
    var contentDrawOffsetY = 0F

    var swipeVertical = true
    var currentPage = 0
    var contentHeight = 0f
    var contentWidth = 0f


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
        val defaultPageMargins = resources.dpToPx(12)
        pageMargins =
            RectF(defaultPageMargins, defaultPageMargins, defaultPageMargins, defaultPageMargins)
        pageCorners = resources.dpToPx(8)

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
        animationManager.stopAll()
        document.recalculatePageSizes(Size(w.toFloat(), h.toFloat()))

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
        println("p0i: from zoom center")
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
        moveTo(absoluteX, absoluteY)
    }

    override fun onScrollEnd() {
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
        val minY = -(getRenderedDocLen(zoom))
        val minX = -(toCurrentScale(document.getMaxPageWidth()))
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
        zoomCenteredTo(zoom * dr, pointF!!)
    }

    open fun toCurrentScale(size: Float): Float {
        return size * zoom
    }

    open fun getRenderedDocLen(zoom: Float): Float {
        return contentHeight * zoom
    }

    fun getDocLen(zoom: Float): Float {
        return document.getContentHeight() * zoom
    }

    override fun moveTo(absX: Float, absY: Float) {
        if (swipeVertical) {
            val documentHeight = getRenderedDocLen(zoom)
            contentDrawOffsetY = if (documentHeight < height) {
                (height - documentHeight) / 2
            } else {
                val contentEnd = absY + documentHeight + pageMargins.bottom
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
            //todo do it for horizontal
        }
        redraw()
    }

    override fun redraw() {
        invalidate()
    }


    fun loadDocument(document: Document) {
        this.document = document
        this.document.setup(Size(width.toFloat(), height.toFloat()))
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
            if (swipeVertical) {
                contentHeight = 0F
                val documentPages = document.getDocumentPages()
                for (i: Int in documentPages.indices) {
                    val page = documentPages[i]
                    drawPageBackgroundNew(page, contentHeight)
                    drawText("Page No $i", page.pageBounds.left, page.pageBounds.top + 100, ccx)
                    drawCircle(page.pageBounds.right, page.pageBounds.top + 100, 30F, ccx)
                    contentHeight += page.size.height
                }
            } else {
                //todo do it for horizontal page ...
            }

            drawLine(0F, (height / 2F), width.toFloat(), (height / 2F), ccx)
        }
    }

    open fun Canvas.drawPageBackgroundNew(page: DocumentPage, totalHeightConsumed: Float) {
        if (swipeVertical) {
            // set page width in a way
            var pageX = 0F
            var pageEnd = 0F
            pageX =
                contentDrawOffsetX + toCurrentScale(document.getMaxPageWidth() - page.size.width) / 2;
            pageEnd =
                (pageX + (toCurrentScale(page.size.width)))
            val addSideWiseMargins = (page.size.width / width.toFloat()) >= 0.95F || zoom != minZoom
            if (addSideWiseMargins) {
                pageX += toCurrentScale(pageMargins.left)
                pageEnd -= toCurrentScale(pageMargins.right)
            }
            val pageY = contentDrawOffsetY + pageMargins.top + (toCurrentScale(totalHeightConsumed))
            val pageBottom = (pageY + (toCurrentScale(page.size.height)) - pageMargins.bottom)

            if (pageCorners > 0) {
                drawRoundRect(
                    RectF(pageX, pageY, pageEnd, pageBottom),
                    pageCorners,
                    pageCorners,
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
        } else {
            // do it for horizontal
        }


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