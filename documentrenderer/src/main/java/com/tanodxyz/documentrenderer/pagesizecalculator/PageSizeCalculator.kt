package com.tanodxyz.documentrenderer.pagesizecalculator

import com.tanodxyz.documentrenderer.Size
import com.tanodxyz.documentrenderer.document.Document

/**
 * This class helps in calculating page sizes based on specific nature of it's implementation.
 */
abstract class PageSizeCalculator {
    abstract fun setup(args: HashMap<String, Any>)
    abstract fun calculate(pageSize: Size): Size
    abstract fun calculateElementSizeRelative(size: Size): Size
    open lateinit var optimalMaxWidthPageSize: Size
    open lateinit var optimalMaxHeightPageSize: Size
    open lateinit var viewSize: Size
    open fun reset() {}

    companion object {
        const val DISPLAY_METRICS: String = "com.displayMetrics"
        const val FIT_POLICY = Document.PROPERTY_DOCUMENT_PAGE_FIT_POLICY
        const val MAX_WIDTH_PAGE_SIZE = "com.maxWidthSize"
        const val MAX_HEIGHT_PAGE_SIZE = "com.maxHeightSize"
        const val VIEW_SIZE = "com.viewSize"
        const val FIT_EACH_PAGE = Document.PROPERTY_DOCUMENT_FIT_EACH_PAGE
    }

    fun Size.ensureValuesAreNonZero(): Size {
        if (this.width <= 0) {
            this.width = 1
        }
        if (this.height <= 0) {
            this.height = 1
        }
        return this
    }
}