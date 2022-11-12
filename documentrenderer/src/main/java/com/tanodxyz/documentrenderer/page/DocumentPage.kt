package com.tanodxyz.documentrenderer.page

import android.content.res.Resources
import android.graphics.RectF
import com.tanodxyz.documentrenderer.document.Size
import com.tanodxyz.documentrenderer.elements.IElement
import java.io.Serializable
import java.security.SecureRandom
import java.util.*

data class DocumentPage(
    val index: Int = -1,
    val elements: MutableList<IElement> = mutableListOf(),
    val originalSize: Size = Size(
        SecureRandom().nextInt(Resources.getSystem().displayMetrics.widthPixels).toFloat(),
        SecureRandom().nextInt(Resources.getSystem().displayMetrics.heightPixels).toFloat()
//       10000F,45678F
    ),
    val pageBounds: RectF = RectF(0F, 0F, 0F, 0F),
) : Serializable {
    var size: Size = originalSize
}