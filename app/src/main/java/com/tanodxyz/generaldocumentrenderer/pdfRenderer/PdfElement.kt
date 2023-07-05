package com.tanodxyz.generaldocumentrenderer.pdfRenderer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.SparseArray
import androidx.core.graphics.toRect
import com.tanodxyz.documentrenderer.elements.ImageElement
import com.tanodxyz.documentrenderer.elements.PageElement
import com.tanodxyz.documentrenderer.getHeight
import com.tanodxyz.documentrenderer.getWidth
import com.tanodxyz.documentrenderer.page.DocumentPage

class PdfElement(page: DocumentPage,val pdfLoader: PdfLoader):PageElement(page) {
    private var currentScaleLevel = -1F
    private var bitmap:Bitmap? = null
    override fun draw(canvas: Canvas, args: SparseArray<Any>?) {
        super.draw(canvas, args)
        if(args.shouldDrawSnapShot()) {
            if(bitmap == null) {
                pdfLoader.loadPageNo(page.uniqueId,page.pageBounds) { loadedBitmap ->
                    this.bitmap = loadedBitmap
                    this.currentScaleLevel = page.documentRenderView.getCurrentZoom()
                    page.redraw()
                }
            }
        } else {
            if(currentScaleLevel != page.documentRenderView.getCurrentZoom() || bitmap == null) {
                pdfLoader.loadPageNo(page.uniqueId,page.pageBounds) { loadedBitmap ->
                    this.bitmap = loadedBitmap
                    this.currentScaleLevel = page.documentRenderView.getCurrentZoom()
                    page.redraw()
                }
            }
        }
        if(page.pageViewState().isObjectPartiallyOrCompletelyVisible()) {
            bitmap?.let { canvas.drawBitmap(it,null,getContentBounds(args.shouldDrawSnapShot()).toRect(),null) }
        }
    }

    override fun getContentHeight(args: SparseArray<Any>?): Float {
        return if(bitmap != null) {
            bitmap!!.height.toFloat()
        } else {
            super.getContentHeight(args)
        }
    }

    override fun getContentWidth(args: SparseArray<Any>?): Float {
        return if(bitmap != null) {
            bitmap!!.width.toFloat()
        } else {
            super.getContentWidth(args)
        }
    }
}