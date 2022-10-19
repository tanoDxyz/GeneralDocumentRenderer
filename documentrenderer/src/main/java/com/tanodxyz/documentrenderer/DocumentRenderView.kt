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
    lateinit var animationManager: AnimationManager
    var documentPages = mutableListOf<DocumentPage>()

    init {
        animationManager = AnimationManager(this)
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
    }

    override fun onScrollStart(
        movementDirections: TouchEventsManager.MovementDirections?,
        distanceX: Float,
        distanceY: Float,
        absoluteX: Float,
        absoluteY: Float
    ) {
        moveTo(distanceX, distanceY, movementDirections)
    }

    override fun onScrollEnd() {
        if (topAnimation) {
            animationManager.startYAnimation(
                contentDrawOffsetY, (0F), null
            )
            topAnimation = false
        }
        if (bottomAnimation) {
            val contentEnd = contentDrawOffsetY + contentHeight
            val animationOffSetY = ((height - pageMargins.bottom) - contentEnd) + contentDrawOffsetY
            animationManager.startYAnimation(contentDrawOffsetY, animationOffSetY, null)
            bottomAnimation = false
        }
    }

    override fun moveTo(
        offsetX: Float,
        offsetY: Float,
        movementDirections: TouchEventsManager.MovementDirections?
    ) {
        if (swipeVertical) {
            val halfHeight = height / 2F
            val contentStart = contentDrawOffsetY + offsetY
            val contentEnd = contentDrawOffsetY + offsetY + contentHeight
            if (movementDirections == null) {
                contentDrawOffsetY = offsetY
            } else {
                if (movementDirections.bottom) {
                    contentDrawOffsetY += if (contentStart <= halfHeight) {
                        offsetY
                    } else {
                        val contentStartPrevious = contentDrawOffsetY
                        halfHeight - contentStartPrevious
                    }
                    if (contentStart > (pageMargins.top) && contentStart <= halfHeight) {
                        topAnimation = true
                    }
                }
                if (movementDirections.top) {
                    val cdofy = contentDrawOffsetY
                    contentDrawOffsetY += if (contentEnd >= halfHeight) {
                        offsetY
                    } else {
                        val extraScrollHeight = halfHeight - contentEnd
                        (offsetY + extraScrollHeight)
                    }
                    if (contentEnd < (height.toFloat() + pageMargins.bottom) && contentEnd >= halfHeight) {
                        bottomAnimation = true
                    }
                }
            }
            invalidate()
        } else {
            //todo horizontal
        }
    }

    fun addDummyPages() {
        for (i: Int in 0 until 5) {
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
                    drawPageBackground(page, contentHeight)
                    contentHeight += page.pageSize.height
                }
            } else {
                //todo do it for horizontal page ...
            }
        }
    }

    open fun Canvas.drawPageBackground(page: DocumentPage, totalHeightConsumed: Float) {
        if (swipeVertical) {
            val pageX = contentDrawOffsetX + pageMargins.left
            val pageY = contentDrawOffsetY + pageMargins.top + totalHeightConsumed
            val pageEnd = when (pageFitPolicy) {
                Document.PAGE_FIT_POLICY.FIT_WIDTH -> {
                    val screenWidthInPixels = resources.displayMetrics.widthPixels
                    (screenWidthInPixels - pageMargins.right).toFloat()
                }
                else -> {
                    (pageX + page.pageSize.width) - pageMargins.right
                }
            }
            val pageBottom = pageY + (page.pageSize.height - pageMargins.bottom)
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
}