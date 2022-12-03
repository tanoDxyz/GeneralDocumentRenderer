package com.tanodxyz.documentrenderer.document

import android.content.Context
import android.graphics.RectF
import android.os.Build
import com.tanodxyz.documentrenderer.*
import com.tanodxyz.documentrenderer.document.Document.Companion.PROPERTY_DOCUMENT_FIT_EACH_PAGE
import com.tanodxyz.documentrenderer.page.DocumentPage
import java.util.*

open class Document(context: Context) {
    protected var maxHeightPageSize = Size(0, 0)
    protected var maxWidthPageSize = Size(0, 0)
    protected val documentMeta = HashMap<String, Any?>()
    protected val originalDocumentPageData = mutableListOf<DocumentPage>()
    protected var originalMaxPageWidth = Size(0, 0)
    protected var originalMaxPageHeight = Size(0, 0)
    protected var contentLength = 0F
    var pageMargins: RectF = RectF(
        context.resources.dpToPx(4), // left margin
        context.resources.dpToPx(4), // top margin
        context.resources.dpToPx(4), // right margin
        context.resources.dpToPx(4) // bottom margin
    )
    var pageCorners: Float = 0.0F

    fun <T> get(property: String): T? {
        val propertyValue = documentMeta[property]
        return if (propertyValue != null) propertyValue as T else null
    }

    operator fun set(property: String, value: Any?) {
        documentMeta[property] = value
    }

    var documentName: String
        get() {
            return get<String>(PROPERTY_DOOCUMENT_NAME)
                ?: "$DOCUMENT_NAME-${System.currentTimeMillis()}"
        }
        set(value) {
            this[PROPERTY_DOOCUMENT_NAME] = value
        }


    var documentScrollStrategy: DocumentScrollStrategy
        get() {
            return get<DocumentScrollStrategy>(PROPERTY_DOOCUMENT_SCROLL_STRATEGY)
                ?: DocumentScrollStrategy.VERTICAL
        }
        set(value) {
            this[PROPERTY_DOOCUMENT_SCROLL_STRATEGY] = value
        }

    var documentFitPagePolicy: PageFitPolicy
        get() {
            return get<PageFitPolicy>(PROPERTY_DOOCUMENT_PAGE_FIT_POLICY) ?: PageFitPolicy.NONE
        }
        set(value) {
            this[PROPERTY_DOOCUMENT_PAGE_FIT_POLICY] = value
        }

    var swipeVertical: Boolean
        get() = get<Boolean>(PROPERTY_DOCUMENT_SWIPE_VERTICAL) ?: true
        set(value) {
            this[PROPERTY_DOCUMENT_SWIPE_VERTICAL] = value
        }
    var pageFling: Boolean
        get() = get<Boolean>(PROPERTY_DOCUMENT_PAGE_FLING) ?: false
        set(value) {
            this[PROPERTY_DOCUMENT_PAGE_FLING] = value
        }

    var documentViewMode: DocumentViewMode
        get() {
            return get<DocumentViewMode>(PROPERTY_DOOCUMENT_VIEW_MODE) ?: DocumentViewMode.DAY
        }
        set(value) {
            this[PROPERTY_DOOCUMENT_VIEW_MODE] = value
        }

    var documentPath: String?
        get() {
            return get<String>(PROPERTY_DOCUMENT_PATH)
        }
        set(value) {
            this[PROPERTY_DOCUMENT_PATH] = value
        }

    companion object {
        val PROPERTY_DOOCUMENT_NAME = "com.gdr.documentName"
        val PROPERTY_DOOCUMENT_SCROLL_STRATEGY = "com.gdr.documentScrollStrategy"
        val PROPERTY_DOOCUMENT_PAGE_FIT_POLICY = "com.gdr.documentPageFitPolicy"
        val PROPERTY_DOOCUMENT_VIEW_MODE = "com.gdr.documentViewMode"
        val PROPERTY_DOCUMENT_PATH = "com.gdr.documentPath"
        val PROPERTY_DOCUMENT_FIT_EACH_PAGE = "com.gdr.fiteachpage"
        val PROPERTY_DOCUMENT_SWIPE_VERTICAL = "com.gdr.swipeVertical"
        val PROPERTY_DOCUMENT_PAGE_FLING = "com.gdr.page.fling"
        const val DOCUMENT_NAME = "document-"
    }


    enum class DocumentScrollStrategy {
        HORIZONTAL, VERTICAL, CUSTOM;

        companion object {
            fun strategy(name: String?): DocumentScrollStrategy {
                return if (name.equals(HORIZONTAL.name, true)) {
                    HORIZONTAL
                } else if (name.equals(VERTICAL.name, true)) {
                    VERTICAL
                } else {
                    CUSTOM
                }
            }
        }
    }

    enum class PageFitPolicy {
        FIT_WIDTH, FIT_HEIGHT, BOTH, NONE;

        companion object {
            fun policy(name: String?): PageFitPolicy {
                return if (name.equals(FIT_WIDTH.name, true)) {
                    FIT_WIDTH
                } else if (name.equals(FIT_HEIGHT.name, true)) {
                    FIT_HEIGHT
                } else if (name.equals(BOTH.name, true)) {
                    BOTH
                } else {
                    NONE
                }
            }
        }
    }

    enum class DocumentViewMode {
        DAY, NIGHT;

        companion object {
            fun mode(name: String?): DocumentViewMode {
                return if (name.equals(DAY.name, true)) {
                    DAY
                } else if (name.equals(NIGHT.name, true)) {
                    NIGHT
                } else {
                    DAY
                }
            }
        }
    }


    fun getPageViaIndex(pageIndex: Int): DocumentPage? {
        return try {
            originalDocumentPageData.first { documentPage -> documentPage.index == pageIndex }
        } catch (ex: Exception) {
            null
        }
    }

    fun isCurrentPage(pageIndex: Int) {

    }


    fun getPageListIndex(pageIndex: Int): Int {
        for (i: Int in originalDocumentPageData.indices) {
            val documentPage = originalDocumentPageData[i]
            if (documentPage.index == pageIndex) {
                return i
            }
        }
        return -1
    }

    fun addPage(documentPage: DocumentPage) {
        originalDocumentPageData.add(documentPage)
    }

    fun addPages(documentPages: List<DocumentPage>) {
        originalDocumentPageData.addAll(documentPages)
    }

    fun deletePage(documentPage: DocumentPage) {
        originalDocumentPageData.remove(documentPage)
    }

    fun deletePage(deletePageIndex: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            originalDocumentPageData.removeIf { documentPage -> documentPage.index == deletePageIndex }
        } else {
            originalDocumentPageData.filter { documentPage -> documentPage.index == deletePageIndex }
                .apply {
                    if (this.isNotEmpty()) {
                        originalDocumentPageData.removeAll(this)
                    }
                }
        }
    }

    fun deletePages(range: IntRange) {
        originalDocumentPageData.removeAll(range getPagesViaPageIndexes originalDocumentPageData)
    }

    fun updatePage(documentPageIndex: Int, updatePage: DocumentPage): Boolean {
        val pageListIndex = getPageListIndex(documentPageIndex)
        val pageExistsForGivenIndex = pageListIndex > -1
        if (pageExistsForGivenIndex) {
            originalDocumentPageData[pageListIndex] = updatePage
        }
        return pageExistsForGivenIndex
    }

    fun setup(viewSize: Size) {
        originalDocumentPageData.forEach { documentPage ->
            val pageSize = documentPage.originalSize
            if (pageSize.width > originalMaxPageWidth.width) {
                originalMaxPageWidth = pageSize
            }
            if (pageSize.height > originalMaxPageHeight.height) {
                originalMaxPageHeight = pageSize
            }
        }
        recalculatePageSizes(viewSize)
    }

    open fun getMaxPageSize(): Size {
        return if (swipeVertical) maxWidthPageSize else maxHeightPageSize
    }

    open fun getMaxPageWidth(): Int {
        return getMaxPageSize().width
    }

    open fun getMaxPageHeight(): Int {
        return getMaxPageSize().height
    }

    open fun recalculatePageSizes(viewSize: Size) {
        val calculator = DefaultPageSizeCalculator(
            documentFitPagePolicy, originalMaxPageWidth,
            originalMaxPageHeight, viewSize, get<Boolean>(PROPERTY_DOCUMENT_FIT_EACH_PAGE) ?: false
        )
        maxWidthPageSize = calculator.optimalMaxWidthPageSize
        maxHeightPageSize = calculator.optimalMaxHeightPageSize
        contentLength = 0F
        originalDocumentPageData.forEach { documentPage ->
            documentPage.size = calculator.calculate(documentPage.originalSize)
            if (documentPage.size.width > maxWidthPageSize.width) {
                maxWidthPageSize.width = documentPage.size.width
            }
            if (documentPage.size.height > maxHeightPageSize.height) {
                maxHeightPageSize.height = documentPage.size.height
            }
            contentLength += if (this.swipeVertical) {
                documentPage.size.height
            } else {
                documentPage.size.width
            }
        }
    }

    fun getDocLen(zoom: Float): Float {
        return getTotalContentLength() * zoom
    }

    inner class DocumentPageIterator : Iterable<DocumentPage> {
        private var nextElementIndex = 0
        override fun iterator(): Iterator<DocumentPage> {
            return object : Iterator<DocumentPage> {
                override fun hasNext(): Boolean {
                    return (nextElementIndex + 1) < getDocumentPages().count()
                }

                override fun next(): DocumentPage {
                    val documentPages = getDocumentPages()
                    val documentPage = documentPages[nextElementIndex]
                    nextElementIndex++
                    return documentPage
                }
            }
        }
    }


    fun getTotalContentLength(): Float = contentLength
    fun getDocumentPages(): List<DocumentPage> = originalDocumentPageData
    fun documentPageIterator(): DocumentPageIterator = DocumentPageIterator()
    fun haveNoPages(): Boolean = getDocumentPages().isEmpty()
    fun getPagesCount(): Int = getDocumentPages().count()

}