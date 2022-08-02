package com.tanodxyz.documentrenderer

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import kotlin.math.roundToInt


fun Resources.dpToPx(dp: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        displayMetrics
    ).roundToInt()
}

fun Resources.screenWidth() {
    this.displayMetrics.widthPixels
}
