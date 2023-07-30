package com.tanodxyz.documentrenderer.extensions

/**
 * A scroller is shown on the right side in portrait mode and at the bottom in landscape mode.
 */
interface ScrollHandle : ViewExtension {
    /**
     * Will be called by [com.tanodxyz.documentrenderer.DocumentRenderView].
     * @param position checkout for the algorithm used in calculating this value [com.tanodxyz.documentrenderer.DocumentRenderView.getPositionOffset]
     */
    fun scroll(position: Float)

    /**
     * It is the total length of scroll bar not just the scroll button.
     * it is different for both vertical and horizontal modes.
     * @see [DefaultScrollHandle]
     */
    fun getScrollerTotalLength(): Int

    val scrollButtonWidth: Float
    val scrollButtonHeight: Float

    /**
     * Margins in vertical mode for the following sides [top,bottom,end]
     * Margins in horizontal mode for the following sides [bottom,start,end]
     */
    var marginUsed: Float
}