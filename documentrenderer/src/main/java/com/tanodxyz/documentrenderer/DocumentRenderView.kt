package com.tanodxyz.documentrenderer

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.tanodxyz.documentrenderer.document.Document
import com.tanodxyz.documentrenderer.page.DocumentPage

open class DocumentRenderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), View.OnTouchListener,
    TouchEventsManager.TouchEventsListener, AnimationManager.AnimationListener {

    private var ccx: Paint = Paint()


    var topAnimation: Boolean = false
    var bottomAnimation: Boolean = false
    lateinit var touchEventMgr: TouchEventsManager
    var enableAntialiasing = true
    var nightMode = false
    var pageBackColor = Color.WHITE

    lateinit var pagePaddings: Rect
    lateinit var pageMargins: RectF

    var pageFitPolicy: Document.PAGE_FIT_POLICY = Document.PAGE_FIT_POLICY.FIT_WIDTH
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

    var maxPageWidth = DocumentPage().pageSize.width //todo will be calculated at runtime later.
    var maxPageHeight = DocumentPage().pageSize.height //todo will be calculated at tuntime later.
    ///////////////////////////////////////////////////////////////////////////////////////////

    private val minZoom = DEFAULT_MIN_SCALE
    private val midZoom = DEFAULT_MID_SCALE
    private val maxZoom = DEFAULT_MAX_SCALE

    ///////////////////////////////////////////////////////////////////////////////////////////
    var documentPages = mutableListOf<DocumentPage>()

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
        println("p0i: from scroll")
        moveTo(absoluteX, absoluteY)
    }

    override fun onScrollEnd() {
//        animationManager.scroller?.abortAnimation()
//        val contentEnd = contentDrawOffsetY + contentHeight
//        val halfHeight = height / 2F
//        println("Bako: $topAnimation $bottomAnimation")
//        if (topAnimation && bottomAnimation) {
//            println("Bako: both executed")
//            if (contentEnd > halfHeight) {
//                topAnimation = true
//                bottomAnimation = false
//                onScrollEnd()
//            }
//        } else if (topAnimation) {
//            println("Bako: top executed only.")
//            animationManager.startYAnimation(
//                contentDrawOffsetY, (0F), null
//            )
//            topAnimation = false
//        } else if (bottomAnimation) {
//            println("Bako: bottom executed only")
//            if (contentHeight > height) {
//                println("Bako: inside bottom contentHeight > height")
//                val animationOffSetY =
//                    ((height - pageMargins.bottom) - contentEnd) + contentDrawOffsetY
//                animationManager.startYAnimation(contentDrawOffsetY, animationOffSetY, null)
//                bottomAnimation = false
//            } else {
//                println("Bako: inside bottom contentHeight < height")
//                bottomAnimation = false
//                topAnimation = true
//                onScrollEnd()
//            }
//        }
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
        val minY = -(getDocLen(zoom))
        val minX = -(toCurrentScale(maxPageWidth))
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

    open fun getDocLen(zoom: Float): Float {
        return contentHeight * zoom
    }

    override fun moveTo(absX: Float, absY: Float) {
        if (swipeVertical) {
//             Check X offset
//            var offsetX = absX
//            val scaledPageWidth: Float = toCurrentScale(maxPageWidth)
//            if (scaledPageWidth < width) {
//                offsetX = width / 2 - scaledPageWidth / 2
//            } else {
//                if (offsetX > 0) {
//                    offsetX = 0f
//                } else if (offsetX + scaledPageWidth < width) {
//                    offsetX = width - scaledPageWidth
//                }
//            }
//
//            // Check Y offset
//            var offsetY = absY
//            val contentHeight: Float = getDocLen(zoom)
//            if (contentHeight < height) { // whole document height visible on screen
//                offsetY = (height - contentHeight) / 2
//            } else {
//                if (offsetY > 0) { // top visible
//                    offsetY = 0f
//                } else if (offsetY + contentHeight < height) { // bottom visible
//                    offsetY = -contentHeight + height
//                }
//            }
//
//
//
//            contentDrawOffsetX = offsetX
//            contentDrawOffsetY = offsetY
            //////////////////////////////////////////////
            //y
            println("SANCHI: ------------------------------------------------------------------")
            println("SANCHI: before contentData |$contentDrawOffsetX| |$contentDrawOffsetY|")
            println("SANCHI: before absData     |$absX              | |$absY    ")


            val documentHeight = getDocLen(zoom)
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
            val isZoomed = zoom != MINIMUM_ZOOM
            println("p0i: before are absx = $absX $contentDrawOffsetX | $contentDrawOffsetY")
            var offsetX = absX
            val scaledPageWidth: Float = toCurrentScale(maxPageWidth)
            if (scaledPageWidth < width) {
                offsetX = width / 2 - scaledPageWidth / 2
            } else {
                if (offsetX > 0) {
                    offsetX = 0f
                } else if (offsetX + scaledPageWidth < width) {
                    offsetX = width - scaledPageWidth
                }
            }
            contentDrawOffsetX = offsetX
        } else {
            //todo do it for horizontal
        }
        println("zander: scrollOffsets are absX = $absX |$contentDrawOffsetX | ${this.context.resources.displayMetrics.widthPixels} | pageWith = ${documentPages[0].pageSize.width}")
        println("p0i: scrollOffsets are absX = $absX $contentDrawOffsetX | $contentDrawOffsetY")
        redraw()
    }

    override fun redraw() {
        invalidate()
    }


    fun addDummyPages() {
        for (i: Int in 0 until 100) {
            documentPages.add(DocumentPage())
        }
        invalidate()
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
                for (i: Int in documentPages.indices) {
                    val page = documentPages[i]
                    drawPageBackgroundNew(page, contentHeight)
                    drawText("Page No $i", page.pageBounds.left, page.pageBounds.top + 100, ccx)
                    drawCircle(page.pageBounds.right, page.pageBounds.top + 100, 30F, ccx)
                    contentHeight += page.pageSize.height
                }
            } else {
                //todo do it for horizontal page ...
            }

            drawLine(0F, (height / 2F), width.toFloat(), (height / 2F), ccx)
        }
    }

    open fun Canvas.drawPageBackgroundNew(page: DocumentPage, totalHeightConsumed: Float) {
        if(swipeVertical) {
            val pageX = contentDrawOffsetX + pageMargins.left
            val pageY = contentDrawOffsetY + pageMargins.top + (toCurrentScale(totalHeightConsumed))
            val pageEnd = (when (pageFitPolicy) {
                Document.PAGE_FIT_POLICY.FIT_WIDTH -> {
                    val screenWidthInPixels = resources.displayMetrics.widthPixels
                    ((contentDrawOffsetX + toCurrentScale(screenWidthInPixels.toFloat())) - pageMargins.right)
                }
                else -> {
                    (pageX + (toCurrentScale(page.pageSize.width))) - pageMargins.right
                }
            })
            val pageBottom = (pageY + (toCurrentScale(page.pageSize.height)) - pageMargins.bottom)
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

        println("TRK: pageOffsets are ${page.pageBounds}")

    }

    open fun Canvas.drawPageBackground(page: DocumentPage, totalHeightConsumed: Float) {
        if (swipeVertical) {

            val pageX = (contentDrawOffsetX + pageMargins.left)
            val pageY = (contentDrawOffsetY + pageMargins.top + totalHeightConsumed)
            val pageEnd = (when (pageFitPolicy) {
                Document.PAGE_FIT_POLICY.FIT_WIDTH -> {
                    val screenWidthInPixels = resources.displayMetrics.widthPixels
                    (screenWidthInPixels - pageMargins.right).toFloat()
                }
                else -> {
                    (pageX + page.pageSize.width) - pageMargins.right
                }
            })

            val pageBottom = (pageY + (page.pageSize.height - pageMargins.bottom))
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
            println("SANCHI: =========================================================")
            println("SANCHI: page bounds are ${page.pageBounds}")
            println("SANCHI: =========================================================")
        } else {
            //todo do it for horizontal page
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

    companion object {
        val DEFAULT_MAX_SCALE = 3.0f
        val DEFAULT_MID_SCALE = 1.75f
        val DEFAULT_MIN_SCALE = 1.0f
        var MAXIMUM_ZOOM = 10f
        var MINIMUM_ZOOM = 1.0F
    }
}