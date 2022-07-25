package com.tanodxyz.documentrenderer.page

import android.graphics.RectF
import com.tanodxyz.documentrenderer.elements.IElement

class DocumentPage {
    private val elements:MutableList<IElement> = mutableListOf()
    private val pageSize = RectF(0f,0f,0f,0f)
}