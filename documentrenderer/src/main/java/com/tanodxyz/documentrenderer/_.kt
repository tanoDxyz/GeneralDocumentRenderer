package com.tanodxyz.documentrenderer

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import com.tanodxyz.documentrenderer.page.DocumentPage
import kotlin.math.roundToInt


fun Resources.dpToPx(dp: Int): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        displayMetrics
    )
}

fun Resources.screenWidth() {
    this.displayMetrics.widthPixels
}

infix fun IntRange.getPagesViaPageIndexes(pageData: MutableList<DocumentPage>): MutableList<DocumentPage> {
    val pages = mutableListOf<DocumentPage>()
    this.forEach { pageIndex ->
        pages.addAll(pageData.filter { page -> page.index == pageIndex })
    }
    return pages
}


data class Size(var width: Int, var height: Int)
data class SizeF(var width: Float, var height: Float)

