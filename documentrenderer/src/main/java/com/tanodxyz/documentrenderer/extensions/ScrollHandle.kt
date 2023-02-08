package com.tanodxyz.documentrenderer.extensions

interface ScrollHandle : ViewExtension {
    fun scroll(position: Float)
    fun getScrollerTotalLength(): Int
    val scrollBarWidth: Float
    val scrollBarHeight: Float
    var marginUsed: Float
}