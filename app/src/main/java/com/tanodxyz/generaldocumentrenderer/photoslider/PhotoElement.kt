package com.tanodxyz.generaldocumentrenderer.photoslider


import android.util.SparseArray
import com.tanodxyz.documentrenderer.elements.ImageElement
import com.tanodxyz.documentrenderer.getHeight
import com.tanodxyz.documentrenderer.getWidth
import com.tanodxyz.documentrenderer.page.DocumentPage

/**
 * An extension to ImageElement which serves one purpose -
 * it's dimension are always equal to page.
 */
class PhotoElement(page:DocumentPage):ImageElement(page) {

    override fun getContentWidth(args: SparseArray<Any>?): Float {
        return page.pageBounds.getWidth()
    }

    override fun getContentHeight(args: SparseArray<Any>?): Float {
        return page.pageBounds.getHeight()
    }
}