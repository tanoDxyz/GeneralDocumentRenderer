package com.tanodxyz.documentrenderer.page

import android.graphics.Bitmap
import com.tanodxyz.documentrenderer.elements.PageElement

abstract class PageSnapshotElement(page: DocumentPage) : PageElement(page) {
    abstract fun preparePageSnapshot(scaleLevel: Float)
    abstract fun isEmpty(): Boolean
    abstract fun getBitmap(scaleDown:Boolean = true,callback: (Bitmap?) -> Unit)
}