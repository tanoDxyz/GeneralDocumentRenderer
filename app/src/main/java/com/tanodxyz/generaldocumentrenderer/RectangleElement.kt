package com.tanodxyz.generaldocumentrenderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.SparseArray
import com.tanodxyz.documentrenderer.elements.PageElement
import com.tanodxyz.documentrenderer.page.DocumentPage
import java.security.SecureRandom

class RectangleElement(page: DocumentPage): PageElement(page) {
    val colors = mutableListOf<Int>().apply {
        add(Color.GREEN)
        add(Color.RED)
        add(Color.MAGENTA)
        add(Color.BLACK)
        add(Color.GRAY)
        add(Color.DKGRAY)
        add(Color.CYAN)
    }

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colors[SecureRandom().nextInt(7)]
    }

    override fun draw(canvas: Canvas, args: SparseArray<Any>?) {
        super.draw(canvas, args)
        val boundsRelativeToPage = getBoundsRelativeToPage(args.shouldDrawFromOrigin())
        canvas.drawRect(boundsRelativeToPage,paint)
    }
}