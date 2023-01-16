package com.tanodxyz.documentrenderer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.view.setMargins
import kotlin.math.roundToInt


class DefaultScrollHandle @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ScrollHandle, View(context, attrs, defStyleAttr) {

    val DEFAULT_WIDTH = context.resources.dpToPx(24)
    val DEFAULT_HEIGHT = context.resources.dpToPx(35)
    private var documentRenderView: DocumentRenderView? = null
    var scrollColor = Color.parseColor("#DCDCDC")
    var ovalLineColor = Color.parseColor("#6082B6")
    var heightScroller = 0F
    var widthScroller = 0F
    private val _2dp = context.resources.dpToPx(2)
    var margingFromParent = context.resources.dpToPx(10)

    var scrollerBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = scrollColor
        style = Paint.Style.FILL_AND_STROKE
        strokeCap = Paint.Cap.ROUND
    }
    val ovalLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ovalLineColor
        style = Paint.Style.FILL_AND_STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = context.resources.dpToPx(2)
    }
    private val oval = RectF(0F, 0F, 0F, 0F)

    var ovalRX = context.resources.dpToPx(100)
    var ovalRY = context.resources.dpToPx(100)
    private val handler_ = Handler(Looper.getMainLooper())
    private val hidePageScrollerRunnable = Runnable { hide(delayed = false) }
    private var relativeHandlerMiddle = 0f
    private var currentPos = 0f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            MeasureSpec.makeMeasureSpec(widthScroller.toInt(), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(heightScroller.toInt(), MeasureSpec.EXACTLY)
        )
    }

    override fun attachTo(view: DocumentRenderView) {
        view.removeView(this)
        val frameLayout = view as FrameLayout
        val layoutParamsForThisView = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParamsForThisView.setMargins(margingFromParent.roundToInt())

        if (view.isSwipeVertical()) {
            widthScroller = DEFAULT_WIDTH
            heightScroller = DEFAULT_HEIGHT
            layoutParamsForThisView.gravity = Gravity.END
        } else {
            widthScroller = DEFAULT_HEIGHT
            heightScroller = DEFAULT_WIDTH
            layoutParamsForThisView.gravity = Gravity.BOTTOM
        }
        layoutParams = layoutParamsForThisView
        frameLayout.addView(this)
        this.documentRenderView = view
        alpha = 0.6F
    }

    override fun detach() {
        this.documentRenderView?.removeView(this)
    }

    fun isScrollHandleShown(): Boolean = visibility == VISIBLE

    override fun scroll(position: Float) {

        if (!isScrollHandleShown()) {
            show()
        } else {
            handler_.removeCallbacks(hidePageScrollerRunnable)
        }
        if (documentRenderView != null) {
            setPosition((if (documentRenderView!!.isSwipeVertical()) documentRenderView!!.getHeight() else documentRenderView!!.getWidth()) * position)
        }
    }

    override fun setPageNumber(pageNumber: String) {

    }

    override fun show() {
        visibility = View.VISIBLE
    }

    override fun hide(delayed: Boolean) {
        if (delayed) {
            handler_.postDelayed(hidePageScrollerRunnable, 1000)
        } else {
            visibility = View.INVISIBLE
        }
    }

    private fun setPosition(pos: Float) {

//        var position = pos
//
//
//        if (java.lang.Float.isInfinite(position) || java.lang.Float.isNaN(position)) {
//            return
//        }
//        val pdfViewSize: Float
//        pdfViewSize = if (documentRenderView!!.isSwipeVertical()) {
//            documentRenderView!!.height.toFloat()
//        } else {
//            documentRenderView!!.width.toFloat()
//        }
//        position -= relativeHandlerMiddle
//        if (position < 0) {
//            position = 0f
//        } else if (position > pdfViewSize - DEFAULT_WIDTH) {
//            position = pdfViewSize - DEFAULT_WIDTH
//        }
//        if (documentRenderView!!.isSwipeVertical()) {
//            y = position
//        } else {
//            x = position
//        }
//        calculateMiddle()

//        return OnTouchListener { view, event ->
//            // position information
//            // about the event by the user
//            val x = event.rawX.toInt()
//            val y = event.rawY.toInt()
//            // detecting user actions on moving
//            when (event.action and MotionEvent.ACTION_MASK) {
//                MotionEvent.ACTION_DOWN -> {
//                    val lParams = view.layoutParams as RelativeLayout.LayoutParams
//                    xDelta = x - lParams.leftMargin
//                    yDelta = y - lParams.topMargin
//                }
//                MotionEvent.ACTION_UP -> Toast.makeText(this,
//                    "new location!", Toast.LENGTH_SHORT)
//                    .show()
//                MotionEvent.ACTION_MOVE -> {
//                    // based on x and y coordinates (when moving image)
//                    // and image is placed with it.
//                    val layoutParams = view.layoutParams as RelativeLayout.LayoutParams
//                    layoutParams.leftMargin = x - xDelta
//                    layoutParams.topMargin = y - yDelta
//                    layoutParams.rightMargin = 0
//                    layoutParams.bottomMargin = 0
//                    view.layoutParams = layoutParams
//                }
//            }
//            // reflect the changes on screen
//            mainLayout.invalidate()
//            true
//        }
        if (documentRenderView!!.isSwipeVertical()) {
            y = pos
        } else {
            x = pos
        }
        invalidate()
    }


    private fun calculateMiddle() {
        val pos: Float
        val viewSize: Float
        val documentRenderViewSize: Float
        if (documentRenderView!!.isSwipeVertical()) {
            pos = y
            viewSize = height.toFloat()
            documentRenderViewSize = documentRenderView!!.getHeight().toFloat()
        } else {
            pos = x
            viewSize = width.toFloat()
            documentRenderViewSize = documentRenderView!!.getWidth().toFloat()
        }
        relativeHandlerMiddle = (pos + relativeHandlerMiddle) / documentRenderViewSize * viewSize
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!documentRenderView!!.isFree()) {
            return super.onTouchEvent(event)
        }
        documentRenderView?.apply {
            when (event!!.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    stopFling()
                    handler_.removeCallbacks(hidePageScrollerRunnable)
                    currentPos = if (isSwipeVertical()) {
                        event.rawY - y
                    } else {
                        event.rawX - x
                    }
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    currentPos = if (isSwipeVertical()) {
                        event.rawY - y
                    } else {
                        event.rawX - x
                    }
                    setPosition(currentPos)
                    setPositionOffset(currentPos,false)
                    return true
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
//                    hide(delayed = true)
//                    pdfView.performPageSnap() //todo we don't need that
                    return true
                }
            }

        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (isInEditMode) {
            return
        }
        canvas?.also { canvas ->
            documentRenderView?.apply {
                oval.left = 0F
                oval.top = 0F
                oval.right = widthScroller
                oval.bottom = heightScroller
                canvas.drawRoundRect(oval, ovalRX, ovalRY, scrollerBackgroundPaint)
                val halfHeightScroller = heightScroller.div(2)
                val lineContentDraw = resources.dpToPx(10)
                val lineDrawY = halfHeightScroller - (lineContentDraw.div(2))
                val lineLength = _2dp * 5
                val halfWidth = widthScroller.div(2)
                val lineDrawX = halfWidth - (lineLength.div(2))
                canvas.drawLine(
                    lineDrawX,
                    lineDrawY,
                    (lineLength + lineDrawX),
                    (lineDrawY),
                    ovalLinePaint
                )
                canvas.drawLine(
                    lineDrawX,
                    lineDrawY + (_2dp * 2),
                    (lineLength + lineDrawX),
                    lineDrawY + (_2dp * 2),
                    ovalLinePaint
                )
                canvas.drawLine(
                    lineDrawX,
                    lineDrawY + (_2dp * 4),
                    (lineLength + lineDrawX),
                    lineDrawY + (_2dp * 4),
                    ovalLinePaint
                )
            }
        }
    }
}