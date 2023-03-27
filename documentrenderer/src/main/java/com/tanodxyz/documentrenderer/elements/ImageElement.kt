package com.tanodxyz.documentrenderer.elements

import android.graphics.Canvas
import android.util.SparseArray
import com.tanodxyz.documentrenderer.page.DocumentPage

class ImageElement(page: DocumentPage): PageElement(page) {
    override fun draw(canvas: Canvas, args: SparseArray<Any>?) {
        super.draw(canvas, args)
        val boundsRelativeToPage = getBoundsRelativeToPage(args.shouldDrawFromOrigin())
        println("Bako: $boundsRelativeToPage")
        canvas.drawRect(boundsRelativeToPage,paint)
    }
}