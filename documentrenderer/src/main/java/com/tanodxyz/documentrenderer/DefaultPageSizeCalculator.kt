package com.tanodxyz.documentrenderer


import android.util.SizeF
import com.tanodxyz.documentrenderer.document.Document.PageFitPolicy

//todo switch is needed......
class DefaultPageSizeCalculator(
    val fitPolicy: PageFitPolicy,
    val originalMaxWidthSize: Size,
    val originalMaxHeightSize: Size,
    val viewSize: Size,
    val fitEachPage: Boolean = false
):PageSizeCalculator {
    private var widthRatio = 0f
    private var heightRatio = 0f
    private lateinit var optimalMaxWidthSize: Size
    private lateinit var optimalMaxHeightSize: Size

    init {
        calculateMaxPages()
    }

    override val optimalMaxWidthPageSize: Size get() = optimalMaxWidthSize
    override val optimalMaxHeightPageSize: Size get() = optimalMaxHeightSize

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