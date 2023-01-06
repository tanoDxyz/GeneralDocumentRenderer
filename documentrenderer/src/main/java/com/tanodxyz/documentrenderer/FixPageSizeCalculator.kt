package com.tanodxyz.documentrenderer

class FixPageSizeCalculator(
    override var viewSize: Size = Size(0,0),
    override var optimalMaxWidthPageSize: Size = viewSize,
    override var optimalMaxHeightPageSize: Size = viewSize
) : PageSizeCalculator {
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

    override fun reset() {
        viewSize = Size(0,0)
        optimalMaxHeightPageSize = viewSize
        optimalMaxWidthPageSize = viewSize
    }
}