package com.tanodxyz.documentrenderer.page

import android.graphics.Bitmap
import com.tanodxyz.documentrenderer.elements.PageElementImpl

abstract class PageSnapshotElementImpl(page: DocumentPage) : PageElementImpl(page) {
    abstract fun preparePageSnapshot(scaleLevel: Float)
    abstract fun isEmpty(): Boolean
    abstract fun getBitmap(scaleDown:Boolean = true,callback: (Bitmap?) -> Unit)
}