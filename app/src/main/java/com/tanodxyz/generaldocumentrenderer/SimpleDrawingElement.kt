package com.tanodxyz.generaldocumentrenderer

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.SparseArray
import androidx.core.text.toSpannable
import com.tanodxyz.documentrenderer.dpToPx
import com.tanodxyz.documentrenderer.elements.ImageElement
import com.tanodxyz.documentrenderer.elements.PageElement
import com.tanodxyz.documentrenderer.elements.SimpleTextElement
import com.tanodxyz.documentrenderer.events.IMotionEventMarker
import com.tanodxyz.documentrenderer.getHeight
import com.tanodxyz.documentrenderer.getWidth
import com.tanodxyz.documentrenderer.hasGenericMotionEvent
import com.tanodxyz.documentrenderer.page.DocumentPage
import kotlin.math.roundToInt

class SimpleDrawingElement(resources: Resources, page: DocumentPage) :
    PageElement(page) {
    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null
    private var _10Dp = 0F
    var textElement = SimpleTextElement(page).apply {
        setText("Simple Canvas".toSpannable())
        textColor = Color.BLACK
    }

    var imageElement = ImageElement(page)

    init {
        movable = true
        debug = false
        _10Dp = resources.dpToPx(10)
        this.debugPaint.apply {
            color = Color.GREEN
            strokeWidth = _10Dp
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.FILL
        }


    }

    override fun getContentHeight(args: SparseArray<Any>?): Float {
        return page.pageBounds.getHeight()
    }

    override fun getContentWidth(args: SparseArray<Any>?): Float {
        return page.pageBounds.getWidth()
    }

    override fun onEvent(iMotionEventMarker: IMotionEventMarker?): Boolean {
        super.onEvent(iMotionEventMarker)
        if (iMotionEventMarker.hasGenericMotionEvent() && isEventOccurredWithInBounds(
                iMotionEventMarker,
                true
            )
        ) {
            val contentBounds = getContentBounds(mostRecentArgs.shouldDrawSnapShot())
            val x = (iMotionEventMarker?.getX() ?: 0f) - contentBounds.left
            val y = (iMotionEventMarker?.getY() ?: 0f) - contentBounds.top
            canvas?.drawPoint(x, y, debugPaint)
        }

        return true;
    }

    override fun draw(canvas: Canvas, args: SparseArray<Any>?) {
        super.draw(canvas, args)
        val contentBounds = getContentBounds(args.shouldDrawSnapShot())
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(getContentWidth(args).roundToInt(), getContentWidth(args).roundToInt(), Bitmap.Config.ARGB_8888)
            this.canvas = Canvas(bitmap!!)
            imageElement.load(bitmap!!, false)
        }
        imageElement.setContentBounds(contentBounds)
        imageElement.draw(canvas,args)
        textElement.draw(canvas, args)
    }
}