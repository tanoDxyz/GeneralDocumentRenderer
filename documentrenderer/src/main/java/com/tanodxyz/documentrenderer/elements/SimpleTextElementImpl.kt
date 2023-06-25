package com.tanodxyz.documentrenderer.elements

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
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
import com.tanodxyz.documentrenderer.events.IMotionEventMarker
import com.tanodxyz.documentrenderer.getHeight
import com.tanodxyz.documentrenderer.getWidth
import com.tanodxyz.documentrenderer.misc.DialogHandle
import com.tanodxyz.documentrenderer.page.DocumentPage
import java.lang.reflect.Constructor
import kotlin.math.roundToInt

open class SimpleTextElementImpl(page: DocumentPage) : PageElementImpl(page = page),
    PageElementImpl.OnLongPressListener {
    private var editTextDialog: EditTextDialog? = null
    protected var appliedLineBreaking = false
    protected lateinit var spannable: Spannable
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
    var allowTextEditing = true

    private var widthInCaseWrapContent = 0
    private var heightInCaseWrapContent = 0

    @RequiresApi(Build.VERSION_CODES.M)
    var lineBreakingStrategy = Layout.BREAK_STRATEGY_SIMPLE

    @RequiresApi(Build.VERSION_CODES.M)
    var hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NONE

    @RequiresApi(Build.VERSION_CODES.P)
    var useLineSpacingFromFallbacks = false

    var applySimpleLineBreaking = false

    protected var layout: StaticLayout? = null

    override var type = "TextElement"

    init {
        longPressListener = this
    }

    fun setText(text: Spannable) {
        this.spannable = text
        appliedLineBreaking = false
        layout = null
        page.redraw()
    }

    fun applySimpleLineBreaking() {
        if (!appliedLineBreaking && applySimpleLineBreaking) {
            appliedLineBreaking = true
            val wordsList = this.spannable.split(' ', '\n')
            val stringBuilder = java.lang.StringBuilder(spannable.length)
            val availableLineWidth = calculateWidth(false) - 200
            var lineWidthConsumed = 0F
            wordsList.forEach { word ->
                val wordWidth = StaticLayout.getDesiredWidth(word, textPaint)
                lineWidthConsumed += wordWidth
                if (lineWidthConsumed < availableLineWidth) {
                    stringBuilder.append(word).append(' ')
                } else {
                    stringBuilder.append('\n').append(word).append(' ')
                    lineWidthConsumed = 0F
                }
            }
            spannable = SpannableString(stringBuilder.toString())
        }
    }

    fun setTypeFace(typeface: Typeface) {
        textPaint.typeface = typeface
    }

    override fun contentWidthInCaseOfWrapContent(): Int {
        return widthInCaseWrapContent
    }

    override fun contentHeightInCaseOfWrapContent(): Int {
        return heightInCaseWrapContent
    }

    protected fun calculateHeight(drawSnapShot: Boolean): Int {
        val height = if (layoutParams.isHeightWrapContent()) {
            val scaledMargins = getScaledMargins(drawSnapShot)
            val pageHeight = page.pageBounds.getHeight()
            var calculatedHeight =
                makeStaticLayout(
                    spannable,
                    widthInCaseWrapContent,
                    Int.MAX_VALUE
                ).height

            if (calculatedHeight >= pageHeight) {
                calculatedHeight = pageHeight.roundToInt()
            }
            calculatedHeight += (longestLineHeight(drawSnapShot)) // we are adding it cause
            heightInCaseWrapContent = calculatedHeight
            heightInCaseWrapContent - scaledMargins.bottom.roundToInt() - scaledMargins.top.roundToInt()
        } else {
            getBoundsRelativeToPage(drawSnapShot).getHeight().roundToInt()
        }
        return height
    }

    fun longestLineHeight(drawSnapShot: Boolean): Int {
        val layout = makeStaticLayout(spannable, calculateWidth(drawSnapShot), Int.MAX_VALUE)
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

    protected fun calculateWidth(drawSnapShot: Boolean): Int {
        val width = if (layoutParams.isWidthWrapContent()) {
            val scaledMargins = getScaledMargins(drawSnapShot)
            val pageWidth = page.pageBounds.getWidth()
            var calculatedWidth = (StaticLayout.getDesiredWidth(spannable, textPaint)).roundToInt()
            if (calculatedWidth >= pageWidth) {
                calculatedWidth = pageWidth.roundToInt()
            }
            widthInCaseWrapContent = calculatedWidth
            widthInCaseWrapContent - scaledMargins.right.roundToInt() - scaledMargins.left.roundToInt()
        } else {
            getBoundsRelativeToPage(drawSnapShot).getWidth().roundToInt()
        }
        return width
    }

    protected fun initTextLayout(args: SparseArray<Any>?) {
        val drawSnapShot = args.shouldDrawSnapShot()
        val oldTextSize = textPaint.textSize
        val newTextSize = page.documentRenderView.toCurrentScale(textSizePixels)
        if (!drawSnapShot && oldTextSize == newTextSize && layout != null) {
            return
        }
        textPaint.textSize = args.textSizeRelativeToSnap(textSizePixels)

        val width = calculateWidth(drawSnapShot)

        val height = calculateHeight(drawSnapShot)
        applySimpleLineBreaking()
        val maxLinesByInspection =
            getMaxLinesByInspection(
                makeStaticLayout(spannable, width, Int.MAX_VALUE),
                height
            )
        layout = makeStaticLayout(spannable, width, maxLinesByInspection)
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
            initTextLayout(args)
            canvas.apply {
                save()
                val leftAndTop = args.getLeftAndTop()
                translate(leftAndTop.x, leftAndTop.y)
                layout?.draw(canvas)
                restore()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    open fun showTextEditDialog() {
        if (editTextDialog == null) {
            editTextDialog = EditTextDialog(page.documentRenderView.context, false) { changedText ->
                changedText?.let { setText(it) }
            }
        }
        editTextDialog?.setTextAndShow(spannable)
    }

    companion object {
        const val DEFAULT_TEXT_SIZE = 22F //px
        const val DEFAULT_TEXT_COLOR = Color.BLACK
        fun newInstance(
            page: DocumentPage,
            text: Spannable,
            textSizePixels: Float,
            textColor: Int,
            typeface: Typeface? = null
        ): SimpleTextElementImpl {
            return SimpleTextElementImpl(page).apply {
                typeface?.let { setTypeFace(it) }
                this.textColor = textColor
                this.textSizePixels = textSizePixels
                textPaint.textSize = this.textSizePixels
                setText(text)
                layoutParams.desiredHeight = WRAP_CONTENT
                layoutParams.desiredWidth = WRAP_CONTENT
            }
        }
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

    override fun onLongPress(eventMarker: IMotionEventMarker?, pageElementImpl: PageElementImpl) {
        if (allowTextEditing) {
            showTextEditDialog()
        }
    }
}