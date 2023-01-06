package com.tanodxyz.documentrenderer

interface PageSizeCalculator {
    fun calculate(pageSize: Size): Size
    var optimalMaxWidthPageSize:Size
    var optimalMaxHeightPageSize: Size
    var viewSize:Size
    fun reset(){}
}