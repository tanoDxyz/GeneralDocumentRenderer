package com.tanodxyz.documentrenderer.elements

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.text.*
import android.util.SparseArray
import com.tanodxyz.documentrenderer.DocumentRenderView.Companion.PAGE_SNAPSHOT_SCALE_DOWN_FACTOR
import com.tanodxyz.documentrenderer.events.IMotionEventMarker
import com.tanodxyz.documentrenderer.page.DocumentPage
import kotlin.math.roundToInt


class StaticTextElement(page: DocumentPage) : PageElement(page = page) {
    private lateinit var text: String
    private var textPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = DEFAULT_TEXT_COLOR
        this.textSize = DEFAULT_TEXT_SIZE
    }

    var textSize: Float = DEFAULT_TEXT_SIZE

    private var layout: StaticLayout? = null

    override var type = "TextElement"
    fun setText(text: String) {
        this.text = text
        initTextLayout(false)
    }

    private fun initTextLayout(drawFromOrigin: Boolean) {
        val oldTextSize = textPaint.textSize
        val newTextSize = page!!.documentRenderView.toCurrentScale(textSize)
        if (!drawFromOrigin && oldTextSize == newTextSize && layout != null) {
            return
        }
        textPaint.textSize =
            page.documentRenderView.toCurrentScale(if (drawFromOrigin) textSize.div(PAGE_SNAPSHOT_SCALE_DOWN_FACTOR) else textSize)
        val width = if (drawFromOrigin) page.documentRenderView.toCurrentScale(layoutParams.width)
            .div(PAGE_SNAPSHOT_SCALE_DOWN_FACTOR) else page.documentRenderView.toCurrentScale(layoutParams.width)
        layout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width.roundToInt()).build()
        } else {
            StaticLayout(
                text,
                textPaint,
                width.roundToInt(),
                Layout.Alignment.ALIGN_NORMAL,
                1.0F,
                1.0F,
                true
            )
        }
    }

//    var textSizeSp: Float
//        get() {
//            return page?.documentRenderView?.resources?.pxToSp(textPaint.textSize)
//                ?: DEFAULT_TEXT_SIZE
//        }
//        set(size) {
//            textPaint.textSize =
//                page?.documentRenderView?.resources?.spToPx(size) ?: DEFAULT_TEXT_SIZE
//        }

    var textColor: Int
        get() {
            return textPaint.color
        }
        set(color) {
            textPaint.color = color
        }

    override fun onEvent(iMotionEventMarker: IMotionEventMarker?): Boolean {
        super.onEvent(iMotionEventMarker)
        return true
    }

    override fun draw(canvas: Canvas, args: SparseArray<Any>?) {
        super.draw(canvas, args)
        val drawFromOrigin = args.shouldDrawFromOrigin()
        initTextLayout(drawFromOrigin)
        canvas.apply {
            save()
            if (drawFromOrigin) {
                translate(
                    page!!.documentRenderView.toCurrentScale(
                        layoutParams.x.div(
                            PAGE_SNAPSHOT_SCALE_DOWN_FACTOR
                        )
                    ),
                    page.documentRenderView.toCurrentScale(
                        layoutParams.y.div(
                            PAGE_SNAPSHOT_SCALE_DOWN_FACTOR
                        ))
                )

            } else {
                getBoundsRelativeToPage().apply {
                    translate(left, top)
                }
            }
            layout?.draw(canvas)
            restore()
        }
    }


    companion object {
        const val DEFAULT_TEXT_SIZE = 22F //px
        const val DEFAULT_TEXT_COLOR = Color.BLACK
    }
}