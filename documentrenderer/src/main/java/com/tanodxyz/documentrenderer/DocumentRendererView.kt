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

    var swipeHorizontal = false
    var swipeVertical = true
    var currentPage = 0

    private var contentHeight = 0f
    private lateinit var dragPinchManager: DragPinchManager
    private var documentPages = mutableListOf<DocumentPage>()

    private var topVisible = false
    private var bottomVisible = false

    init {
        init()
    }

    private fun init() {
        val fourDp = resources.dpToPx(4).toFloat()
        pageMargins = RectF(fourDp, fourDp, fourDp, fourDp)
        pageCorners = fourDp.toFloat()
        setupPagePaintObject()
        setOnTouchListener(this)
        dragPinchManager = DragPinchManager(this.context, this)
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
                if (i == 0) {
                    topVisible = isPageVisibleOnScreen(page.pageBounds)
                }
                if (i == documentPages.count() - 1) {
                    bottomVisible = isPageVisibleOnScreen(page.pageBounds)
                }
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
        for (i: Int in 0 until 8) {
            documentPages.add(DocumentPage())
        }
        invalidate()
    }

    open fun Canvas.drawPageBackground(page: DocumentPage, totalHeightConsumed: Float) {
        val pageStartX = currentOffsetX + pageMargins.left
        val pageStartY = currentOffsetY + pageMargins.top + totalHeightConsumed
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
        if (isContentYScrollable()) {
            val pb = documentPages[documentPages.count() - 1].pageBounds
            val lastPageBounds = RectF(pb.left, pb.top, pb.right, pb.bottom)
            val absCurrentY = offsetY.absoluteValue
            val absRecentY = currentOffsetY.absoluteValue
            val delta = absCurrentY - absRecentY
            var scrollPosY = offsetY
            if (offsetY > 0) {
                scrollPosY = 0F
            } else if (offsetY < 0) {
                lastPageBounds.top -= delta
                lastPageBounds.bottom -= delta
                if (isPageVisibleOnScreen(lastPageBounds)) {
                    if (lastPageBounds.bottom < height) {
                        scrollPosY += (height - (lastPageBounds.bottom + pageMargins.bottom))
                    }
                }
            }
            currentOffsetY = scrollPosY
            currentOffsetX = offsetX
            invalidate()
        }
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
        scrollDirection: DragPinchManager.ScrollDirection
    ) {
        moveTo(deltaX, deltaY)
    }

    override fun getCurrentX(): Float {
        return currentOffsetX
    }

    override fun getCurrentY(): Float {
        return currentOffsetY
    }

    override fun zoomCenteredTo(zoom: Float, pivot: PointF) {

    }

    override fun onScrollStart(direction: DragPinchManager.ScrollDirection) {
    }


    override fun onScrollEnd() {
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return dragPinchManager.onTouchEvent(event)
    }

}