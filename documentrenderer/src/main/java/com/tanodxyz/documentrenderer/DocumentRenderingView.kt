package com.tanodxyz.documentrenderer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.*
import kotlin.concurrent.thread

class DocumentRenderingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback, DrawingSource {

    private var surfaceHolder: SurfaceHolder? = null
    private var reDrawer: ReDrawer? = null

    init {
        setRedrawer(DefaultReDrawer())
        holder.addCallback(this)
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

    override fun surfaceCreated(holder: SurfaceHolder) {
        this.surfaceHolder = holder
        this.reDrawer?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        this.surfaceHolder = holder
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        this.surfaceHolder = null
        this.reDrawer?.stop()
    }

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
    override fun draw(canvas: Canvas?) {
        canvas?.apply {
            super.draw(canvas)
        }
    }
    
}