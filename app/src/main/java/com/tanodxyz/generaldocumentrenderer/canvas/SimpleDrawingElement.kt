package com.tanodxyz.generaldocumentrenderer.canvas

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.SparseArray
import androidx.core.text.toSpannable
import com.tanodxyz.documentrenderer.copy
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
    private var redBrushColorChooser = BrushColorChooser(page,Color.RED,"Red")
    private var yellowBrushColorChooser = BrushColorChooser(page,Color.YELLOW,"Yellow")
    private var blueBrushColorChooser = BrushColorChooser(page,Color.BLUE,"Blue")
    var textElement = SimpleTextElement(page).apply {
        setText("Green Brush Selected".toSpannable())
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
        if (iMotionEventMarker.hasGenericMotionEvent()
            && isEventOccurredWithInBounds(iMotionEventMarker, true)
        ) {
            val redBrushColorChooserTapped = redBrushColorChooser.onEvent(iMotionEventMarker)
            val yellowBrushColorChooserTapped = yellowBrushColorChooser.onEvent(iMotionEventMarker)
            val blueBrushColorChooserTapped = blueBrushColorChooser.onEvent(iMotionEventMarker)
            if(!redBrushColorChooserTapped && !yellowBrushColorChooserTapped && !blueBrushColorChooserTapped) {
                val contentBounds = getContentBounds(mostRecentArgs.shouldDrawSnapShot())
                val x = (iMotionEventMarker?.getX() ?: 0f) - contentBounds.left
                val y = (iMotionEventMarker?.getY() ?: 0f) - contentBounds.top
                canvas?.drawPoint(x, y, debugPaint)
            }
        }

        return true;
    }

    override fun draw(canvas: Canvas, args: SparseArray<Any>?) {
        super.draw(canvas, args)
        val contentBounds = getContentBounds(args.shouldDrawSnapShot())
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(
                contentBounds.getWidth().roundToInt(),
                contentBounds.getHeight().roundToInt(),
                Bitmap.Config.ARGB_8888
            )
            this.canvas = Canvas(bitmap!!)
            imageElement.load(bitmap!!, false)
        }

        imageElement.setContentBounds(contentBounds)
        imageElement.draw(canvas, args)
        textElement.draw(canvas, args)

        // set bounds for red brush to draw inside another element
        val leftTopMarginFromParent = page.documentRenderView.toCurrentScale(50)
        val redBrushContentBounds = contentBounds.copy().apply {
            left += leftTopMarginFromParent
            top += leftTopMarginFromParent
            right = left + redBrushColorChooser.getContentWidth(args)
            bottom = top + redBrushColorChooser.getContentHeight(args)
        }
        redBrushColorChooser.setContentBounds(redBrushContentBounds)
        redBrushColorChooser.draw(canvas, args)

        // set for yellow brush chooser
        // set bounds for red brush to draw inside another element
        val yellowBrushContentBounds = contentBounds.copy().apply {
            left += page.documentRenderView.toCurrentScale(100) + leftTopMarginFromParent
            right = left + yellowBrushColorChooser.getContentWidth(args)
            top += leftTopMarginFromParent
            bottom = top + yellowBrushColorChooser.getContentHeight(args)
        }

        yellowBrushColorChooser.setContentBounds(yellowBrushContentBounds)
        yellowBrushColorChooser.draw(canvas, args)


        // set for blue brush chooser
        // set bounds for red brush to draw inside another element
        val blueBrushContentBounds = contentBounds.copy().apply {
            left += page.documentRenderView.toCurrentScale(200) + leftTopMarginFromParent
            right = left + blueBrushColorChooser.getContentWidth(args)
            top += leftTopMarginFromParent
            bottom = top + blueBrushColorChooser.getContentHeight(args)
        }

        blueBrushColorChooser.setContentBounds(blueBrushContentBounds)
        blueBrushColorChooser.draw(canvas, args)

    }


    inner class BrushColorChooser(page: DocumentPage,val brushColor:Int,val colorName:String): PageElement(page) {
        init {
            debugPaint.color = brushColor
            debugPaint.style = Paint.Style.FILL
            desiredWidth = 64f
            desiredHeight =64f
            debug = false
        }

        override fun onEvent(event: IMotionEventMarker?): Boolean {
            return if(isEventOccurredWithInBounds(event,false)) {
                this@SimpleDrawingElement.debugPaint.color = brushColor
                this@SimpleDrawingElement.textElement.setText("$colorName Brush Selected".toSpannable())
                true
            } else {
                false
            }
        }
        override fun draw(canvas: Canvas, args: SparseArray<Any>?) {
            super.draw(canvas, args)
            val contentBounds = getContentBounds(args.shouldDrawSnapShot())
            val cx = contentBounds.left + contentBounds.getWidth().div(2)
            val cy = contentBounds.top + contentBounds.getHeight().div(2)
            canvas.drawCircle(cx,cy, contentBounds.getWidth().div(2),debugPaint)
        }
    }
}