package com.tanodxyz.documentrenderer

class FixPageSizeCalculator(val viewSize: Size) {
    fun calculate(pageSize: Size): Size {
        if (pageSize.width <= 0 || pageSize.height <= 0) {
            return viewSize
        }
        val maxWidth = viewSize.width
        val maxHeight = viewSize.height

        val w = if(pageSize.width > maxWidth) {
            maxWidth
        } else {
            pageSize.width
        }
        val h = if(pageSize.height > maxHeight) {
            maxHeight
        } else {
            pageSize.height
        }
        return Size(w,h)
    }

    val maxWidthPageSize = viewSize
    val maxHeightPageSize = viewSize
}