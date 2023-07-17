package com.tanodxyz.generaldocumentrenderer.pdfRenderer

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.util.lruCache
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.Size
import com.tanodxyz.documentrenderer.document.Document
import com.tanodxyz.documentrenderer.elements.InteractiveElement
import com.tanodxyz.documentrenderer.page.DocumentPage
import com.tanodxyz.generaldocumentrenderer.pdfRenderer.PDFRenderer.Companion.key
import java.lang.Exception

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class PdfLoader(val renderView: DocumentRenderView) {
    private var document: Document? = null
    private var pdfRenderer: PDFRenderer? = PDFRenderer(renderView)
    private val handler = Handler(Looper.getMainLooper())

    fun loadFromFile(filePath: String): PdfLoader {
        pdfRenderer?.openPdfFile(filePath)
        return this
    }

    fun loadFromAssets(
        assetManager: AssetManager,
        fileName: String,
    ): PdfLoader {
        pdfRenderer?.openPdfFromAssets(assetManager, fileName)
        return this
    }

    fun loadPageNo(pageNumber: Int, pageBounds: RectF, callback: (Bitmap?) -> Unit) {
        pdfRenderer?.loadPageNew(pageNumber, pageBounds, callback)
    }

    fun recycle(pageNumber: Int, pageBounds: RectF) {
        pdfRenderer?.remove(key(pageNumber, pageBounds))
    }

    fun prepareDocument(loadInRenderView: Boolean, onFinish: () -> Unit) {
        pdfRenderer?.getPageSizes { pdfPageSizesList ->
            document = Document(renderView.context)
            document?.documentFitPagePolicy = Document.PageFitPolicy.BOTH

            document!!.swipeVertical = true

            document!!.fitEachPage = true

            document!!.pageFling = true
            for (i: Int in pdfPageSizesList.indices) {
                val pdfPageSize = pdfPageSizesList[i]
                val elements = mutableListOf<InteractiveElement>()
                val page = DocumentPage(i, elements, pdfPageSize).apply {
                    PdfElement(this, this@PdfLoader).apply {
                        elements.add(this)
                    }
                    document?.addPage(this)
                }
                page.setUseScalingFactorForSnapshot(false)
            }
            if (loadInRenderView) {
                document?.let { renderView.loadDocument(it, onFinish) }
            } else {
                handler.post(onFinish)
            }
        }
    }

    fun close() {
        pdfRenderer?.close()
        document?.close()
        handler.removeCallbacksAndMessages(null)
    }
}