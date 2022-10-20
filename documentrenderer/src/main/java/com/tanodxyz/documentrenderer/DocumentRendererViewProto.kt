//package com.tanodxyz.documentrenderer
//
//import android.content.Context
//import android.graphics.*
//import android.util.AttributeSet
//import android.view.MotionEvent
//import android.view.View
//import com.tanodxyz.documentrenderer.document.Document
//import com.tanodxyz.documentrenderer.page.DocumentPage
//
//open class DocumentRendererViewProto @JvmOverloads constructor(
//    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
//) : View(context, attrs, defStyleAttr), MovementAndZoomHandler, View.OnTouchListener {
//
//    private lateinit var touchEventMgr: TouchEventsManager
//    private var bottomEdgeScroll: Boolean = true
//    private var topEdgeScroll: Boolean = true
//    private var startEdgeScroll = false
//    private var endEdgeScroll = false
//    var enableAntialiasing = true
//    var nightMode = false
//    var pageBackColor = Color.WHITE
//
//    // global page policies
//    lateinit var pagePaddings: Rect
//    lateinit var pageMargins: RectF
//
//    var pageFitPolicy: Document.PAGE_FIT_POLICY = Document.PAGE_FIT_POLICY.FIT_PAGE
//    var pageCorners: Float = 0.0F
//
//    protected val antialiasFilter =
//        PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
//    protected val pagePaint = Paint(Paint.ANTI_ALIAS_FLAG)
//
//
//    protected var currentOffsetX = 0F
//    protected var currentOffsetY = 0F
//
//
//    var swipeVertical = true
//    var currentPage = 0
//
//    private var contentHeight = 0f
//    private lateinit var dragPinchManager: DragPinchManager
//    private var documentPages = mutableListOf<DocumentPage>()
//
//    private var topVisible = false
//    private var bottomVisible = false
//    private lateinit var animationManager: AnimationManager
//
//    private var bottomOverscrollHeight = 0F
//    private var endOverscrollWidth: Float = 0F
//
//    init {
//        init()
//    }
//
//    private fun init() {
//        val fourDp = resources.dpToPx(36).toFloat()
//        pageMargins = RectF(fourDp, fourDp, fourDp, fourDp)
//        pageCorners = fourDp.toFloat()
//        setupPagePaintObject()
//        setOnTouchListener(this)
////        animationManager = AnimationManager(this)
//        dragPinchManager = DragPinchManager(this.context, this, animationManager)
//        touchEventMgr = TouchEventsManager(this.context)
//        touchEventMgr.registerListener(
//            object :TouchEventsManager.TouchEventsListener {
//                override fun getCurrentX(): Float {
//                    return currentOffsetX
//                }
//
//                override fun getCurrentY(): Float {
//                    return currentOffsetY
//                }
//
//                override fun onScrollStart(
//                    movementDirections: TouchEventsManager.MovementDirections,
//                    distanceX: Float,
//                    distanceY: Float,
//                    absoluteX: Float,
//                    absoluteY: Float
//                ) {
//                    println("")
//                    currentOffsetX = absoluteX
//                    currentOffsetY = absoluteY
//                }
//
//                override fun onScrollEnd() {
//                    println("Bako: end scroll")
//                }
//
//            }
//        )
//    }
//
//    private fun setupPagePaintObject() {
//        pagePaint.apply {
//            style = Paint.Style.FILL_AND_STROKE
//            color = if (nightMode) Color.BLACK else pageBackColor
//        }
//    }
//
//    override fun onDraw(canvas: Canvas?) {
//        super.onDraw(canvas)
//        if (isInEditMode) {
//            return
//        }
//        canvas?.apply {
//            drawFilter = if (enableAntialiasing && drawFilter == null) {
//                antialiasFilter
//            } else {
//                null
//            }
//            drawBackground(this)
//
//            contentHeight = 0f
//            for (i: Int in 0 until documentPages.count()) {
//                val page = documentPages[i]
//                drawPageBackground(page, contentHeight)
//                contentHeight += if (swipeVertical) {
//                    page.pageSize.height
//                } else {
//                    if (pageFitPolicy == Document.PAGE_FIT_POLICY.FIT_WIDTH) {
//                        resources.displayMetrics.widthPixels.toFloat()
//                    } else {
//                        page.pageSize.width
//                    }
//                }
//            }
//        }
//    }
//
//    open fun drawBackground(canvas: Canvas) {
//        val bg = background
//        if (bg == null) {
//            canvas.drawColor(if (nightMode) Color.BLACK else Color.LTGRAY)
//        } else {
//            bg.draw(canvas)
//        }
//    }
//
//    fun addDummyPages() {
//        for (i: Int in 0 until 5) {
//            documentPages.add(DocumentPage())
//        }
//        invalidate()
//    }
//
//    open fun Canvas.drawPageBackground(page: DocumentPage, totalHeightConsumed: Float) {
//
//        val pageStartX =
//            if (swipeVertical) currentOffsetX + pageMargins.left else currentOffsetX + pageMargins.left + totalHeightConsumed
//        val pageStartY =
//            if (swipeVertical) currentOffsetY + pageMargins.top + totalHeightConsumed else currentOffsetY + pageMargins.top
//        val pageEnd =
//            if (pageFitPolicy == Document.PAGE_FIT_POLICY.FIT_WIDTH) {
//                val screenWidthInPixels = resources.displayMetrics.widthPixels
//                if (swipeVertical) {
//                    (screenWidthInPixels - pageMargins.right).toFloat()
//                } else {
//                    (pageStartX + screenWidthInPixels) - pageMargins.right
//                }
//            } else if (pageFitPolicy == Document.PAGE_FIT_POLICY.FIT_PAGE) {
//                (pageStartX + page.pageSize.width) - pageMargins.right
//            } else {
//                (pageStartX + page.pageSize.width) - pageMargins.right
//            }
//        val pageBottom = (page.pageSize.height - pageMargins.bottom) + pageStartY
//        if (pageCorners > 0) {
//            drawRoundRect(
//                RectF(pageStartX, pageStartY, pageEnd, pageBottom),
//                pageCorners,
//                pageCorners,
//                pagePaint
//            )
//        } else {
//            drawRect(RectF(pageStartX, pageStartY, pageEnd, pageBottom), pagePaint)
//        }
//        page.pageBounds.apply {
//            left = pageStartX
//            top = pageStartY
//            right = pageEnd
//            bottom = pageBottom
//        }
//    }
//
//    fun getPageCount(): Int = documentPages.count()
//
//    override fun moveTo(offsetX: Float, offsetY: Float) {
//
//        if (swipeVertical) {
//            var deltaY = offsetY
//            val halfHeight = height / 2F
//            if (deltaY >= 0) {
//                topEdgeScroll = true
//                if (deltaY > halfHeight) {
//                    deltaY = halfHeight
//                }
//                currentOffsetY = deltaY
//
//            } else {
//                val contentBottom = contentHeight + deltaY
//                val previousContentBottom = contentHeight + currentOffsetY
//                if (previousContentBottom >= halfHeight) {
//                    if (contentBottom < halfHeight) {
//                        deltaY += (halfHeight - contentBottom)
//                    }
//                    if (contentBottom < (height + pageMargins.bottom)) {
//                        bottomOverscrollHeight += (currentOffsetY - deltaY)
//                        bottomEdgeScroll = true
//                    }
//                    currentOffsetY = deltaY
//                }
//            }
//
//        } else {
//
//            val halfWidth = width / 2f
//            val previousX = currentOffsetX
//            var deltaX = offsetX
//
//            var contentStartPrevious = previousX
//            var contentEndPrevious = previousX + contentHeight
//
//            val contentStart = deltaX
//            val contentEnd = deltaX + contentHeight
//
//            if (contentStart > 0) {
//                startEdgeScroll = true
//            }
//            if (contentEnd < width) {
//                endEdgeScroll = true
//                endOverscrollWidth += (width - contentEnd)
//            }
//
//            if (contentStart >= halfWidth) {
//                deltaX -= contentStart - halfWidth
//            }
//
//            if (contentEnd <= halfWidth) {
//                deltaX += (halfWidth - contentEnd)
//            }
//
//            currentOffsetX = deltaX
//        }
//        invalidate()
//    }
//
//    fun isContentYScrollable(): Boolean = contentHeight > height
//
//    fun isPageVisibleOnScreen(
//        pageBounds: RectF,
//        partially: Boolean = false,
//        accountForMargins: Boolean = true
//    ): Boolean {
//        val pageBottom = pageBounds.bottom
//        val pageTop = pageBounds.top
//        var pageMarginTop = 0F
//        var pageMarginBottom = 0F
//        if (accountForMargins) {
//            pageMarginTop = pageMargins.top
//            pageMarginBottom = pageMargins.bottom
//        }
//        val topIsVisible = (pageTop >= pageMarginTop && pageTop <= (height - pageMarginBottom))
//        val bottomIsVisible =
//            (pageBottom <= (height - pageMarginBottom) && (pageBottom >= pageMarginTop))
//        return if (partially) {
//            topIsVisible || bottomIsVisible
//        } else {
//            topIsVisible && bottomIsVisible
//        }
//    }
//
//    override fun moveRelative(deltaX: Float, deltaY: Float) {
//        moveTo((currentOffsetX + deltaX), currentOffsetY + deltaY)
//    }
//
//    override fun scrollTo(
//        deltaX: Float,
//        deltaY: Float,
//        scrollDirections: DragPinchManager.ScrollDirections
//    ) {
//        moveTo(deltaX, deltaY)
//    }
//
//    override fun moveToTopWithAnimation(startY: Float) {
//        animation
//    }
//
//    override fun getBottomBounds(): RectF {
//        return documentPages[documentPages.count() - 1].pageBounds
//    }
//
//    override fun getCurrentX(): Float {
//        return currentOffsetX
//    }
//
//    override fun getCurrentY(): Float {
//        return currentOffsetY
//    }
//
//    override fun zoomCenteredTo(zoom: Float, pivot: PointF) {
//
//    }
//
//    override fun setCurrentY(y: Float) {
//        this.currentOffsetY = y
//    }
//
//    override fun onScrollStart(direction: DragPinchManager.ScrollDirections) {
//        animationManager.stopAll()
//
//        if (topEdgeScroll) {
//            topEdgeScroll = false
//        }
//        if (bottomEdgeScroll) {
//            bottomEdgeScroll = false
//        }
//        if (startEdgeScroll) {
//            startEdgeScroll = false
//        }
//        if (endEdgeScroll) {
//            endOverscrollWidth = 0F
//            endEdgeScroll = false
//        }
//    }
//
//
//    override fun onScrollEnd() {
//        if (topEdgeScroll) {
////            animationManager.startYAnimation(currentOffsetY, 0F)
//        }
////        if (bottomEdgeScroll) {
////            val contentTop = (currentOffsetY + bottomOverscrollHeight)
////            val contentBottom = (currentOffsetY + bottomOverscrollHeight) + contentHeight
////            if (contentBottom >= (height - pageMargins.bottom)) {
////                val fl = contentBottom - (height - pageMargins.bottom)
////                bottomOverscrollHeight -= (fl)
////            }
////            if (contentTop > pageMargins.top) {
////                val fl = contentTop - pageMargins.top
////                bottomOverscrollHeight -= fl
////            }
////            animationManager.startYAnimation(
////                currentOffsetY,
////                (currentOffsetY + bottomOverscrollHeight)
////            )
////        }
////        if (startEdgeScroll) {
////            animationManager.startXAnimation(
////                currentOffsetX,
////                (0F)
////            )
////        }
////        if (endEdgeScroll) {
////            val finalContentEndAfterAnimation = contentHeight + currentOffsetX + endOverscrollWidth
////            if(finalContentEndAfterAnimation > (width - pageMargins.right)) {
////                endOverscrollWidth -= (finalContentEndAfterAnimation - width) + pageMargins.right
////            }
////            animationManager.startXAnimation(
////                currentOffsetX,
////                (currentOffsetX + endOverscrollWidth )
////            )
////        }
//    }
//
//    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//        return touchEventMgr.onTouchEvent(event)/*dragPinchManager.onTouchEvent(event)*/
//    }
//}