package com.tanodxyz.documentrenderer

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.RectF
import android.os.Build
import android.util.TypedValue
import android.view.View
import com.tanodxyz.documentrenderer.page.DocumentPage
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs


fun Resources.dpToPx(dp: Int): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        displayMetrics
    )
}

fun Resources.spToPx(sp: Int): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        sp.toFloat(),
        displayMetrics
    )
}

fun Resources.screenWidth() {
    this.displayMetrics.widthPixels
}

/**
 * Limits the given **number** between the other values
 * @param number  The number to limit.
 * @param between The smallest value the number can take.
 * @param and     The biggest value the number can take.
 * @return The limited number.
 */
fun limit(number: kotlin.Float, between: kotlin.Float, and: kotlin.Float): kotlin.Float {
    if (number <= between) {
        return between
    }
    return if (number >= and) {
        and
    } else number
}


object ViewIdGenerator {
    private val sNextGeneratedId: AtomicInteger = AtomicInteger(1)
    @SuppressLint("NewApi")
    fun generateViewId(): Int {
        if (Build.VERSION.SDK_INT < 17) {
            while (true) {
                val result: Int = sNextGeneratedId.get()
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                var newValue = result + 1
                if (newValue > 0x00FFFFFF) newValue = 1 // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result
                }
            }
        } else {
            return View.generateViewId()
        }
    }
}


infix fun IntRange.getPagesViaPageIndexes(pageData: MutableList<DocumentPage>): MutableList<DocumentPage> {
    val pages = mutableListOf<DocumentPage>()
    this.forEach { pageIndex ->
        pages.addAll(pageData.filter { page -> page.uniquieID == pageIndex })
    }
    return pages
}


data class Size(var width: Int, var height: Int)
data class SizeF(var width: Float, var height: Float)


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
    println("poi: pageBounds are $this")
    val pageBounds = this
    return if (pageBounds.top > 0 && pageBounds.bottom >= pageBounds.top) {
        pageBounds.height()
    } else if (pageBounds.top < 0 && pageBounds.bottom < 0) {
        abs(pageBounds.bottom) - abs(pageBounds.top)
    } else if (pageBounds.top < 0 && pageBounds.bottom > 0) {
        pageBounds.bottom + abs(pageBounds.top)
    } else {
        pageBounds.width()
    }
}

fun Pair<Float, Float>.getHeight(): Float {
    return RectF(0F, this.first, 0F, this.second).getHeight()
}

fun Pair<Float, Float>.getWidth(): Float {
    return RectF(this.first, 0F, this.second, 0F).getWidth()
}

