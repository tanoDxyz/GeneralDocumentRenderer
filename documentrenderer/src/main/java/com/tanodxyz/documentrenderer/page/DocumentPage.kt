package com.tanodxyz.documentrenderer.page

import android.graphics.PointF
import android.graphics.RectF
import com.tanodxyz.documentrenderer.document.PageSize
import com.tanodxyz.documentrenderer.elements.IElement
import java.io.Serializable

data class DocumentPage(
    val elements: MutableList<IElement> = mutableListOf(),
    val pageSize: PageSize = PageSize(234f, 400f)
):Serializable