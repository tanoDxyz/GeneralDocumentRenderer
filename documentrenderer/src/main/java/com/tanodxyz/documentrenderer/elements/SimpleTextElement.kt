package com.tanodxyz.documentrenderer.elements

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristic
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.text.TextUtils
import android.util.SparseArray
import androidx.annotation.RequiresApi
import com.tanodxyz.documentrenderer.events.IMotionEventMarker
import com.tanodxyz.documentrenderer.getHeight
import com.tanodxyz.documentrenderer.getWidth
import com.tanodxyz.documentrenderer.page.DocumentPage
import com.tanodxyz.documentrenderer.pagesizecalculator.PageSizeCalculator
import java.lang.Exception
import java.lang.reflect.Constructor
import kotlin.math.roundToInt

class SimpleTextElement(page: DocumentPage) : PageElement(page = page) {
    protected var appliedLineBreaking = false
    protected lateinit var charSequence: CharSequence
    protected var textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { 
        this.color = DEFAULT_TEXT_COLOR
        this.textSize = DEFAULT_TEXT_SIZE
    }
    var textSizePixels: Float = textPaint.textSize
    var ellipseSize = TextUtils.TruncateAt.END
    var alignment = Layout.Alignment.ALIGN_NORMAL
    var textDirectionHeuristics = TextDirectionHeuristics.LTR
    var spacingmult = 1.0F
    var spacingAdd = 2.0F
    var includePadding = false
    @RequiresApi(Build.VERSION_CODES.M)
    var lineBreakingStrategy = Layout.BREAK_STRATEGY_SIMPLE

    @RequiresApi(Build.VERSION_CODES.M)
    var hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NONE

    @RequiresApi(Build.VERSION_CODES.P)
    var useLineSpacingFromFallbacks = false

    var applySimpleLineBreaking = true

    protected var layout: StaticLayout? = null

    override var type = "TextElement"

    fun setText(text: CharSequence) {
        this.charSequence = text
        appliedLineBreaking = false
        layout = null
    }
    
    fun applySimpleLineBreaking() {
        if(!appliedLineBreaking && applySimpleLineBreaking) {
            appliedLineBreaking = true
            val wordsList = this.charSequence.split(' ','\n')
            val stringBuilder = java.lang.StringBuilder(charSequence.length)
            val availableLineWidth = calculateWidth(false) - 200
            var lineWidthConsumed = 0F
            wordsList.forEach { word ->
                val wordWidth = StaticLayout.getDesiredWidth(word, textPaint)
                lineWidthConsumed += wordWidth
                if (lineWidthConsumed < availableLineWidth) {
                    stringBuilder.append(word).append(' ')
                } else {
                    stringBuilder.append('\n').append(word).append( ' ')
                    lineWidthConsumed = 0F
                }
            }
            charSequence = stringBuilder.toString()
        }
    }

    fun setTypeFace(typeface: Typeface) {
        textPaint.typeface = typeface
    }

    protected fun calculateHeight(drawFromOrigin: Boolean): Int {
        return getBoundsRelativeToPage(drawFromOrigin).getHeight().roundToInt()
    }

    protected fun calculateWidth(drawFromOrigin: Boolean): Int {
        return getBoundsRelativeToPage(drawFromOrigin).getWidth().roundToInt()
    }

    protected fun initTextLayout(args: SparseArray<Any>?) {
        val drawFromOrigin = args.shouldDrawFromOrigin()
        val oldTextSize = textPaint.textSize
        val newTextSize = page.documentRenderView.toCurrentScale(textSizePixels)
        if (!drawFromOrigin && oldTextSize == newTextSize && layout != null) {
            return
        }
        textPaint.textSize = args.textSizeRelativeToSnap(textSizePixels)
        val width = calculateWidth(drawFromOrigin)
        val height = calculateHeight(drawFromOrigin)
        applySimpleLineBreaking()
        val maxLinesByInspection =
            getMaxLinesByInspection(
                makeStaticLayout(width, Int.MAX_VALUE),
                height
            )
        layout = makeStaticLayout(width, maxLinesByInspection)
    }


    protected fun makeStaticLayout(
        width: Int,
        maxLines: Int
    ): StaticLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val builder =
                StaticLayout.Builder.obtain(charSequence, 0, charSequence.length, textPaint, width)
                    .setMaxLines(maxLines).setAlignment(alignment)
                    .setEllipsize(ellipseSize)
                    .setTextDirection(textDirectionHeuristics)
                    .setLineSpacing(spacingAdd, spacingmult)
                    .setIncludePad(includePadding)
                    .setBreakStrategy(lineBreakingStrategy)
                    .setHyphenationFrequency(hyphenationFrequency)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                builder.setUseLineSpacingFromFallbacks(useLineSpacingFromFallbacks)
            }
            builder.build()
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
            constructor.isAccessible = true
            constructor.newInstance(
                charSequence,
                0,
                charSequence.length,
                textPaint,
                width,
                alignment,
                textDirectionHeuristics,
                spacingmult,
                spacingAdd,
                includePadding,
                ellipseSize,
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

    private fun getMaxLinesByInspection(staticLayout: StaticLayout, maxHeight: Int): Int {
        var line = staticLayout.lineCount - 1
        while (line >= 0 && staticLayout.getLineBottom(line) >= maxHeight) {
            line--
        }
        return line + 1
    }

    override fun draw(canvas: Canvas, args: SparseArray<Any>?) {
        super.draw(canvas, args)
        try {
            val drawFromOrigin = args.shouldDrawFromOrigin()
            initTextLayout(args)
            canvas.apply {
                save()
                val leftAndTop = args.getLeftAndTop()
                translate(leftAndTop.x, leftAndTop.y)
                layout?.draw(canvas)
                restore()
            }
        }catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    override fun onEvent(iMotionEventMarker: IMotionEventMarker?): Boolean {
        super.onEvent(iMotionEventMarker)
        return true
    }

    companion object {
        const val DEFAULT_TEXT_SIZE = 22F //px
        const val DEFAULT_TEXT_COLOR = Color.BLACK
    }
}