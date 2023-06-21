package com.tanodxyz.documentrenderer.elements

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.text.TextPaint
import android.util.SparseArray
import androidx.core.graphics.toRect
import com.tanodxyz.documentrenderer.dpToPx
import com.tanodxyz.documentrenderer.events.IMotionEventMarker
import com.tanodxyz.documentrenderer.page.DocumentPage

open class ImageElement(
    page: DocumentPage,
    var unloadedBitmapRectangleColor: Int = Color.BLACK,
    var unloadedBitmapRectangleTextColor: Int = Color.BLACK,
    var unloadedBitmapRectangleBorderSize: Float = 2F, // pixels
    var unloadedBitmapTextSize: Float = 14F, // pixels
    var unloadedBitmapTextMessage: String = "Image",
    var drawUnloadedBitmapBox: Boolean = true
) : PageElement(page = page) {

    private var bitmap: Bitmap? = null

    override var type = "ImageElement"

    private var paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = unloadedBitmapRectangleColor
        strokeWidth = unloadedBitmapRectangleBorderSize
        style = Paint.Style.STROKE

    }
    var textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = unloadedBitmapRectangleTextColor
        textSize = unloadedBitmapTextSize
        style = Paint.Style.FILL
    }

    // image
    @Synchronized
    fun load(bitmap: Bitmap) {
        this.bitmap = bitmap
        page.redraw()
    }

    open fun drawUnloadedBitmapBox(
        canvas: Canvas,
        boundsRelativeToPage: RectF,
        args: SparseArray<Any>?
    ) {
        canvas.drawRect(boundsRelativeToPage, paint)
        val textSizeRelativeToSnap = args.textSizeRelativeToSnap(unloadedBitmapTextSize)
        textPaint.textSize = textSizeRelativeToSnap
        val fm: Paint.FontMetrics = textPaint.getFontMetrics()
        val textHeight = fm.bottom - fm.top + fm.leading
        val textDrawX =
            boundsRelativeToPage.left + page.documentRenderView.context.resources.dpToPx(8)
        val textDrawY =
            boundsRelativeToPage.top + textHeight
        canvas.drawText(unloadedBitmapTextMessage, textDrawX, textDrawY, textPaint)
    }

    override fun draw(canvas: Canvas, args: SparseArray<Any>?) {
        super.draw(canvas, args)
        val boundsRelativeToPage = getBoundsRelativeToPage(args.shouldDrawSnapShot())
        synchronized(this) {
            if (bitmap == null && drawUnloadedBitmapBox) {
                drawUnloadedBitmapBox(canvas, boundsRelativeToPage, args)
            } else {
                drawBitmap(canvas, bitmap!!, boundsRelativeToPage.toRect(), paint)
            }
        }
    }

    open fun drawBitmap(
        canvas: Canvas,
        bitmap: Bitmap,
        targetRect: Rect,
        paint: Paint
    ) {
        canvas.drawBitmap(bitmap, null, targetRect, null)
    }

    override fun onEvent(iMotionEventMarker: IMotionEventMarker?): Boolean {
        return super.onEvent(iMotionEventMarker)
    }

}