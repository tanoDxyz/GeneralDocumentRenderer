package com.tanodxyz.documentrenderer.elements

import android.R
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.text.*
import android.util.Log
import android.util.SparseArray
import com.tanodxyz.documentrenderer.DocumentRenderView.Companion.PAGE_SNAPSHOT_SCALE_DOWN_FACTOR
import com.tanodxyz.documentrenderer.events.IMotionEventMarker
import com.tanodxyz.documentrenderer.page.DocumentPage
import com.tanodxyz.documentrenderer.spToPx
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import kotlin.math.roundToInt


class StaticTextElement(page: DocumentPage) : PageElement(page = page) {
    private lateinit var text: String
    private var textPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = DEFAULT_TEXT_COLOR
        this.textSize = DEFAULT_TEXT_SIZE
    }

    var textSizePixels: Float = DEFAULT_TEXT_SIZE

    private var layout: StaticLayout? = null

    override var type = "TextElement"
    fun setText(text: String, textSizeSp: Float = -1F) {
        this.text = text
        setTextSize(textSizeSp)
        initTextLayout(false)
    }

    fun setTextSize(sp: Float) {
        textSizePixels = if (sp <= 0) {
            DEFAULT_TEXT_SIZE
        } else {
            page?.documentRenderView?.resources?.spToPx(sp) ?: DEFAULT_TEXT_SIZE
        }
    }

    private fun initTextLayout(drawFromOrigin: Boolean) {
        val oldTextSize = textPaint.textSize
        val newTextSize = page!!.documentRenderView.toCurrentScale(textSizePixels)
        if (!drawFromOrigin && oldTextSize == newTextSize && layout != null) {
            return
        }
        textPaint.textSize =
            page.documentRenderView.toCurrentScale(
                if (drawFromOrigin) textSizePixels.div(
                    PAGE_SNAPSHOT_SCALE_DOWN_FACTOR
                ) else textSizePixels
            )
        val width = if (drawFromOrigin) page.documentRenderView.toCurrentScale(layoutParams.width)
            .div(PAGE_SNAPSHOT_SCALE_DOWN_FACTOR) else page.documentRenderView.toCurrentScale(
            layoutParams.width
        )
        val height = if(drawFromOrigin) page.documentRenderView.toCurrentScale(layoutParams.height).div(
            PAGE_SNAPSHOT_SCALE_DOWN_FACTOR) else page.documentRenderView.toCurrentScale(layoutParams.height)
        val maxLinesByInspection =
            getMaxLinesByInspection(makeStaticLayout(width.roundToInt(),Int.MAX_VALUE), height.roundToInt())
        layout = makeStaticLayout(width.roundToInt(),maxLinesByInspection)
    }

    private fun makeStaticLayout(width: Int, maxLines: Int): StaticLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width)
                .setMaxLines(maxLines).setEllipsize(TextUtils.TruncateAt.END).build()
        } else {
            val constructor: Constructor<StaticLayout> =
                StaticLayout::class.java.getConstructor(
                    CharSequence::class.java,
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                    TextPaint::class.java,
                    Int::class.javaPrimitiveType,
                    Layout.Alignment::class.java,
                    TextDirectionHeuristic::class.java,
                    Float::class.javaPrimitiveType,
                    Float::class.javaPrimitiveType,
                    Boolean::class.javaPrimitiveType,
                    TextUtils.TruncateAt::class.java,
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType
                )
            constructor.setAccessible(true)
            constructor.newInstance(
                text,
                0,
                text.length,
                textPaint,
                width,
                Layout.Alignment.ALIGN_NORMAL,
                TextDirectionHeuristics.FIRSTSTRONG_LTR,
                1.0,
                1.0,
                true,
                TextUtils.TruncateAt.END,
                width,
                maxLines
            )
        }
    }

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
                        )
                    )
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

    private fun getMaxLinesByInspection(staticLayout: StaticLayout, maxHeight: Int): Int {
        var line = staticLayout.lineCount - 1
        while (line >= 0 && staticLayout.getLineBottom(line) >= maxHeight) {
            line--
        }
        return line + 1
    }

    companion object {
        const val DEFAULT_TEXT_SIZE = 22F //px
        const val DEFAULT_TEXT_COLOR = Color.BLACK
    }
}