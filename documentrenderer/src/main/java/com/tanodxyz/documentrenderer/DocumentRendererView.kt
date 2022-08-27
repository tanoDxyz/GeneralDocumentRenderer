package com.tanodxyz.documentrenderer

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.tanodxyz.documentrenderer.document.Document
import com.tanodxyz.documentrenderer.page.DocumentPage
import kotlin.math.absoluteValue

open class DocumentRendererView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), MovementAndZoomHandler, View.OnTouchListener {
    private var bottomEdgeScroll: Boolean = true
    private var topEdgeScroll: Boolean = true
    private var startEdgeScroll = false
    private var endEdgeScroll = false
    var enableAntialiasing = true
    var nightMode = false
    var pageBackColor = Color.WHITE

    // global page policies
    lateinit var pagePaddings: Rect
    lateinit var pageMargins: RectF

    var pageFitPolicy: Document.PAGE_FIT_POLICY = Document.PAGE_FIT_POLICY.FIT_WIDTH
    var pageCorners: Float = 0.0F

    protected val antialiasFilter =
        PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    protected val pagePaint = Paint(Paint.ANTI_ALIAS_FLAG)


    protected var currentOffsetX = 0F
    protected var currentOffsetY = 0F


    var swipeVertical = true
    var currentPage = 0

    private var contentHeight = 0f
    private lateinit var dragPinchManager: DragPinchManager
    private var documentPages = mutableListOf<DocumentPage>()

    private var topVisible = false
    private var bottomVisible = false
    private lateinit var animationManager: AnimationManager

    private var bottomOverscrollHeight = 0F

    init {
        init()
    }

    private fun init() {
        val fourDp = resources.dpToPx(12).toFloat()
        pageMargins = RectF(fourDp, fourDp, fourDp, fourDp)
        pageCorners = fourDp.toFloat()
        setupPagePaintObject()
        setOnTouchListener(this)
        animationManager = AnimationManager(this)
        dragPinchManager = DragPinchManager(this.context, this, animationManager)
    }

    private fun setupPagePaintObject() {
        pagePaint.apply {
            style = Paint.Style.FILL_AND_STROKE
            color = if (nightMode) Color.BLACK else pageBackColor
        }
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

            contentHeight = 0f
            for (i: Int in 0 until documentPages.count()) {
                val page = documentPages[i]
                drawPageBackground(page, contentHeight)
                contentHeight += page.pageSize.height
            }
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

    fun addDummyPages() {
        for (i: Int in 0 until 3) {
            documentPages.add(DocumentPage())
        }
        invalidate()
    }

    open fun Canvas.drawPageBackground(page: DocumentPage, totalHeightConsumed: Float) {

        val pageStartX =
            if (swipeVertical) currentOffsetX + pageMargins.left else currentOffsetX + pageMargins.left + totalHeightConsumed
        val pageStartY =
            if (swipeVertical) currentOffsetY + pageMargins.top + totalHeightConsumed else currentOffsetY + pageMargins.top
        val pageEnd =
            if (pageFitPolicy == Document.PAGE_FIT_POLICY.FIT_WIDTH) {
                val screenWidthInPixels = resources.displayMetrics.widthPixels
                (screenWidthInPixels - pageMargins.right).toFloat()
            } else if (pageFitPolicy == Document.PAGE_FIT_POLICY.FIT_PAGE) {
                //todo fit the page to the screen amigo
                (page.pageSize.width - pageMargins.right)
            } else {
                (page.pageSize.width - pageMargins.right)
            }
        val pageBottom = (page.pageSize.height - pageMargins.bottom) + pageStartY
        if (pageCorners > 0) {
            drawRoundRect(
                RectF(pageStartX, pageStartY, pageEnd, pageBottom),
                pageCorners,
                pageCorners,
                pagePaint
            )
        } else {
            drawRect(RectF(pageStartX, pageStartY, pageEnd, pageBottom), pagePaint)
        }
        page.pageBounds.apply {
            left = pageStartX
            top = pageStartY
            right = pageEnd
            bottom = pageBottom
        }
    }

    fun getPageCount(): Int = documentPages.count()

    override fun moveTo(offsetX: Float, offsetY: Float) {

        if (swipeVertical) {
            var deltaY = offsetY
            val halfHeight = height / 2F
            if (deltaY >= 0) {
                topEdgeScroll = true
                if (deltaY > halfHeight) {
                    deltaY = halfHeight
                }
                currentOffsetY = deltaY

            } else {
                val contentBottom = contentHeight + deltaY
                val previousContentBottom = contentHeight + currentOffsetY
                if (previousContentBottom >= halfHeight) {
                    if (contentBottom < halfHeight) {
                        deltaY += (halfHeight - contentBottom)
                    }
                    if (contentBottom < (height + pageMargins.bottom)) {
                        bottomOverscrollHeight += (currentOffsetY - deltaY)
                        bottomEdgeScroll = true
                    }
                    currentOffsetY = deltaY
                }
//                if(contentBottom < height && contentHeight >= halfHeight) {
//                    bottomEdgeScroll = true
//                }
//                if(contentBottom < halfHeight && previousContentBottom > halfHeight) {
//                    deltaY += (halfHeight - contentBottom)
//                }
//                println("Bakko: half height $halfHeight $contentBottom $deltaY")

            }

        }
//        if (swipeVertical) {
//            var finalPosY = offsetY
//            if (offsetY > 0) {
//                if (enabledOverScrollTop) {
//                    topEdgeScroll = true
//                    val halfHeight = (height / 2F)
//                    finalPosY = if (offsetY > halfHeight) {
//                        halfHeight
//                    } else {
//                        offsetY
//                    }
//                } else {
//                    currentOffsetY = 0F
//                }
//            } else if (offsetY < 0) {
//                val pb = documentPages[documentPages.count() - 1].pageBounds
//                val lastPageBounds = RectF(pb.left, pb.top, pb.right, pb.bottom)
//                val absCurrentY = offsetY.absoluteValue
//                val absRecentY = currentOffsetY.absoluteValue
//                val delta = absCurrentY - absRecentY
//                lastPageBounds.top -= delta
//                lastPageBounds.bottom -= delta
//                if (isPageVisibleOnScreen(lastPageBounds)) {
//                    if (lastPageBounds.bottom < height && contentHeight > height) {
//                        if (enabledOverScrollBottom) {
//                            val halfHeight = (height / 2F)
//                            bottomEdgeScroll = true
//                            if (pb.bottom < halfHeight) {
//                                finalPosY += halfHeight - (pb.bottom + pageMargins.bottom)
//                            }
//                        } else {
//                            finalPosY += (height - (lastPageBounds.bottom + pageMargins.bottom))
//                        }
//                    } else {
//                        finalPosY = currentOffsetY
//                    }
//                }
//            }
//            currentOffsetY = finalPosY
//        } else {
//            // horizontal scroll so.
//            val halfWidth = width / 2F
//            var finalPosX = offsetX
//            if (offsetX > 0) {
//                if (enabledOverScrollStart) {
//                    startEdgeScroll = true
//                    finalPosX = if (offsetX > halfWidth) {
//                        halfWidth
//                    } else {
//                        offsetY
//                    }
//                } else {
//                    currentOffsetX = 0F
//                }
//            }
//            currentOffsetX = finalPosX
//
//        }
        invalidate()
    }

    fun isContentYScrollable(): Boolean = contentHeight > height

    fun isPageVisibleOnScreen(
        pageBounds: RectF,
        partially: Boolean = false,
        accountForMargins: Boolean = true
    ): Boolean {
        val pageBottom = pageBounds.bottom
        val pageTop = pageBounds.top
        var pageMarginTop = 0F
        var pageMarginBottom = 0F
        if (accountForMargins) {
            pageMarginTop = pageMargins.top
            pageMarginBottom = pageMargins.bottom
        }
        val topIsVisible = (pageTop >= pageMarginTop && pageTop <= (height - pageMarginBottom))
        val bottomIsVisible =
            (pageBottom <= (height - pageMarginBottom) && (pageBottom >= pageMarginTop))
        return if (partially) {
            topIsVisible || bottomIsVisible
        } else {
            topIsVisible && bottomIsVisible
        }
    }

    override fun moveRelative(deltaX: Float, deltaY: Float) {
        moveTo((currentOffsetX + deltaX), currentOffsetY + deltaY)
    }

    override fun scrollTo(
        deltaX: Float,
        deltaY: Float,
        scrollDirections: DragPinchManager.ScrollDirections
    ) {
        moveTo(deltaX, deltaY)
    }

    override fun moveToTopWithAnimation(startY: Float) {
        animation
    }

    override fun getBottomBounds(): RectF {
        return documentPages[documentPages.count() - 1].pageBounds
    }

    override fun getCurrentX(): Float {
        return currentOffsetX
    }

    override fun getCurrentY(): Float {
        return currentOffsetY
    }

    override fun zoomCenteredTo(zoom: Float, pivot: PointF) {

    }

    override fun setCurrentY(y: Float) {
        this.currentOffsetY = y
    }

    override fun onScrollStart(direction: DragPinchManager.ScrollDirections) {
        println("IOIP: scroll start ....")
        animationManager.stopAll()
        if (topEdgeScroll) {
            topEdgeScroll = false
        }
        if (bottomEdgeScroll) {
            bottomEdgeScroll = false
        }
    }


    override fun onScrollEnd() {
        if (topEdgeScroll) {
            animationManager.startYAnimation(currentOffsetY, 0F)
        }
        if (bottomEdgeScroll) {
            val contentTop = (currentOffsetY + bottomOverscrollHeight)
            val contentBottom = (currentOffsetY + bottomOverscrollHeight) + contentHeight
            if (contentBottom >= (height - pageMargins.bottom)) {
                val fl = contentBottom - (height - pageMargins.bottom)
                bottomOverscrollHeight -= (fl)
            }
            if(contentTop > pageMargins.top) {
                val fl = contentTop - pageMargins.top
                bottomOverscrollHeight -=fl
            }
            println("IOIP: contentTop = $contentTop contentBottom = $contentBottom contentHeight = $contentHeight currY = $currentOffsetY")
            animationManager.startYAnimation(
                currentOffsetY,
                (currentOffsetY + bottomOverscrollHeight)
            )
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return dragPinchManager.onTouchEvent(event)
    }
}