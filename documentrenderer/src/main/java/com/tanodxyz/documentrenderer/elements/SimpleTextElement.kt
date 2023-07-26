package com.tanodxyz.documentrenderer.elements

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.Spannable
import android.text.StaticLayout
import android.text.TextDirectionHeuristic
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.text.TextUtils
import android.util.SparseArray
import android.view.View
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.core.text.toSpannable
import com.tanodxyz.documentrenderer.R
import com.tanodxyz.documentrenderer.events.LongPressEvent
import com.tanodxyz.documentrenderer.getHeight
import com.tanodxyz.documentrenderer.getWidth
import com.tanodxyz.documentrenderer.misc.DialogHandle
import com.tanodxyz.documentrenderer.page.DocumentPage
import java.lang.reflect.Constructor
import kotlin.math.roundToInt

open class SimpleTextElement(page: DocumentPage) : PageElement(page),
    PageElement.OnLongPressListener {
    private var editTextDialog: EditTextDialog? = null
    protected var appliedLineBreaking = false
    protected lateinit var spannable: Spannable
    protected var textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = DEFAULT_TEXT_COLOR
        this.textSize = DEFAULT_TEXT_SIZE
    }
    protected var layout: StaticLayout? = null
    var textSizePixels: Float = textPaint.textSize
    private var scaleLevelForWhichSizeMeasured = -1F
    var ellipseSize = TextUtils.TruncateAt.END
    var alignment = Layout.Alignment.ALIGN_NORMAL
    var textDirectionHeuristics: TextDirectionHeuristic = TextDirectionHeuristics.LTR
    var spacingmult = 1.0F
    var spacingAdd = 2.0F
    var textColor:Int = textPaint.color
    var includePadding = false
    var allowTextEditing = true
    override var movable = !allowTextEditing
    @RequiresApi(Build.VERSION_CODES.M)
    var lineBreakingStrategy = Layout.BREAK_STRATEGY_SIMPLE
    @RequiresApi(Build.VERSION_CODES.M)
    var hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NONE
    @RequiresApi(Build.VERSION_CODES.P)
    var useLineSpacingFromFallbacks = false
    override var type = "TextElement"

    init {
        debug = false
        longPressListener = this
    }

    fun setText(text: Spannable) {
        this.spannable = text
        appliedLineBreaking = false
        layout = null
        page.redraw()
    }

    fun getText():Spannable? {
        return if(this::spannable.isInitialized) {
            this.spannable
        } else {
            null
        }
    }

    override fun onLongPress(
        eventMarker: LongPressEvent?,
        pageElement: PageElement
    ) {
        if (allowTextEditing) {
            showTextEditDialog()
        }
    }

    fun setTypeFace(typeface: Typeface) {
        textPaint.typeface = typeface
    }

    override fun setContentBounds(bounds: RectF) {
        super.setContentBounds(bounds)
        layout = null // as bounds changed so text layout must be recreated.
        usePreCalculatedBounds = true
    }

    protected fun initTextLayout(args: SparseArray<Any>?) {
        if (shouldCalculate(args)) {
            textPaint.textSize = args.textSizeRelativeToSnap(textSizePixels)
            textPaint.color = textColor
            val boundsRelativeToPage = this.getContentBounds(args.shouldDrawSnapShot())
            val width = boundsRelativeToPage.getWidth()
            val height = boundsRelativeToPage.getHeight()
            scaleLevelForWhichSizeMeasured = page.documentRenderView.getCurrentZoom()
            val maxLinesByInspection =
                getMaxLinesByInspection(
                    makeStaticLayout(spannable, width.roundToInt(), Int.MAX_VALUE),
                    height
                )
            layout = makeStaticLayout(spannable, width.roundToInt(), maxLinesByInspection)
        }
    }

    private fun getMaxLinesByInspection(staticLayout: StaticLayout, maxHeight: Float): Int {
        var line = staticLayout.lineCount - 1
        while (line >= 0 && staticLayout.getLineBottom(line) >= maxHeight) {
            line--
        }
        return line + 1
    }

    override fun draw(canvas: Canvas, args: SparseArray<Any>?) {
        super.draw(canvas, args)
        try {
            initTextLayout(args)
            canvas.apply {
                save()
                val leftAndTop = args.getLeftAndTop()
                translate(leftAndTop.x , leftAndTop.y )
                layout?.draw(this)
                restore()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun getContentHeight(args: SparseArray<Any>?): Float {
        if (desiredHeight > 0) {
            return super.getContentHeight(args)
        }
        return if (scaleLevelForWhichSizeMeasured != page.documentRenderView.getCurrentZoom()) {
            val pageHeight = page.pageBounds.getHeight()
            val width = getContentWidth(args).roundToInt()
            var calculatedHeight =
                makeStaticLayout(
                    spannable,
                    width,
                    Int.MAX_VALUE
                ).height
            if (calculatedHeight >= pageHeight) {
                calculatedHeight = pageHeight.roundToInt()
            }
            calculatedHeight += (longestLineHeight(args))
            actualHeight = calculatedHeight.toFloat()
            actualHeight
        } else {
            actualHeight
        }
    }

    override fun getContentWidth(args: SparseArray<Any>?): Float {
        if (desiredWidth > 0) {
            return super.getContentWidth(args)
        }
        return if (scaleLevelForWhichSizeMeasured != page.documentRenderView.getCurrentZoom()) {
            val pageWidth = page.pageBounds.getWidth()
            var calculatedWidth = (StaticLayout.getDesiredWidth(spannable, textPaint)).roundToInt()
            if (calculatedWidth >= pageWidth) {
                calculatedWidth = pageWidth.roundToInt()
            }
            actualWidth = (calculatedWidth.toFloat())
            actualWidth
        } else {
            actualWidth
        }
    }

    protected fun makeStaticLayout(
        spannable: Spannable,
        width: Int,
        maxLines: Int
    ): StaticLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val builder =
                StaticLayout.Builder.obtain(spannable, 0, spannable.length, textPaint, width)
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
                spannable,
                0,
                spannable.length,
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

    fun longestLineHeight(args: SparseArray<Any>?): Int {
        val layout = makeStaticLayout(spannable, getContentWidth(args).roundToInt(), Int.MAX_VALUE)
        var longestLineHeight = 0
        layout.apply {
            val lineCount: Int = lineCount
            for (i in 0 until lineCount) {
                val lineHeight: Int = getLineBottom(i) - getLineTop(i)
                if (lineHeight > longestLineHeight) {
                    longestLineHeight = lineHeight
                }
            }
        }
        return longestLineHeight
    }

    open fun showTextEditDialog() {
        if (editTextDialog == null) {
            editTextDialog = EditTextDialog(page.documentRenderView.context, false) { changedText ->
                changedText?.let { setText(it) }
            }
        }
        editTextDialog?.setTextAndShow(spannable)
    }

    fun shouldCalculate(args: SparseArray<Any>?): Boolean {
        val drawSnapShot = args.shouldDrawSnapShot()
        val oldTextSize = textPaint.textSize
        val newTextSize = page.documentRenderView.toCurrentScale(textSizePixels)
        return (drawSnapShot || oldTextSize != newTextSize || layout == null)
    }

    inner class EditTextDialog(
        windowContext: Context,
        cancelOnTouchOutSide: Boolean = true,
        val textChangeCallback: (Spannable?) -> Unit
    ) :
        DialogHandle(windowContext, cancelOnTouchOutSide) {
        init {

            createDialog()
            setListeners()

            setOnShowListener {
                page.documentRenderView.canProcessTouchEvents = false
            }

            setOnDismissListener {
                page.documentRenderView.canProcessTouchEvents = true
            }
        }

        fun setListeners() {
            findViewViaId<View>(R.id.okButton)?.setOnClickListener {
                textChangeCallback(findViewViaId<EditText>(R.id.inputView)?.text?.toSpannable())
                hide()
            }
            findViewViaId<View>(R.id.cancelButton)?.setOnClickListener {
                hide()
            }
        }

        fun setTextAndShow(spannable: Spannable) {
            findViewViaId<EditText>(R.id.inputView)?.setText(spannable)
            show()
        }

        override fun getContainerLayout(): Int {
            return R.layout.text_edit_dialog
        }

    }

    companion object {
        const val DEFAULT_TEXT_SIZE = 22F //px
        const val DEFAULT_TEXT_COLOR = Color.BLACK
        fun calculateTextHeight(
            simpleTextElement: SimpleTextElement,
            maxWidth: Int
        ): Int {
            val staticLayout = StaticLayout(
                simpleTextElement.spannable, simpleTextElement.textPaint, (maxWidth -  if(simpleTextElement.symmetric)  simpleTextElement.margins.left.times(2) else 0F).roundToInt(),
                simpleTextElement.alignment, simpleTextElement.spacingmult, simpleTextElement.spacingAdd, simpleTextElement.includePadding
            )
            return staticLayout.height
        }
    }
}