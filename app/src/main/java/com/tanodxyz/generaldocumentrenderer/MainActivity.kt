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
import com.tanodxyz.documentrenderer.elements.PageElement
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

        createAndAddPagesToDocument(this, renderView) { document ->

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

//            val pdfParser = PdfParser(this)
//            pdfParser.openDocument("bcd.pdf")
        }
    }

    companion object {
        var document: Document? = null
        fun createAndAddPagesToDocument(
            context: Context,
            documentPageRequestHandler: DocumentRenderView,
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
                        val elements = mutableListOf<PageElement>()

                        val documentPage = DocumentPage(
                            uniqueId = i,
                            elements,
                            documentRenderView = documentPageRequestHandler,
                            originalSize = Size(
                                secureRandom.nextInt(1000),
                                secureRandom.nextInt(1000)
                            )
                        )
                        val pageElement = PageElement(page = documentPage)
                        pageElement.layoutParams.width = 200
                        pageElement.layoutParams.height = 200

//                        val pageElement1 = PageElement(page = documentPage)
//                        pageElement1.layoutParams.width = 200
//                        pageElement1.layoutParams.height = 200
//                        val pageElement2 = PageElement(page = documentPage)
//                        pageElement2.layoutParams.width = 200
//                        pageElement2.layoutParams.height = 200
//                        val pageElement3 = PageElement(page = documentPage)
//                        pageElement3.layoutParams.width = 200
//                        pageElement3.layoutParams.height = 200
//                        val pageElement4 = PageElement(page = documentPage)
//                        pageElement4.layoutParams.width = 200
//                        pageElement4.layoutParams.height = 200
//                        val pageElement5 = PageElement(page = documentPage)
//                        pageElement5.layoutParams.width = 200
//                        pageElement5.layoutParams.height = 200
//                        val pageElement6 = PageElement(page = documentPage)
//                        pageElement6.layoutParams.width = 200
//                        pageElement6.layoutParams.height = 200
//                        val pageElement7 = PageElement(page = documentPage)
//                        pageElement7.layoutParams.width = 200
//                        pageElement7.layoutParams.height = 200
//                        val pageElement8 = PageElement(page = documentPage)
//                        pageElement8.layoutParams.width = 200
//                        pageElement8.layoutParams.height = 200
//                        val pageElement9 = PageElement(page = documentPage)
//                        pageElement9.layoutParams.width = 200
//                        pageElement9.layoutParams.height = 200
//                        val pageElement10 = PageElement(page = documentPage)
//                        pageElement10.layoutParams.width = 200
//                        pageElement10.layoutParams.height = 200
                        elements.add(pageElement)
//                        elements.add(pageElement1)
//                        elements.add(pageElement2)
//                        elements.add(pageElement3)
//                        elements.add(pageElement4)
//                        elements.add(pageElement5)
//                        elements.add(pageElement6)
//                        elements.add(pageElement7)
//                        elements.add(pageElement8)
//                        elements.add(pageElement9)
//                        elements.add(pageElement10)

                        document!!.addPage(
                            documentPage
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