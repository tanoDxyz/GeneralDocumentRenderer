package com.tanodxyz.documentrenderer.page

import android.content.Context
import android.content.res.Resources
import android.graphics.RectF
import com.tanodxyz.documentrenderer.Size
import com.tanodxyz.documentrenderer.elements.IElement
import com.tanodxyz.documentrenderer.getHeight
import com.tanodxyz.documentrenderer.getWidth
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
        return pageBounds.getWidth()
    }

    fun getHeight(): Float {
        return pageBounds.getHeight()
    }

    fun resetPageBounds() {
        pageBounds.top = 0F
        pageBounds.left = 0F
        pageBounds.right = 0F
        pageBounds.bottom = 0F
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