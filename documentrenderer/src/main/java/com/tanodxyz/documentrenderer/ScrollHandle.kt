package com.tanodxyz.documentrenderer

interface ScrollHandle : ViewExtension {
    fun scroll(position: Float)
    fun setPageNumber(pageNumber: String)
    fun getScrollerHeight(): Float
    fun getScrollerWidth(): Float
    fun getMarginFromParent(): Float
}