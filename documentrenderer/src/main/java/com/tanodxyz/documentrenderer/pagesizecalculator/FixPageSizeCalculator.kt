package com.tanodxyz.documentrenderer.pagesizecalculator

import com.tanodxyz.documentrenderer.Size

class FixPageSizeCalculator : PageSizeCalculator() {

    override var viewSize: Size = Size(0, 0)
    override var optimalMaxWidthPageSize: Size = Size(0, 0)
    override var optimalMaxHeightPageSize: Size = Size(0, 0)


    override fun setup(args: HashMap<String, Any>) {
        viewSize = args[VIEW_SIZE] as Size
        viewSize.ensureValuesAreNonZero()
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