package com.tanodxyz.documentrenderer

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.tanodxyz.documentrenderer.document.Document
import com.tanodxyz.documentrenderer.page.DocumentPage

open class DocumentRenderingSurfaceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), DrawingSource {

    private var surfaceHolder: SurfaceHolder? = null
    protected var reDrawer: ReDrawer? = null


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


    private var documentPages = mutableListOf<DocumentPage>()

    init {
        val fourDp = resources.dpToPx(4).toInt()
        pageMargins = Rect(fourDp,fourDp,fourDp,fourDp)
        pageCorners = fourDp.toFloat()
        setupPagePaintObject()
        setRedrawer(DefaultReDrawer())
        holder.addCallback(SurfaceHolderCallbacks())

    }

    private inner class SurfaceHolderCallbacks : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            this@DocumentRenderingSurfaceView.apply {
                surfaceHolder = holder
                reDrawer?.start()
            }
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            this@DocumentRenderingSurfaceView.surfaceHolder = holder
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            this@DocumentRenderingSurfaceView.apply {
                surfaceHolder = null
                reDrawer?.stop()
            }
        }
    }

    private fun setupPagePaintObject() {
        pagePaint.apply {
            style = Paint.Style.FILL_AND_STROKE
            color = if (nightMode) Color.BLACK else pageBackColor
        }
    }


    fun setRedrawer(reDrawer: ReDrawer) {
        this.reDrawer?.stop()
        this.reDrawer = reDrawer
        this.reDrawer?.setSurface(this)
        this.surfaceHolder?.surface?.apply {
            if (this.isValid) {
                reDrawer.start()
            }
        }
    }

    fun getRedrawer() = reDrawer


    override fun onDrawFrame(canvas: Canvas?) {
        draw(canvas)
    }

    override fun getSurfaceHolder(): SurfaceHolder? {
        return surfaceHolder
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }
    var c = false
    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
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

            // draw all page backgrounds
            if(!c) {
                for (i: Int in 0 until documentPages.count()) {
                    val documentPage = documentPages[i]
                    drawPageBackground(documentPage)
                }
                currentOffsetY = 0f
                c= true
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
    }

    open fun Canvas.drawPageBackground(page: DocumentPage) {
        val pageStartX = currentOffsetX + pageMargins.left
        val pageStartY = currentOffsetY + pageMargins.top
        val pageEnd =
        if(pageFitPolicy == Document.PAGE_FIT_POLICY.FIT_WIDTH) {
            val screenWidthInPixels = resources.displayMetrics.widthPixels
            (screenWidthInPixels - pageMargins.right).toFloat()
        } else if(pageFitPolicy == Document.PAGE_FIT_POLICY.FIT_PAGE) {
            //todo fit the page to the screen amigo
            (page.pageSize.width - pageMargins.right)
        } else {
            (page.pageSize.width - pageMargins.right)
        }
        val pageBottom = (page.pageSize.height - pageMargins.bottom) + pageStartY
        if (pageCorners > 0) {
            drawRoundRect(RectF(pageStartX,pageStartY,pageEnd,pageBottom),pageCorners,pageCorners,pagePaint)
        } else {
            drawRect(RectF(pageStartX,pageStartY,pageEnd,pageBottom),pagePaint)
        }
        currentOffsetY = pageStartY + (page.pageSize.height - pageMargins.bottom)
    }

    fun startScrollingDebug() {
        while(true) {
            Thread.sleep(500)
            currentOffsetY +=10
            c= false
        }
    }
}