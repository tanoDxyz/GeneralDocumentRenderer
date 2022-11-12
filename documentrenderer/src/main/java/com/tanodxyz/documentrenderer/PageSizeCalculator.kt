package com.tanodxyz.documentrenderer


import android.util.SizeF
import com.tanodxyz.documentrenderer.document.Document.PageFitPolicy
import com.tanodxyz.documentrenderer.document.Size

class PageSizeCalculator(
    val fitPolicy: PageFitPolicy,
    val originalMaxWidthSize: Size,
    val originalMaxHeightSize: Size,
    val viewSize: Size,
    val fitEachPage: Boolean = false
) {
    private var widthRatio = 0f
    private var heightRatio = 0f
    private lateinit var optimalMaxWidthSize: Size
    private lateinit var optimalMaxHeightSize: Size

    init {
        calculateMaxPages()
    }

    val optimalMaxWidthPageSize: Size get() = optimalMaxWidthSize
    val optimalMaxHeightPageSize: Size get() = optimalMaxHeightSize

    fun calculate(pageSize: Size): Size {
        if (pageSize.width <= 0 || pageSize.height <= 0) {
            return Size(0F, 0F)
        }
        val maxWidth = if (fitEachPage) viewSize.width else pageSize.width * widthRatio
        val maxHeight = if (fitEachPage) viewSize.height else pageSize.height * heightRatio
        return when (fitPolicy) {
            PageFitPolicy.FIT_HEIGHT -> fitHeight(pageSize, maxHeight)
            PageFitPolicy.BOTH -> fitBoth(pageSize, maxWidth, maxHeight)
            else -> fitWidth(pageSize, maxWidth)
        }
    }

    private fun calculateMaxPages() {
        when (fitPolicy) {
            PageFitPolicy.FIT_HEIGHT -> {
                optimalMaxHeightSize =
                    fitHeight(originalMaxHeightSize, viewSize.height.toFloat())
                heightRatio =
                    optimalMaxHeightSize.height / originalMaxHeightSize.height
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
                    originalMaxHeightSize, originalMaxHeightSize.width * localWidthRatio,
                    viewSize.height.toFloat()
                )
                heightRatio =
                    optimalMaxHeightSize.height / originalMaxHeightSize.height
                optimalMaxWidthSize = fitBoth(
                    originalMaxWidthSize,
                    viewSize.width.toFloat(),
                    originalMaxWidthSize.height * heightRatio
                )
                widthRatio = optimalMaxWidthSize.width / originalMaxWidthSize.width
            }
            else -> {
                optimalMaxWidthSize =
                    fitWidth(originalMaxWidthSize, viewSize.width.toFloat())
                widthRatio = optimalMaxWidthSize.width / originalMaxWidthSize.width
                optimalMaxHeightSize = fitWidth(
                    originalMaxHeightSize,
                    originalMaxHeightSize.width * widthRatio
                )
            }
        }
    }

    private fun fitWidth(size: Size, maxWidth: Float): Size {
        var w = size.width
        var h = size.height
        val ratio = w / h
        w = maxWidth
        h = Math.floor((maxWidth / ratio).toDouble()).toFloat()
        return Size(w, h)
    }

    private fun fitHeight(size: Size, maxHeight: Float): Size {
        var w = size.width
        var h = size.height
        val ratio = h / w
        h = maxHeight
        w = Math.floor((maxHeight / ratio).toDouble()).toFloat()
        return Size(w, h)
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
        return Size(w, h)
    }
}