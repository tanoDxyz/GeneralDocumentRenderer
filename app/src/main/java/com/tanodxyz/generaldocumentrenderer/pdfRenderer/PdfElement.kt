package com.tanodxyz.generaldocumentrenderer.pdfRenderer

import android.graphics.Bitmap
import android.graphics.Canvas

import android.graphics.RectF
import android.util.SparseArray
import androidx.core.graphics.toRect
import androidx.core.text.toSpannable
import com.tanodxyz.documentrenderer.copy
import com.tanodxyz.documentrenderer.elements.PageElement
import com.tanodxyz.documentrenderer.elements.SimpleTextElement
import com.tanodxyz.documentrenderer.page.DocumentPage

/**
 * Each [PdfElement] draws bitmap onto page canvas or drawing surface and it works as follows.
 *
 * initially when page is rendered by [com.tanodxyz.documentrenderer.DocumentRenderView].
 * a call to [PdfElement]'s [PdfElement.draw] method is made.
 * which checks if the [PdfElement] has [Bitmap] for [currentScaleLevel].
 * if so just draw the bitmap otherwise load it using [PdfLoader].
 */
class PdfElement(page: DocumentPage, val pdfLoader: PdfLoader) : PageElement(page) {
    private var currentScaleLevel = -1F
    private var bitmap: Bitmap? = null
    private var lock = Any()
    private var textElement = SimpleTextElement(page)

    init {
        textElement.setText("Loading Page".toSpannable())

        page.drawPageSnapShot = true
    }

    private fun loadBitmap(callback: () -> Unit) {
        pdfLoader.loadPageNo(page.uniqueId, page.pageBounds) { loadedBitmap ->
            synchronized(lock) {
                this.bitmap = loadedBitmap
                this.currentScaleLevel = page.documentRenderView.getCurrentZoom()
                callback()
            }
        }
    }


    private fun bitmapIsValid(applyScaleCheck: Boolean = true): Boolean {
        val bitmapIsValid = this.bitmap.isValid()
        return if (bitmapIsValid && applyScaleCheck) {
            (currentScaleLevel == page.documentRenderView.getCurrentZoom())
        } else {
            bitmapIsValid
        }
    }

    private fun drawSnapShot(canvas: Canvas) {
        if (bitmapIsValid(false)) {
            canvas.drawBitmap(
                bitmap!!,
                null,
                getContentBounds(true),
                null
            )
        }
    }

    override fun draw(canvas: Canvas, args: SparseArray<Any>?) {
        super.draw(canvas, args)
        synchronized(lock) {
            if (isPageVisible()) {
                val contentBounds = getContentBounds(args.shouldDrawSnapShot())
                if (args.shouldDrawSnapShot()) {
                    drawSnapShot(canvas)
                } else {
                    if (bitmapIsValid()) {
                        canvas.drawBitmap(
                            bitmap!!,
                            null,
                            contentBounds.toRect(),
                            null
                        )
                    } else {
                        loadBitmap {
                            redraw(true)
                        }
                        canvas.drawTextElementWithArgs(contentBounds,args)
                    }

                }
            }
        }
    }

    private fun Canvas.drawTextElementWithArgs(
        contentBounds: RectF,
        args: SparseArray<Any>?
    ) {
        val textBounds = contentBounds.copy()
        val _100 = page.documentRenderView.toCurrentScale(100)
        textBounds.left += _100
        textBounds.top += _100
        textBounds.right = textBounds.left + (_100.times(3))
        textBounds.bottom = textBounds.top + (_100.times(3))
        textElement.setContentBounds(textBounds)
        textElement.draw(this, args)
    }

    override fun recycle() {
        super.recycle()
        synchronized(lock) {
            if (bitmap.isValid()) {
                bitmap = null
                pdfLoader.recycle(page.uniqueId, page.pageBounds)
            }
        }
    }

    private fun redraw(checkPageVisibilityState: Boolean = true) {
        if (checkPageVisibilityState) {
            if (isPageVisible()) {
                page.documentRenderView.redraw()
            }
        } else {
            page.documentRenderView.redraw()
        }
    }

    private fun isPageVisible(): Boolean {
        return page.pageViewState().isObjectPartiallyOrCompletelyVisible()
    }

    override fun getContentHeight(args: SparseArray<Any>?): Float {
        return if (bitmap != null) {
            bitmap!!.height.toFloat()
        } else {
            super.getContentHeight(args)
        }
    }

    override fun getContentWidth(args: SparseArray<Any>?): Float {
        return if (bitmap != null) {
            bitmap!!.width.toFloat()
        } else {
            super.getContentWidth(args)
        }
    }

    companion object {
        fun Bitmap?.isValid(): Boolean {
            return this != null && (!this.isRecycled)
        }
    }
}