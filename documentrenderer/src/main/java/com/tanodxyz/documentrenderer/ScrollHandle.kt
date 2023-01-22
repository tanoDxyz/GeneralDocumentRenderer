package com.tanodxyz.documentrenderer

interface ScrollHandle : ViewExtension {
    fun scroll(position: Float)

    /**
     * indicates total length
     *
     */
    fun getScrollerTotalLength(): Int
    val scrollBarWidth: Float
    val scrollBarHeight: Float
    var marginUsed: Float
}