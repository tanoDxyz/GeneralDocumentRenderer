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
import com.tanodxyz.documentrenderer.getHeight
import com.tanodxyz.documentrenderer.page.DocumentPage
import kotlin.math.roundToInt

open class ImageElement(page: DocumentPage) : PageElement(page = page) {
    private var bitmap: Bitmap? = null
    var unloadedBitmapRectangleAndTextColor = Color.BLACK
    var unloadedBitmapRectangleBorderSize = 2F // pixels
    var unloadedBitmapTextSize = 14F // pixels
    var unloadedBitmapTextMessage = "Image"
    private var paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = unloadedBitmapRectangleAndTextColor
        textSize = unloadedBitmapTextSize
        strokeWidth = unloadedBitmapRectangleBorderSize
        style = Paint.Style.STROKE

    }

    // image
    @Synchronized
    fun load(bitmap: Bitmap) {
        this.bitmap = bitmap
    }

    open fun drawUnloadedBitmapBox(
        canvas: Canvas,
        boundsRelativeToPage: RectF,
        args: SparseArray<Any>?
    ) {
        canvas.drawRect(boundsRelativeToPage, paint)
        val textSizeRelativeToSnap = args.textSizeRelativeToSnap(unloadedBitmapTextSize)
        paint.textSize = textSizeRelativeToSnap
        val textWidth = paint.measureText(unloadedBitmapTextMessage)
        val halfWidth = actualWidth.div(2)
        val textDrawX = (halfWidth - (textWidth.div(2))) + boundsRelativeToPage.left
        val textDrawY =
            boundsRelativeToPage.top + boundsRelativeToPage.getHeight().div(2)
        canvas.drawText(unloadedBitmapTextMessage, textDrawX, textDrawY, paint)
    }

    override fun draw(canvas: Canvas, args: SparseArray<Any>?) {
        super.draw(canvas, args)
        val boundsRelativeToPage = getBoundsRelativeToPage(args.shouldDrawFromOrigin())
        synchronized(this) {
            if (bitmap == null) {
                drawUnloadedBitmapBox(canvas, boundsRelativeToPage, args)
            } else {
                val leftAndTop = args.getLeftAndTop()
                val srcRect = Rect(leftAndTop.x.roundToInt(),leftAndTop.y.roundToInt(),bitmap!!.width,bitmap!!.height)
                drawBitmap(canvas,bitmap!!,srcRect,boundsRelativeToPage.toRect(),paint)
            }
        }
    }

    open fun drawBitmap(canvas: Canvas, bitmap: Bitmap, srcRect: Rect?, targetRect: Rect, paint: Paint) {
        canvas.drawBitmap(bitmap!!, null, targetRect, null)
    }

    override fun onEvent(iMotionEventMarker: IMotionEventMarker?): Boolean {
        return super.onEvent(iMotionEventMarker)
    }

}