package com.tanodxyz.generaldocumentrenderer.pdfRenderer

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.Size
import com.tanodxyz.documentrenderer.document.Document
import com.tanodxyz.documentrenderer.elements.InteractiveElement
import com.tanodxyz.documentrenderer.page.DocumentPage
import java.lang.Exception

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class PdfLoader(val renderView: DocumentRenderView) {
    private var document:Document? = null
    private var pdfRenderer: PDFRenderer? = PDFRenderer(renderView)

    fun loadFromFile(filePath: String): PdfLoader {
        pdfRenderer?.openPdfFile(filePath)
        return this
    }

    fun loadFromAssets(assetManager: AssetManager, fileName: String): PdfLoader {
        pdfRenderer?.openPdfFromAssets(assetManager, fileName)
        return this
    }

    fun loadPageNo(pageNumber: Int, pageBounds:RectF, callback: (Bitmap?) -> Unit) {
        pdfRenderer?.loadPage(pageNumber, pageBounds, callback)
    }

    fun prepareDocument(loadInRenderView: Boolean,  onFinish: () -> Unit) {
        val handler = Handler(Looper.getMainLooper())
        pdfRenderer?.getPageSizes { pdfPageSizesList ->
            document = Document(renderView.context)
//            for (i: Int in  pdfPageSizesList.indices) {
              for(i:Int in 0 .. 0){
                val pdfPageSize = pdfPageSizesList[i]
                val elements = mutableListOf<InteractiveElement>()
                DocumentPage(i, elements, pdfPageSize).apply {
                    PdfElement(this,this@PdfLoader).apply {
                        elements.add(this)
                    }
                    document?.addPage(this)
                }
            }
            if(loadInRenderView){
                document?.let { renderView.loadDocument(it,onFinish) }
            } else {
                handler.post(onFinish)
            }
        }
    }

    fun reset() {
        close()
        pdfRenderer = PDFRenderer(renderView)
    }

    fun close() {
        pdfRenderer?.close()
        document?.close()
    }
}