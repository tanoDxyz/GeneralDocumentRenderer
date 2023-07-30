package com.tanodxyz.documentrenderer.pagesizecalculator

import android.util.DisplayMetrics
import com.tanodxyz.documentrenderer.Size

/**
 * The calculation algorithm is same as that of [FixPageSizeCalculator] with one major difference that
 * instead of View size for max size check - Screen size is used.
 */
class UnBoundedPageSizeCalculator:PageSizeCalculator() {

    override var viewSize: Size = Size(0, 0)
    override var optimalMaxWidthPageSize: Size = Size(0, 0)
    override var optimalMaxHeightPageSize: Size = Size(0, 0)

    override fun setup(args: HashMap<String, Any>) {
        val displayMetrics = args[DISPLAY_METRICS] as DisplayMetrics
        viewSize = Size(displayMetrics.widthPixels,displayMetrics.heightPixels)
        optimalMaxWidthPageSize = viewSize
        optimalMaxHeightPageSize = viewSize
    }

    override fun calculate(pageSize: Size): Size {
        if (pageSize.width <= 0 || pageSize.height <= 0) {
            return viewSize
        }
        val maxWidth = viewSize.width
        val maxHeight = viewSize.height

        val w = if (pageSize.width > maxWidth) {
            maxWidth
        } else {
            pageSize.width
        }
        val h = if (pageSize.height > maxHeight) {
            maxHeight
        } else {
            pageSize.height
        }
        return Size(w, h)
    }

    override fun calculateElementSizeRelative(size: Size): Size {
        return calculate(size)
    }
}