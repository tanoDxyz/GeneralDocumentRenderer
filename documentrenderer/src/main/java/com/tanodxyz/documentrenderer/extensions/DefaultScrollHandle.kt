package com.tanodxyz.documentrenderer.extensions

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
import android.view.MotionEvent.*
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.DocumentRenderView.Companion.SCROLL_HANDLE_AND_PAGE_DISPLAY_BOX_HIDE_DELAY_MILLI_SECONDS
import com.tanodxyz.documentrenderer.dpToPx
import kotlin.math.roundToInt

/**
 * Scroll Button that can be used to fast-forward or backward [com.tanodxyz.documentrenderer.document.Document] pages
 * inside [DocumentRenderView].
 *
 * This implementation works as follows.
 * >
 * In [DocumentRenderView] vertical swipe mode
 * the total length or height of scroller is [DocumentRenderView.getHeight].
 *
 * In [DocumentRenderView] horizontal swipe mode
 *  the total length or width of scroller is [DocumentRenderView.getWidth]
 *
 *  inside this length there is a scroll button that user can touch and move vertical or horizontal
 *  depending on the [DocumentRenderView]s swipe mode.
 *
 *
 */

open class DefaultScrollHandle @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ScrollHandle, View(context, attrs, defStyleAttr) {

    private var doOnNextLayout: (() -> Unit)? = null
    private var touched = false
    val DEFAULT_WIDTH = context.resources.dpToPx(24)
    val DEFAULT_HEIGHT = context.resources.dpToPx(35)
    private var documentRenderView: DocumentRenderView? = null
    var scrollColor = Color.parseColor("#DCDCDC")
    var ovalLineColor = Color.parseColor("#6082B6")
    var heightScroller = 0F
    var widthScroller = 0F
    private val _2dp = context.resources.dpToPx(2)
    override var marginUsed = context.resources.dpToPx(16)
    private var drawOffset: Float = 0F

    val scrollerBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = scrollColor
        style = Paint.Style.FILL_AND_STROKE
        strokeCap = Paint.Cap.ROUND
    }
    val ovalLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ovalLineColor
        style = Paint.Style.FILL_AND_STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = context.resources.dpToPx(1)
    }
    private val oval = RectF(0F, 0F, 0F, 0F)

    var ovalRX = context.resources.dpToPx(100)
    var ovalRY = context.resources.dpToPx(100)
    private val handler_ = Handler(Looper.getMainLooper())
    private val hidePageScrollerRunnable = Runnable { hide(delayed = false) }


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        doOnNextLayout?.invoke()
    }

    override fun attachTo(view: DocumentRenderView, onLayout: (() -> Unit)?) {
        view.removeView(this)
        val margins = marginUsed.roundToInt()
        val layoutParamsForThisView = if (view.isSwipeVertical()) {
            widthScroller = DEFAULT_WIDTH
            heightScroller = DEFAULT_HEIGHT
            FrameLayout.LayoutParams(
                widthScroller.roundToInt(), ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                gravity = Gravity.END
                topMargin = margins
                bottomMargin = margins
                marginEnd = margins
            }
        } else {
            widthScroller = DEFAULT_HEIGHT
            heightScroller = DEFAULT_WIDTH
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, heightScroller.roundToInt()
            ).apply {
                gravity = Gravity.BOTTOM
                bottomMargin = margins
                marginEnd = margins
                marginStart = margins
            }
        }
        layoutParams = layoutParamsForThisView
        this.doOnNextLayout = onLayout
        view.addView(this)
        this.documentRenderView = view
        alpha = 0.6F
    }

    override fun detach(view: DocumentRenderView) {
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
            setPosition(position)
        }
    }


    override fun getScrollerTotalLength(): Int {
        return if (documentRenderView!!.isSwipeVertical()) {
            (measuredHeight - heightScroller).roundToInt()
        } else {
            (measuredWidth - widthScroller).roundToInt()
        }
    }

    override val scrollButtonWidth: Float
        get() = widthScroller

    override val scrollButtonHeight: Float
        get() = heightScroller


    override fun show() {
        visibility = VISIBLE
    }

    override fun hide(delayed: Boolean) {
        if (delayed) {
            handler_.postDelayed(
                hidePageScrollerRunnable,
                SCROLL_HANDLE_AND_PAGE_DISPLAY_BOX_HIDE_DELAY_MILLI_SECONDS
            )
        } else {
            visibility = INVISIBLE
        }
    }

    private fun setPosition(pos: Float) {
        val swipeVertical = documentRenderView!!.isSwipeVertical()
        if (swipeVertical) {
            val verticalTopPosition = 0F
            val verticalBottomPosition = this@DefaultScrollHandle.measuredHeight - (heightScroller)
            val maxHeight = (verticalBottomPosition - verticalTopPosition)
            drawOffset = if (pos <= verticalTopPosition) {
                verticalTopPosition
            } else if ((pos) >= maxHeight) {
                maxHeight
            } else {
                pos
            }
        } else {
            val horizontalLeftPosition = 0F
            val horizontalRightPosition = this@DefaultScrollHandle.measuredWidth - (widthScroller)
            val maxWidth = (horizontalRightPosition - horizontalLeftPosition)
            drawOffset = if (pos <= horizontalLeftPosition) {
                horizontalLeftPosition
            } else if (pos >= maxWidth) {
                maxWidth
            } else {
                pos
            }
        }
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!documentRenderView!!.isFree() || documentRenderView!!.documentFitsView()) {
            return super.onTouchEvent(event)
        }
        documentRenderView?.apply {

            val x = event!!.x
            val y = event.y
            when (event.action) {
                ACTION_DOWN, ACTION_POINTER_DOWN -> {
                    stopFling()
                    handler_.removeCallbacks(hidePageScrollerRunnable)
                    touched = oval.contains(x, y)
                    return touched
                }

                ACTION_MOVE -> {
                    if (touched) {
                        val currentPos = if (isSwipeVertical()) {
                            y
                        } else {
                            x
                        }
                        setPosition(currentPos)
                        setPositionOffset(drawOffset, false)
                    }
                    return true
                }

                ACTION_CANCEL, ACTION_UP, ACTION_POINTER_UP -> {
                    touched = false
                    redraw()

                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isInEditMode) {
            return
        }
        canvas.apply {
            documentRenderView?.apply {
                val swipeVertical = isSwipeVertical()
                oval.left = 0F
                oval.top = 0F
                oval.right = widthScroller
                oval.bottom = heightScroller
                if (swipeVertical) {
                    oval.top = drawOffset
                    oval.bottom += drawOffset
                } else {
                    oval.left = drawOffset
                    oval.right += drawOffset
                }
                canvas.drawRoundRect(oval, ovalRX, ovalRY, scrollerBackgroundPaint)
                val halfHeightScroller = heightScroller.div(2)
                val lineContentDraw = resources.dpToPx(7)
                var lineDrawY = halfHeightScroller - (lineContentDraw.div(2))
                if (swipeVertical) {
                    lineDrawY += drawOffset
                }
                val lineLength = _2dp * 5
                val halfWidth = widthScroller.div(2)
                var lineDrawX = halfWidth - (lineLength.div(2))
                if (!swipeVertical) {
                    lineDrawX += drawOffset
                }
                canvas.drawLine(
                    lineDrawX, lineDrawY, (lineLength + lineDrawX), (lineDrawY), ovalLinePaint
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