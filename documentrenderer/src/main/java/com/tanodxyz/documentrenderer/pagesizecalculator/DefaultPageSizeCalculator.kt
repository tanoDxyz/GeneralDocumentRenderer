package com.tanodxyz.documentrenderer.pagesizecalculator


import com.tanodxyz.documentrenderer.Size
import com.tanodxyz.documentrenderer.document.Document
import com.tanodxyz.documentrenderer.document.Document.PageFitPolicy


class DefaultPageSizeCalculator : PageSizeCalculator() {
    private var fitPolicy: PageFitPolicy = PageFitPolicy.BOTH
    private var originalMaxWidthSize: Size = Size(0, 0)
    private var originalMaxHeightSize: Size = Size(0, 0)
    override var viewSize: Size = Size(0, 0)
    private var fitEachPage: Boolean = false
    private var widthRatio = 0f
    private var heightRatio = 0f
    private lateinit var optimalMaxWidthSize: Size
    private lateinit var optimalMaxHeightSize: Size

    override fun setup(args: HashMap<String, Any>) {
        val fitPolicy = args[FIT_POLICY] as PageFitPolicy? ?: Document.PageFitPolicy.BOTH
        val fitEachPage: Boolean = args[FIT_EACH_PAGE] as? Boolean ?: true
        setup(
            fitPolicy,
            args[MAX_WIDTH_PAGE_SIZE] as Size,
            args[MAX_HEIGHT_PAGE_SIZE] as Size,
            args[VIEW_SIZE] as Size,
            fitEachPage
        )
    }

    fun setup(
        fitPolicy: PageFitPolicy,
        originalMaxWidthSize: Size,
        originalMaxHeightSize: Size,
        viewSize: Size,
        fitEachPage: Boolean = false
    ) {
        this.fitPolicy = fitPolicy
        originalMaxHeightSize.ensureValuesAreNonZero()
        originalMaxWidthSize.ensureValuesAreNonZero()
        this.originalMaxWidthSize = originalMaxWidthSize
        this.originalMaxHeightSize = originalMaxHeightSize
        this.viewSize = viewSize
        this.fitEachPage = fitEachPage
        calculateMaxPages()
    }

    override var optimalMaxWidthPageSize: Size
        set(value) {
            optimalMaxWidthSize = value
        }
        get() = optimalMaxWidthSize

    override var optimalMaxHeightPageSize: Size
        set(value) {
            optimalMaxHeightSize = value
        }
        get() = optimalMaxHeightSize


    override fun calculate(pageSize: Size): Size {
        if (pageSize.width <= 0 || pageSize.height <= 0) {
            return Size(0, 0)
        }
        val maxWidth = if (fitEachPage) viewSize.width.toFloat() else pageSize.width * widthRatio
        val maxHeight =
            if (fitEachPage) viewSize.height.toFloat() else pageSize.height * heightRatio
        return when (fitPolicy) {
            PageFitPolicy.FIT_HEIGHT -> fitHeight(pageSize, maxHeight)
            PageFitPolicy.BOTH -> fitBoth(pageSize, maxWidth, maxHeight)
            PageFitPolicy.NONE -> pageSize
            else -> fitWidth(pageSize, maxWidth)
        }
    }

    override fun calculateElementSizeRelative(size: Size): Size {
        return calculate(size)
    }

    private fun calculateMaxPages() {
        when (fitPolicy) {
            PageFitPolicy.FIT_HEIGHT -> {
                optimalMaxHeightSize =
                    fitHeight(originalMaxHeightSize, viewSize.height.toFloat())
                heightRatio =
                    optimalMaxHeightSize.height / originalMaxHeightSize.height.toFloat()
                optimalMaxWidthSize = fitHeight(
                    originalMaxWidthSize,
                    originalMaxWidthSize.height * heightRatio
                )
            }

            PageFitPolicy.BOTH -> {
                val localOptimalMaxWidth: Size = fitBoth(
                    originalMaxWidthSize,
                    viewSize.width.toFloat(),
                    viewSize.height.toFloat()
                )
                val localWidthRatio = localOptimalMaxWidth.width / originalMaxWidthSize.width
                optimalMaxHeightSize = fitBoth(
                    originalMaxHeightSize, originalMaxHeightSize.width.toFloat() * localWidthRatio,
                    viewSize.height.toFloat()
                )
                heightRatio =
                    optimalMaxHeightSize.height / originalMaxHeightSize.height.toFloat()
                optimalMaxWidthSize = fitBoth(
                    originalMaxWidthSize,
                    viewSize.width.toFloat(),
                    originalMaxWidthSize.height * heightRatio
                )
                widthRatio = optimalMaxWidthSize.width / originalMaxWidthSize.width.toFloat()
            }

            PageFitPolicy.NONE -> {
                optimalMaxWidthSize = Size(viewSize.width, viewSize.height)
                widthRatio = optimalMaxWidthSize.width / optimalMaxWidthSize.height.toFloat()
                optimalMaxHeightSize = Size(viewSize.height, viewSize.width)
            }

            else -> {
                optimalMaxWidthSize =
                    fitWidth(originalMaxWidthSize, viewSize.width.toFloat())
                widthRatio = optimalMaxWidthSize.width / originalMaxWidthSize.width.toFloat()
                optimalMaxHeightSize = fitWidth(
                    originalMaxHeightSize,
                    originalMaxHeightSize.width * widthRatio
                )
            }
        }
    }

    private fun fitWidth(size: Size, maxWidth: Float): Size {
        var w = size.width.toFloat()
        var h = size.height.toFloat()
        val ratio = w / h
        w = maxWidth
        h = Math.floor((maxWidth / ratio).toDouble()).toFloat()
        return Size(w.toInt(), h.toInt())
    }

    private fun fitHeight(size: Size, maxHeight: Float): Size {
        var w = size.width.toFloat()
        var h = size.height.toFloat()
        val ratio = h / w
        h = maxHeight
        w = Math.floor((maxHeight / ratio).toDouble()).toFloat()
        return Size(w.toInt(), h.toInt())
    }

    private fun fitBoth(size: Size, maxWidth: Float, maxHeight: Float): Size {
        var w = size.width.toFloat()
        var h = size.height.toFloat()
        val ratio = w / h
        w = maxWidth
        h = Math.floor((maxWidth / ratio).toDouble()).toFloat()
        if (h > maxHeight) {
            h = maxHeight
            w = Math.floor((maxHeight * ratio).toDouble()).toFloat()
        }
        return Size(w.toInt(), h.toInt())
    }
}