package com.tanodxyz.generaldocumentrenderer

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.SparseArray
import android.view.MotionEvent
import com.tanodxyz.documentrenderer.dpToPx
import com.tanodxyz.documentrenderer.elements.PageElement
import com.tanodxyz.documentrenderer.elements.SimpleTextElement
import com.tanodxyz.documentrenderer.events.IMotionEventMarker
import com.tanodxyz.documentrenderer.hasGenericMotionEvent
import com.tanodxyz.documentrenderer.page.DocumentPage

class SimpleDrawingElement(resources: Resources, page: DocumentPage, width: Int, height: Int) :
    PageElement(page, width, height) {
    var points = mutableListOf<PointF>()

    var textElement = SimpleTextElement(page)
    init {
        this.debugPaint.apply {
            color = Color.GREEN
            strokeWidth = resources.dpToPx(10)
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.FILL
        }

    }

    override fun onEvent(iMotionEventMarker: IMotionEventMarker?): Boolean {
        val eventOccurredWithInBounds = isEventOccurredWithInBounds(iMotionEventMarker, true)
        if (iMotionEventMarker.hasGenericMotionEvent()) {
            val genericMotionEvent = iMotionEventMarker?.event

            if (eventOccurredWithInBounds) {
                iMotionEventMarker.apply {
                    if (page.documentRenderView.canProcessTouchEvents && genericMotionEvent?.action == MotionEvent.ACTION_DOWN) {
                        page.documentRenderView.canProcessTouchEvents = false
                    }
                    val x = this?.getX() ?: 0f
                    val y = this?.getY() ?: 0f
                    points.add(PointF(x, y))
                }

            }

            // just whenever action up is triggered we will assume user stopped drawing.
            if (genericMotionEvent?.action == MotionEvent.ACTION_UP) {
                page.documentRenderView.canProcessTouchEvents = true
            }

        }

        return true;
    }

    override fun draw(canvas: Canvas, args: SparseArray<Any>?) {
        super.draw(canvas, args)
        val boundsRelativeToPage = getBoundsRelativeToPage(args.shouldDrawSnapShot())
        points.forEach { point ->
            canvas.drawPoint(
                point.x + boundsRelativeToPage.left,
                point.y + boundsRelativeToPage.top,
                debugPaint
            )
        }
    }
}