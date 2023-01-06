package com.tanodxyz.documentrenderer.page

import android.content.Context
import android.content.res.Resources
import android.graphics.RectF
import com.tanodxyz.documentrenderer.Size
import com.tanodxyz.documentrenderer.elements.IElement
import java.io.Serializable
import java.security.SecureRandom
import java.util.*
import kotlin.math.abs

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

    fun getWidth(): Float {
        return if (pageBounds.left > 0 && pageBounds.right >= pageBounds.left) {
            pageBounds.width()
        } else if (pageBounds.left < 0 && pageBounds.right < 0) {
            abs(pageBounds.right) - abs(pageBounds.left)
        } else if (pageBounds.left < 0 && pageBounds.right > 0) {
            pageBounds.right + abs(pageBounds.left)
        } else {
            pageBounds.width()
        }
    }

    fun getHeight(): Float {
        return if (pageBounds.top > 0 && pageBounds.bottom >= pageBounds.top) {
            pageBounds.height()
        } else if (pageBounds.top < 0 && pageBounds.bottom < 0) {
            abs(pageBounds.bottom) - abs(pageBounds.top)
        } else if (pageBounds.top < 0 && pageBounds.bottom > 0) {
            pageBounds.bottom + abs(pageBounds.top)
        } else {
            pageBounds.width()
        }
    }


    companion object {
        fun newPageWithWidthEqualsScreenWidth(context: Context, height: Int): DocumentPage {
            val widthPixels = context.resources.displayMetrics.widthPixels
            return DocumentPage(originalSize = Size(widthPixels, height))
        }

        fun newPageWithHeightEqualsScreenHeight(context: Context, width: Int): DocumentPage {
            val heightPixels = context.resources.displayMetrics.heightPixels
            return DocumentPage(originalSize = Size(width, heightPixels))
        }
    }
}