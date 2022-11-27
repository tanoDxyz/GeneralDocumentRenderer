package com.tanodxyz.documentrenderer.page

import android.content.Context
import android.content.res.Resources
import android.graphics.RectF
import com.tanodxyz.documentrenderer.Size
import com.tanodxyz.documentrenderer.elements.IElement
import java.io.Serializable
import java.security.SecureRandom
import java.util.*

data class DocumentPage(
    val index: Int = -1,
    val elements: MutableList<IElement> = mutableListOf(),
    val originalSize: Size = Size(
        3555,
        2666
    ),
    val pageBounds: RectF = RectF(0F, 0F, 0F, 0F),
) : Serializable {
    var size: Size = originalSize



    companion object {
        fun newPageWithWidthEqualsScreenWidth(context: Context, height: Int): DocumentPage {
            val widthPixels = context.resources.displayMetrics.widthPixels
            return DocumentPage(originalSize = Size(widthPixels, height))
        }

        fun newPageWithHeightEqualsScreenHeight(context: Context, width: Int): DocumentPage {
            val heightPixels = context.resources.displayMetrics.heightPixels
            return DocumentPage(originalSize = Size(width, heightPixels))
        }

        fun isPageVisibleOnScreen(
            isVertical: Boolean,
            pageBounds: RectF,
            viewSize: Size
        ): PageVisibility {
            if (isVertical) {
                val pageBottom = pageBounds.bottom
                val pageTop = pageBounds.top

                val topIsVisible = (pageTop >= 0 && pageTop <= (viewSize.height))
                val bottomIsVisible =
                    (pageBottom <= (viewSize.height) && (pageBottom >= 0))

                return if (topIsVisible && bottomIsVisible) PageVisibility.VISIBLE
                else if (topIsVisible || bottomIsVisible) PageVisibility.PARTIALLY_VISIBLE
                else PageVisibility.INVISIBLE
            } else {
                val pageStart = pageBounds.left
                val pageEnd = pageBounds.right
                val startIsVisible = (pageStart >= 0 && pageStart <= (viewSize.width))
                val endIsVisible = pageEnd <= viewSize.width && pageEnd >= 0
                return if (startIsVisible && endIsVisible) PageVisibility.VISIBLE
                else if (startIsVisible || endIsVisible) PageVisibility.PARTIALLY_VISIBLE
                else PageVisibility.INVISIBLE
            }
        }
    }
}