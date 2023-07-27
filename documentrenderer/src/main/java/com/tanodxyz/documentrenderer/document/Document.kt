package com.tanodxyz.documentrenderer.document

import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.graphics.RectF
import com.tanodxyz.documentrenderer.*
import com.tanodxyz.documentrenderer.page.DocumentPage
import com.tanodxyz.documentrenderer.pagesizecalculator.DefaultPageSizeCalculator
import com.tanodxyz.documentrenderer.pagesizecalculator.PageSizeCalculator
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

/**
 * As the name suggests
 * This class encapsulates set of data and operations required for [DocumentRenderView] to render pages.
 */
open class Document(context: Context, var pageSizeCalculator: PageSizeCalculator? = null) {
    protected var maxHeightPageSize = Size(0, 0)
    protected var maxWidthPageSize = Size(0, 0)
    protected val documentMeta = HashMap<String, Any?>()
    protected val documentPageData = mutableListOf<DocumentPage>()
    protected var originalMaxPageWidth = Size(0, 0)
    protected var originalMaxPageHeight = Size(0, 0)
    protected var contentLength = 0F

    /**
     * these page indexes are used in various calculations.
     */
    internal lateinit var pageIndexes: MutableList<PointF>

    var pageMargins: RectF = RectF(
        context.resources.dpToPx(2), // left margin
        context.resources.dpToPx(2), // top margin
        context.resources.dpToPx(2), // right margin
        context.resources.dpToPx(2) // bottom margin
    )

    /**
     * used to round the corner of pages.
     */
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

    /**
     * Used by [PageSizeCalculator] to size or shape the pages accordingly.
     * see [PageFitPolicy]
     */
    var documentFitPagePolicy: PageFitPolicy
        get() {
            return get<PageFitPolicy>(PROPERTY_DOCUMENT_PAGE_FIT_POLICY) ?: PageFitPolicy.NONE
        }
        set(value) {
            this[PROPERTY_DOCUMENT_PAGE_FIT_POLICY] = value
        }

    /**
     * Indicates that pages will swipe vertically.
     */
    var swipeVertical: Boolean
        get() = get<Boolean>(PROPERTY_DOCUMENT_SWIPE_VERTICAL) ?: true
        set(value) {
            this[PROPERTY_DOCUMENT_SWIPE_VERTICAL] = value
        }

    /**
     * If night mode is active. it's user's responsibility to design [com.tanodxyz.documentrenderer.elements.InteractiveElement]
     * in such a way that each individual element changes its visual appearance.
     */
    var nightMode: Boolean
        get() = get<Boolean>(PROPERTY_NIGHT_MODE) ?: false
        set(value) {
            this[PROPERTY_NIGHT_MODE] = value
        }

    /**
     * It is the page back color when no content or elements are rendered onto it.
     * it is white and black respectively for day and night modes.
     */
    var pageBackColor: Int
        get() = get<Int>(PROPERTY_PAGE_BACK_COLOR) ?: Color.WHITE
        set(value) {
            this[PROPERTY_PAGE_BACK_COLOR] = value
        }

    /**
     * This property tells [DocumentRenderView] that whenever fling is detected.
     * swipe single page only.
     * if the page is scaled or zoomed - in that case page fling will cause the page
     * x,y bounds to move to the edge of the page.
     *
     * so it is summarised as follows.
     * when page is completely visible any fling will cause the page to go off-screen completely (swipe)
     * but if the page is partially visible (zoomed or scaled) than fling will cause the page to move to the edge of the page in fling's direction.
     * @see [https://github.com/tanoDxyz/GeneralDocumentRenderer/blob/main/app/src/main/java/com/tanodxyz/generaldocumentrenderer/photoslider/PhotoSliderActivity.kt]
     *
     */
    var pageFling: Boolean
        get() = get<Boolean>(PROPERTY_DOCUMENT_PAGE_FLING) ?: false
        set(value) {
            this[PROPERTY_DOCUMENT_PAGE_FLING] = value
        }

    var fitEachPage: Boolean
        get() = get<Boolean>(PROPERTY_DOCUMENT_FIT_EACH_PAGE) ?: true
        set(value) {
            this[PROPERTY_DOCUMENT_FIT_EACH_PAGE] = value
        }

    companion object {
        const val PROPERTY_DOCUMENT_NAME = "com.gdr.documentName"
        const val PROPERTY_DOCUMENT_PAGE_FIT_POLICY = "com.gdr.documentPageFitPolicy"
        const val PROPERTY_DOCUMENT_FIT_EACH_PAGE = "com.gdr.fitEachPage"
        const val PROPERTY_DOCUMENT_SWIPE_VERTICAL = "com.gdr.swipeVertical"
        const val PROPERTY_DOCUMENT_PAGE_FLING = "com.gdr.page.fling"
        const val PROPERTY_NIGHT_MODE = "com.night.mode"
        const val PROPERTY_PAGE_BACK_COLOR = "com.page.back.color"
        const val DOCUMENT_NAME = "document-"
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

    @Synchronized
    open fun addPage(documentPage: DocumentPage) {
        documentPageData.add(documentPage)
    }

    @Synchronized
    open fun addPages(documentPages: List<DocumentPage>) {
        documentPageData.addAll(documentPages)
    }

    @Synchronized
    open fun setup(documentRenderView: DocumentRenderView) {
        documentPageData.forEach { documentPage ->
            val pageSize = documentPage.originalSize
            if (pageSize.width > originalMaxPageWidth.width) {
                originalMaxPageWidth = pageSize
            }
            if (pageSize.height > originalMaxPageHeight.height) {
                originalMaxPageHeight = pageSize
            }
        }
        recalculatePageSizesAndIndexes(documentRenderView)
    }

    @Synchronized
    open fun getMaxPageSize(): Size {
        return if (swipeVertical) maxWidthPageSize else maxHeightPageSize
    }

    @Synchronized
    open fun getMaxPageWidth(): Int {
        return getMaxPageSize().width
    }

    @Synchronized
    open fun getMaxPageHeight(): Int {
        return getMaxPageSize().height
    }

    @Synchronized
    open fun recalculatePageSizesAndIndexes(documentRenderView: DocumentRenderView) {
        val viewSize = Size(documentRenderView.measuredWidth, documentRenderView.measuredHeight)
        if (documentPageData.isEmpty()) {
            return
        }
        if (viewSize.width <= 0 || viewSize.height <= 0) {
            return
        }

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
            put(PageSizeCalculator.DISPLAY_METRICS, documentRenderView.displayMetrics!!)
        }
        // setup page size calculator
        pageSizeCalculator!!.setup(props)
        maxWidthPageSize = pageSizeCalculator!!.optimalMaxWidthPageSize
        maxHeightPageSize = pageSizeCalculator!!.optimalMaxHeightPageSize
        // calculate page size and element sizes
        contentLength = 0F
        var indexX = 0F
        var indexY = 0F
        pageIndexes = ArrayList(documentPageData.count())
        for (i: Int in documentPageData.indices) {
            val documentPage = documentPageData[i]

            // calculate leftMargin & topMargin pageRelativeBounds
            documentPage.modifiedSize = pageSizeCalculator!!.calculate(documentPage.originalSize)
            if (documentPage.modifiedSize.width > maxWidthPageSize.width) {
                maxWidthPageSize.width = documentPage.modifiedSize.width
            }
            if (documentPage.modifiedSize.height > maxHeightPageSize.height) {
                maxHeightPageSize.height = documentPage.modifiedSize.height
            }
            contentLength += if (this.swipeVertical) {
                documentPage.modifiedSize.height
            } else {
                documentPage.modifiedSize.width
            }
            documentPage.resetPageBounds()
            if (swipeVertical) {
                pageIndexes.add(PointF(0F, indexY))
                indexY += documentPage.modifiedSize.height
            } else {
                pageIndexes.add(PointF(indexX, 0F))
                indexX += documentPage.modifiedSize.width
            }
            documentPage.onMeasurementDone(documentRenderView)
        }
    }

    fun getPageSizeWithMargins(page: DocumentPage): Size {
        val horizontalMargins = pageMargins.left + pageMargins.right
        val verticalMargins = pageMargins.top + pageMargins.bottom
        val originalSize = page.originalSize
        val size = Size(originalSize.width, originalSize.height)
        size.width -= horizontalMargins.roundToInt()
        size.height -= verticalMargins.roundToInt()
        return size
    }

    @Synchronized
    open fun toCurrentScale(size: Number, zoom: Float): Float {
        return size.toFloat() * zoom
    }

    @Synchronized
    fun getDocLen(zoom: Float): Float {
        return getTotalContentLength() * zoom
    }

    @Synchronized
    open fun getTotalContentLength(): Float = contentLength

    @Synchronized
    open fun getDocumentPages(): List<DocumentPage> = documentPageData

    @Synchronized
    open fun haveNoPages(): Boolean = getDocumentPages().isEmpty()

    @Synchronized
    open fun getPagesCount(): Int = getDocumentPages().count()

    @Synchronized
    open fun getPage(pageNumber: Int): DocumentPage? {
        return if (pageNumber < 0 || pageNumber >= getPagesCount()) {
            null
        } else {
            documentPageData[pageNumber]
        }
    }

    @Synchronized
    open fun isEmpty(): Boolean = getPagesCount() < 1

    @Synchronized
    open fun getPagesToBeDrawn(currentPage: Int, viewSize: Int): List<DocumentPage> {
        if (isEmpty()) {
            return mutableListOf()
        }
        val page = currentPage - 1
        val pagesToBeDrawn = mutableListOf<DocumentPage>()
        if (page <= 0) {
            var pagesAddedToListSize = 0
            for (i: Int in documentPageData.indices) {
                val documentPage = documentPageData[i]
                if (viewSize < pagesAddedToListSize) {
                    break
                }
                pagesToBeDrawn.add(documentPage)
                pagesAddedToListSize += if (swipeVertical) {
                    documentPage.modifiedSize.height
                } else {
                    documentPage.modifiedSize.width
                }
            }
        } else {
            var pagesAddedToListForwardSize = 0
            for (i: Int in page until documentPageData.count()) {
                val documentPage = documentPageData[i]
                if (viewSize < pagesAddedToListForwardSize) {
                    break
                }
                pagesToBeDrawn.add(documentPage)
                pagesAddedToListForwardSize += if (swipeVertical) {
                    documentPage.modifiedSize.height
                } else {
                    documentPage.modifiedSize.width
                }
            }

            var pagesAddedToListBackwardSize = 0
            for (i: Int in (page - 1) downTo 0) {
                val documentPage = documentPageData[i]
                if (viewSize < pagesAddedToListBackwardSize) {
                    break
                }
                pagesToBeDrawn.add(0, documentPage)
                pagesAddedToListBackwardSize += if (swipeVertical) {
                    documentPage.modifiedSize.height
                } else {
                    documentPage.modifiedSize.width
                }
            }
        }
        return pagesToBeDrawn
    }

    @Synchronized
    open fun close() {
        documentPageData.forEach { it.recycle() }
    }
}