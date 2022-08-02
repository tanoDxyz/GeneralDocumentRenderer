package com.tanodxyz.documentrenderer

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.tanodxyz.documentrenderer.document.Document
import com.tanodxyz.documentrenderer.page.DocumentPage

open class DocumentRendererView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), MovementAndZoomHandler, View.OnTouchListener {
    var enableAntialiasing = true
    var nightMode = false
    var pageBackColor = Color.WHITE

    // global page policies
    lateinit var pagePaddings: Rect
    lateinit var pageMargins: Rect

    var pageFitPolicy: Document.PAGE_FIT_POLICY = Document.PAGE_FIT_POLICY.NONE
    var pageCorners: Float = 0.0F

    protected val antialiasFilter =
        PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    protected val pagePaint = Paint(Paint.ANTI_ALIAS_FLAG)


    protected var currentOffsetX = 0F
    protected var currentOffsetY = 0F

    var swipeHorizontal = false
    var swipeVertical = true
    var currentPage = 0

    private lateinit var dragPinchManager: DragPinchManager
    private var documentPages = mutableListOf<DocumentPage>()

    init {
        init()
    }

    private fun init() {
        val fourDp = resources.dpToPx(4)
        pageMargins = Rect(fourDp, fourDp, fourDp, fourDp)
        pageCorners = fourDp.toFloat()
        setupPagePaintObject()
        setOnTouchListener(this)
        dragPinchManager = DragPinchManager(this.context,this)
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
            var totalHeightConsumed = 0F
            documentPages.forEach { page ->
                drawPageBackground(page, totalHeightConsumed)
                totalHeightConsumed += page.pageSize.height
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
        for (i: Int in 0 until 100) {
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
    }

    override fun moveTo(offsetX: Float, offsetY: Float) {
        currentOffsetX = offsetX
        currentOffsetY = offsetY
        invalidate()
    }

    override fun getCurrentX(): Float {
        return currentOffsetX
    }

    override fun getCurrentY(): Float {
        return currentOffsetY
    }

    override fun zoomCenteredTo(zoom: Float, pivot: PointF) {

    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return dragPinchManager.onTouchEvent(event)
    }

}