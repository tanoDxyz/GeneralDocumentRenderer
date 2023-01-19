package com.tanodxyz.documentrenderer

interface ScrollHandle : ViewExtension {
    fun scroll(position: Float)
    fun getScrollerHeight(): Float
    fun getScrollerWidth(): Float
    fun getMarginFromParent(): Float
}