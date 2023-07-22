package com.tanodxyz.documentrenderer

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Looper
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import com.tanodxyz.documentrenderer.events.GenericMotionEvent
import com.tanodxyz.documentrenderer.events.IMotionEventMarker
import com.tanodxyz.documentrenderer.page.DocumentPage
import com.tanodxyz.documentrenderer.page.ObjectViewState
import java.io.Closeable
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs


fun Resources.dpToPx(dp: Int): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        displayMetrics
    )
}

fun Resources.pdfPointToPixel(points: Int): Float {
    val inches = points / 72F
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_IN,
        inches,
        displayMetrics
    )
}

fun Resources.spToPx(sp: Number): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        sp.toFloat(),
        displayMetrics
    )
}

fun Resources.pxToSp(pixels: Number): Float {
    return pixels.toFloat() / displayMetrics.scaledDensity
}

fun Resources.screenWidth() {
    this.displayMetrics.widthPixels
}

object ViewIdGenerator {
    private val sNextGeneratedId: AtomicInteger = AtomicInteger(1)

    @SuppressLint("NewApi")
    fun generateViewId(): Int {
        return View.generateViewId()
    }
}

fun getPageViewState(pageBounds: RectF, viewSize: Size, swipeVertical: Boolean): ObjectViewState {
    val viewBounds = RectF(0F, 0F, viewSize.width.toFloat(), viewSize.height.toFloat())
    val viewBoundsRelativeToPageBounds =
        RectF(pageBounds.left, pageBounds.top, viewSize.width.toFloat(), viewSize.height.toFloat())
    val pageIsTotallyVisible = viewBounds.contains(pageBounds)
    var pageIsPartiallyVisible = false
    if (!pageIsTotallyVisible) {
        val pageAndViewIntersected = viewBoundsRelativeToPageBounds.intersects(
            pageBounds.left,
            pageBounds.top,
            pageBounds.right,
            pageBounds.bottom
        )
        pageIsPartiallyVisible = if (pageAndViewIntersected) {
            if (swipeVertical) {
                if (pageBounds.top < viewBounds.top) {
                    pageBounds.bottom >= viewBounds.top
                } else {
                    pageBounds.top <= viewBounds.bottom
                }
            } else {
                if (pageBounds.left < viewBounds.left) {
                    pageBounds.right >= viewBounds.left
                } else {
                    pageBounds.left <= viewBounds.right
                }
            }
        } else {
            false
        }
    }
    return if (pageIsTotallyVisible) ObjectViewState.VISIBLE
    else if (pageIsPartiallyVisible) ObjectViewState.PARTIALLY_VISIBLE else ObjectViewState.INVISIBLE
}

infix fun IntRange.getPagesViaPageIndexes(pageData: MutableList<DocumentPage>): MutableList<DocumentPage> {
    val pages = mutableListOf<DocumentPage>()
    this.forEach { pageIndex ->
        pages.addAll(pageData.filter { page -> page.uniqueId == pageIndex })
    }
    return pages
}


data class Size(var width: Int, var height: Int) {}
data class SizeF(var width: Float, var height: Float)

fun RectF.reset() {
    top = 0F
    left = 0F
    right = 0F
    bottom = 0F
}

fun RectF.getWidth(): Float {
    val pageBounds = this
    return if (pageBounds.left > 0 && pageBounds.right >= pageBounds.left) {
        pageBounds.width()
    } else if (pageBounds.left < 0 && pageBounds.right < 0) {
        abs(pageBounds.right) - abs(pageBounds.left)
    } else if (pageBounds.left < 0 && pageBounds.right > 0) {
        pageBounds.right + abs(pageBounds.left)
    } else {
        pageBounds.width()
    }
}

fun RectF.getHeight(): Float {
    val bounds = this
    return if (bounds.top > 0 && bounds.bottom >= bounds.top) {
        bounds.height()
    } else if (bounds.top < 0 && bounds.bottom < 0) {
        abs(bounds.bottom) - abs(bounds.top)
    } else if (bounds.top < 0 && bounds.bottom > 0) {
        bounds.bottom + abs(bounds.top)
    } else {
        bounds.width()
    }
}

fun Pair<Float, Float>.getHeight(): Float {
    return RectF(0F, this.first, 0F, this.second).getHeight()
}

fun Pair<Float, Float>.getWidth(): Float {
    return RectF(this.first, 0F, this.second, 0F).getWidth()
}

/**
 * By this we mean that at least one pointer is active.
 */
fun IMotionEventMarker?.isEventFinishedOrCanceled(): Boolean {
    return when (this) {
        is GenericMotionEvent -> !hasNoMotionEvent() && motionEvent?.action == MotionEvent.ACTION_UP || motionEvent?.action == MotionEvent.ACTION_CANCEL
        else -> false
    }
}


fun Bitmap?.recycleSafely() {
    kotlin.runCatching {
        this?.recycle()
    }
}

fun Closeable?.closeResource() {
    try {
        this?.close()
    } catch (ex: java.lang.Exception) {
        ex.printStackTrace()
    }
}

fun isMainThread(): Boolean = Looper.myLooper() == Looper.getMainLooper()

fun RectF.copy():RectF {
    return RectF(this.left,this.top,this.right,this.bottom)
}

fun IMotionEventMarker?.hasGenericMotionEvent():Boolean {
    return if(this == null) {
        false
    } else {
        !this.hasNoMotionEvent()
    }
}

