package com.tanodxyz.generaldocumentrenderer

import android.content.Context
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.tanodxyz.documentrenderer.*
import com.tanodxyz.documentrenderer.document.Document
import com.tanodxyz.documentrenderer.elements.DefaultCircularProgressBarElement
import com.tanodxyz.documentrenderer.extensions.DefaultScrollHandle

import com.tanodxyz.documentrenderer.page.DocumentPage
import com.tanodxyz.documentrenderer.pagesizecalculator.DefaultPageSizeCalculator
import com.tanodxyz.generaldocumentrenderer.pdfparsing.PdfParser
import com.tanodxyz.itext722g.IText722
import org.jetbrains.annotations.TestOnly
import java.security.SecureRandom
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), DocumentRenderView.IdleStateCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.renderView = findViewById(R.id.documentRenderView)
        init()
    }

    private fun init() {

        simpleIdleResource = SimpleIdlingResource()
        // init android itext library 7.2.2
        IText722.init(this)

        createAndAddPagesToDocument(this, renderView.documentPageRequestHandler) { document ->

            document[Document.PROPERTY_DOCUMENT_PAGE_FIT_POLICY] = Document.PageFitPolicy.BOTH
            document.documentFitPagePolicy = Document.PageFitPolicy.BOTH

            document.swipeVertical = true

            document.fitEachPage = true

            document.pageFling = true

            renderView.loadDocument(document)

            // for testing.
            renderView.idleStateCallback = this

            renderView.setBuzyStateIndicator(DefaultCircularProgressBarElement(this))
            renderView.addScrollHandle(DefaultScrollHandle(this))

            val pdfParser = PdfParser(this)
            pdfParser.openDocument("bcd.pdf")
        }
    }

    companion object {
        var document: Document? = null
        fun createAndAddPagesToDocument(
            context: Context,
            documentPageRequestHandler: DocumentRenderView.DocumentPageRequestHandler,
            callback: (Document) -> Unit
        ) {
            if (document != null) {
                callback(document!!)
            } else {
                val secureRandom = SecureRandom()
                document = Document(context, DefaultPageSizeCalculator())
                val handler = Handler()
                thread(start = true) {
                    for (i: Int in 0 until 1200) {
                        document!!.addPage(
                            DocumentPage(
                                uniqueId = i,
                                documentPageRequestHandler = documentPageRequestHandler,
                                originalSize = Size(
                                    secureRandom.nextInt(1000),
                                    secureRandom.nextInt(1000)
                                )
                            )
                        )
                    }
                    handler.post {
                        callback(document!!)
                    }
                }

            }

        }

    }

    fun switchSwipeModeClicked(view: View) {
        renderView.changeSwipeMode()
    }

    var simpleIdleResource: SimpleIdlingResource? = null

    lateinit var renderView: DocumentRenderView
    override fun renderViewState(idle: Boolean) {
        simpleIdleResource?.setIdleState(idle)
    }
}