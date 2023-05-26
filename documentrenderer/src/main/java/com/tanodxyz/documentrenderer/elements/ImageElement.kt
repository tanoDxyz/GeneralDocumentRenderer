package com.tanodxyz.documentrenderer.elements

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.SparseArray
import com.tanodxyz.documentrenderer.page.DocumentPage

class ImageElement(page: DocumentPage): PageElement(page) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
    }
    override fun draw(canvas: Canvas, args: SparseArray<Any>?) {
        super.draw(canvas, args)
        val boundsRelativeToPage = getBoundsRelativeToPage(args.shouldDrawFromOrigin())

        canvas.drawRect(boundsRelativeToPage,paint)
    }
}