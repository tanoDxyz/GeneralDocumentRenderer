package com.tanodxyz.documentrenderer.elements

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.SparseArray
import androidx.core.graphics.toRect
import com.tanodxyz.documentrenderer.DocumentRenderView.Companion.PAGE_SNAPSHOT_SCALE_DOWN_FACTOR
import com.tanodxyz.documentrenderer.getHeight
import com.tanodxyz.documentrenderer.getWidth
import com.tanodxyz.documentrenderer.page.DocumentPage
import com.tanodxyz.documentrenderer.recycleSafetly
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

class PageSnapShotElement(documentPage: DocumentPage) : PageElement(page = documentPage) {
    private var pageSnapShot: PageSnapShot? = null
    private var args = SparseArray<Any>(5)
    override fun draw(canvas: Canvas, args: SparseArray<Any>?) {
        super.draw(canvas, args)
        canvas.apply {
            pageSnapShot?.also {
                drawBitmap(it.bitmap!!, null, page!!.pageBounds.toRect(), null)
            }
        }
    }

    fun isEmpty(): Boolean {
        return pageSnapShot == null || pageSnapShot?.bitmap == null
    }

    fun preparePageSnapShot() {
        val documentRenderView = page!!.documentRenderView
        if (pageSnapShot == null || pageSnapShot!!.scaleLevel != documentRenderView.getCurrentZoom()) {
            createPageSnapShot()
            args.apply {
                this[DocumentPage.RE_DRAW_WITH_RELATIVE_TO_ORIGIN__SNAPSHOT__] = true
                page.elements.forEach { iElement ->
                    iElement.draw(pageSnapShot!!.canvas, this)
                }
            }
        }
    }

    private fun createPageSnapShot() {
        if (pageSnapShot != null) {
            pageSnapShot?.bitmap?.recycleSafetly()
        }
        val pageBounds = page!!.pageBounds
        val documentRenderView = page.documentRenderView
        pageSnapShot = PageSnapShot(
            Bitmap.createBitmap(
                pageBounds.getWidth().div(PAGE_SNAPSHOT_SCALE_DOWN_FACTOR).toInt(),
                pageBounds.getHeight().div(PAGE_SNAPSHOT_SCALE_DOWN_FACTOR).toInt(),
                Bitmap.Config.ARGB_4444
            ),
            documentRenderView.getCurrentZoom()
        )
    }

    override fun recycle() {
        pageSnapShot?.apply {
            bitmap?.recycleSafetly()
        }
        pageSnapShot = null
    }

    class PageSnapShot(
        var bitmap: Bitmap? = null,
        var scaleLevel: Float = 0F,
        var canvas: Canvas = Canvas(bitmap!!)
    )

}