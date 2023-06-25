package com.tanodxyz.generaldocumentrenderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.SparseArray
import com.tanodxyz.documentrenderer.elements.PageElementImpl
import com.tanodxyz.documentrenderer.page.DocumentPage
import java.security.SecureRandom
import kotlin.math.roundToInt

class RectangleElementImpl(page: DocumentPage): PageElementImpl(page) {
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
        val boundsRelativeToPage = getBoundsRelativeToPage(args.shouldDrawSnapShot())

        println("marko: $boundsRelativeToPage")
        canvas.drawRect(boundsRelativeToPage,paint)
    }

    override fun contentWidthInCaseOfWrapContent(): Int {
        return page.documentRenderView.toCurrentScale(710).roundToInt()
    }

    override fun contentHeightInCaseOfWrapContent(): Int {
        return page.documentRenderView.toCurrentScale(500).roundToInt()
    }
}