package com.tanodxyz.documentrenderer.document

import android.content.Context
import android.graphics.Color
import android.graphics.RectF
import com.tanodxyz.documentrenderer.*
import com.tanodxyz.documentrenderer.page.DocumentPage
import java.lang.Exception
import java.util.*
import kotlin.math.min
import kotlin.math.roundToInt

open class Document(context: Context, var pageSizeCalculator: PageSizeCalculator? = null) {
    protected var maxHeightPageSize = Size(0, 0)
    protected var maxWidthPageSize = Size(0, 0)
    protected val documentMeta = HashMap<String, Any?>()
    protected val originalDocumentPageData = mutableListOf<DocumentPage>()
    protected var originalMaxPageWidth = Size(0, 0)
    protected var originalMaxPageHeight = Size(0, 0)
    protected var contentLength = 0F
    private var pagesToBeDrawn: MutableList<DocumentPage> = mutableListOf()

    var pageMargins: RectF = RectF(
        context.resources.dpToPx(2), // left margin
        context.resources.dpToPx(2), // top margin
        context.resources.dpToPx(2), // right margin
        context.resources.dpToPx(2) // bottom margin
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
            return get<String>(PROPERTY_DOCUMENT_NAME)
                ?: "$DOCUMENT_NAME-${System.currentTimeMillis()}"
        }
        set(value) {
            this[PROPERTY_DOCUMENT_NAME] = value
        }


    var documentFitPagePolicy: PageFitPolicy
        get() {
            return get<PageFitPolicy>(PROPERTY_DOCUMENT_PAGE_FIT_POLICY) ?: PageFitPolicy.NONE
        }
        set(value) {
            this[PROPERTY_DOCUMENT_PAGE_FIT_POLICY] = value
        }

    var swipeVertical: Boolean
        get() = get<Boolean>(PROPERTY_DOCUMENT_SWIPE_VERTICAL) ?: true
        set(value) {
            this[PROPERTY_DOCUMENT_SWIPE_VERTICAL] = value
        }

    var nightMode: Boolean
        get() = get<Boolean>(PROPERTY_NIGHT_MODE) ?: false
        set(value) {
            this[PROPERTY_NIGHT_MODE] = value
        }

    var pageBackColor: Int
        get() = get<Int>(PROPERTY_PAGE_BACK_COLOR) ?: Color.WHITE
        set(value) {
            this[PROPERTY_PAGE_BACK_COLOR] = value
        }

    var pageFling: Boolean
        get() = get<Boolean>(PROPERTY_DOCUMENT_PAGE_FLING) ?: false
        set(value) {
            this[PROPERTY_DOCUMENT_PAGE_FLING] = value
        }

    var documentViewMode: DocumentViewMode
        get() {
            return get<DocumentViewMode>(PROPERTY_DOCUMENT_VIEW_MODE) ?: DocumentViewMode.DAY
        }
        set(value) {
            this[PROPERTY_DOCUMENT_VIEW_MODE] = value
        }

    companion object {
        const val PROPERTY_DOCUMENT_NAME = "com.gdr.documentName"
        const val PROPERTY_DOCUMENT_PAGE_FIT_POLICY = "com.gdr.documentPageFitPolicy"
        const val PROPERTY_DOCUMENT_VIEW_MODE = "com.gdr.documentViewMode"
        const val PROPERTY_DOCUMENT_FIT_EACH_PAGE = "com.gdr.fiteachpage"
        const val PROPERTY_DOCUMENT_SWIPE_VERTICAL = "com.gdr.swipeVertical"
        const val PROPERTY_DOCUMENT_PAGE_FLING = "com.gdr.page.fling"
        const val PROPERTY_NIGHT_MODE = "com.night.mode"
        const val PROPERTY_PAGE_BACK_COLOR = "com.page.back.color"
        const val DOCUMENT_NAME = "document-"

        const val MAX_PAGES_DRAWN = 10
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


    //    fun getPageViaIndex(pageIndex: Int): DocumentPage? {
//        return try {
//            originalDocumentPageData.first { documentPage -> documentPage.uniquieID == pageIndex }
//        } catch (ex: Exception) {
//            null
//        }
//    }
//
//
//
//    fun getPageListIndex(pageIndex: Int): Int {
//        for (i: Int in originalDocumentPageData.indices) {
//            val documentPage = originalDocumentPageData[i]
//            if (documentPage.uniquieID == pageIndex) {
//                return i
//            }
//        }
//        return -1
//    }
//
    fun addPage(documentPage: DocumentPage) {
        originalDocumentPageData.add(documentPage)
    }

    fun addPages(documentPages: List<DocumentPage>) {
        originalDocumentPageData.addAll(documentPages)
    }
//
//    fun deletePage(documentPage: DocumentPage) {
//        originalDocumentPageData.remove(documentPage)
//    }
//
//    fun deletePage(deletePageIndex: Int) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            originalDocumentPageData.removeIf { documentPage -> documentPage.uniquieID == deletePageIndex }
//        } else {
//            originalDocumentPageData.filter { documentPage -> documentPage.uniquieID == deletePageIndex }
//                .apply {
//                    if (this.isNotEmpty()) {
//                        originalDocumentPageData.removeAll(this)
//                    }
//                }
//        }
//    }
//
//    fun deletePages(range: IntRange) {
//        originalDocumentPageData.removeAll(range getPagesViaPageIndexes originalDocumentPageData)
//    }
//
//    fun updatePage(documentPageIndex: Int, updatePage: DocumentPage): Boolean {
//        val pageListIndex = getPageListIndex(documentPageIndex)
//        val pageExistsForGivenIndex = pageListIndex > -1
//        if (pageExistsForGivenIndex) {
//            originalDocumentPageData[pageListIndex] = updatePage
//        }
//        return pageExistsForGivenIndex
//    }

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
        if (pageSizeCalculator == null) {
            pageSizeCalculator = DefaultPageSizeCalculator()
        }

        val props = hashMapOf<String, Any>().apply {
            put(PageSizeCalculator.FIT_POLICY, documentFitPagePolicy)
            put(PageSizeCalculator.MAX_WIDTH_PAGE_SIZE, originalMaxPageWidth)
            put(PageSizeCalculator.MAX_HEIGHT_PAGE_SIZE, originalMaxPageHeight)
            put(PageSizeCalculator.VIEW_SIZE, viewSize)
            put(
                PageSizeCalculator.FIT_EACH_PAGE,
                get<Boolean>(PROPERTY_DOCUMENT_FIT_EACH_PAGE) ?: false
            )
        }
        pageSizeCalculator!!.setup(props)
        maxWidthPageSize = pageSizeCalculator!!.optimalMaxWidthPageSize
        maxHeightPageSize = pageSizeCalculator!!.optimalMaxHeightPageSize

        contentLength = 0F
        originalDocumentPageData.forEach { documentPage ->
            documentPage.size = pageSizeCalculator!!.calculate(documentPage.originalSize)
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
            documentPage.resetPageBounds()
        }

    }

    open fun toCurrentScale(size: Number, zoom: Float): Float {
        return size.toFloat() * zoom
    }


    fun getDocLen(zoom: Float): Float {
        return getTotalContentLength() * zoom
    }

//    fun documentPageIterator(): DocumentPageIterator = DocumentPageIterator()
//
//    inner class DocumentPageIterator : Iterable<DocumentPage> {
//        private var nextElementIndex = 0
//        override fun iterator(): Iterator<DocumentPage> {
//            return object : Iterator<DocumentPage> {
//                override fun hasNext(): Boolean {
//                    return (nextElementIndex + 1) < getDocumentPages().count()
//                }
//
//                override fun next(): DocumentPage {
//                    val documentPages = getDocumentPages()
//                    val documentPage = documentPages[nextElementIndex]
//                    nextElementIndex++
//                    return documentPage
//                }
//            }
//        }
//    }


    fun getTotalContentLength(): Float = contentLength
    fun getDocumentPages(): List<DocumentPage> = originalDocumentPageData
    fun haveNoPages(): Boolean = getDocumentPages().isEmpty()
    fun getPagesCount(): Int = getDocumentPages().count()
    fun getPage(pageNumber: Int): DocumentPage? {
        return if (pageNumber < 0 || pageNumber >= getPagesCount()) {
            null
        } else {
            originalDocumentPageData[pageNumber]
        }
    }
}