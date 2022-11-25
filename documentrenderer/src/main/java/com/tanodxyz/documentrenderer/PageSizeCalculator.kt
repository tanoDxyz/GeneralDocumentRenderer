package com.tanodxyz.documentrenderer

interface PageSizeCalculator {
    fun calculate(pageSize: Size): Size
    val optimalMaxWidthPageSize:Size
    val optimalMaxHeightPageSize: Size
}