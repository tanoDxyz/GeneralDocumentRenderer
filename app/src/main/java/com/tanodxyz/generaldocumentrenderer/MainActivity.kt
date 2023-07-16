package com.tanodxyz.generaldocumentrenderer

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import com.tanodxyz.documentrenderer.*
import com.tanodxyz.documentrenderer.document.Document
import com.tanodxyz.documentrenderer.elements.DefaultCircularProgressBarElement
import com.tanodxyz.documentrenderer.elements.ImageElement
import com.tanodxyz.documentrenderer.elements.InteractiveElement
import com.tanodxyz.documentrenderer.extensions.DefaultScrollHandle
import com.tanodxyz.documentrenderer.page.DocumentPage
import com.tanodxyz.documentrenderer.pagesizecalculator.DefaultPageSizeCalculator
import com.tanodxyz.generaldocumentrenderer.pdfRenderer.PDFRenderer
import com.tanodxyz.generaldocumentrenderer.pdfRenderer.PdfLoader
import com.tanodxyz.generaldocumentrenderer.pdfRenderer.PdfViewActivity
import com.tanodxyz.generaldocumentrenderer.pdfRenderer.PdfViewActivity.Companion.launchActivity

class MainActivity : AppCompatActivity(), DocumentRenderView.IdleStateCallback {
    private lateinit var renderView: DocumentRenderView
    var simpleIdleResource: SimpleIdlingResource? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        renderView = findViewById(R.id.documentRenderView)
        init()
    }

    private fun init() {
        PdfViewActivity.launchActivity(this)
        simpleIdleResource = SimpleIdlingResource()
        renderView.doOnLayout {
            createAndAddPagesToDocument(renderView) { document ->

                document[Document.PROPERTY_DOCUMENT_PAGE_FIT_POLICY] = Document.PageFitPolicy.BOTH
                document.documentFitPagePolicy = Document.PageFitPolicy.BOTH

                document.swipeVertical = true

                document.fitEachPage = true

                document.pageFling = true

                renderView.loadDocument(document)

                // for testing.
                renderView.idleStateCallback = this

                renderView.setBusyStateIndicator(DefaultCircularProgressBarElement(this))
                renderView.setScrollHandler(DefaultScrollHandle(this))

            }

        }
    }

    fun createAndAddPagesToDocument(renderView: DocumentRenderView,callback:(Document)->Unit) {
        Document(renderView.context).apply {
            addPage(DocumentPage(0, mutableListOf(),Size(4000,500)))
            addPage(DocumentPage(1, mutableListOf(),Size(2000,500)))
            addPage(DocumentPage(2, mutableListOf(),Size(200,5000)))
            addPage(DocumentPage(3, mutableListOf(),Size(200,500)))
            addPage(DocumentPage(4, mutableListOf(),Size(600,500)))
            addPage(DocumentPage(5, mutableListOf(),Size(9100,500)))
            addPage(DocumentPage(6, mutableListOf(),Size(200,500)))
            callback(this)
        }
    }

    fun switchSwipeModeClicked(view: View) {
        renderView.changeSwipeMode()
    }



    override fun renderViewState(idle: Boolean) {
        simpleIdleResource?.setIdleState(idle)
    }

}
