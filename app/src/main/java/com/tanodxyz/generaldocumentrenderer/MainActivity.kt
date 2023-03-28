package com.tanodxyz.generaldocumentrenderer

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.tanodxyz.documentrenderer.*
import com.tanodxyz.documentrenderer.document.Document
import com.tanodxyz.documentrenderer.elements.DefaultCircularProgressBarElement
import com.tanodxyz.documentrenderer.elements.PageElement
import com.tanodxyz.documentrenderer.elements.SimpleStaticTextElement
import com.tanodxyz.documentrenderer.extensions.DefaultScrollHandle
import com.tanodxyz.documentrenderer.page.DocumentPage
import com.tanodxyz.documentrenderer.pagesizecalculator.FixPageSizeCalculator
import com.tanodxyz.itext722g.IText722
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
        var sr = SecureRandom()
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
                document = Document(context, FixPageSizeCalculator())
                val handler = Handler()
                thread(start = true) {
                    for (i: Int in 0 until 1200) {
                        val elements = mutableListOf<PageElement>()

                        val documentPage = DocumentPage(
                            uniqueId = i,
                            elements,
                            documentRenderView = documentPageRequestHandler,
                            originalSize = Size(
                                /*secureRandom.nextInt(1000) + 10*/712,
                                /*secureRandom.nextInt(1000) + 10*/712
                            )
                        )

                        val textElement = SimpleStaticTextElement(page = documentPage)
                        val createFromAsset =
                            Typeface.createFromAsset(context.assets, "fonts/abcd.otf")
                        textElement.setTypeFace(createFromAsset)
//                        textElement.paddings = RectF(32F,32F,32F,32F)
                        textElement.widthMatchParent = true
                        textElement.heightMatchParent = true
                        textElement.textColor = Color.BLACK
                        textElement.x = 80
                        textElement.y = 80
                        val wordtoSpan: Spannable =
                            SpannableString("${sr.nextInt()} You manually draw the text on Canvas when you haveeeeeee styling needs that are not supported by default by the platform, like writing text that follows a curved path.\n" +
                                    "\n" +
                                    "Spans allow you to implement multi-style text with finer grained customisation. For example, you can define paragraphs of your text to have a bullet point by applying a BulletSpan. You can customise the gap between the text margin and the bullet and the colour of the bullet. Starting with Android P, you can even set the radius of the bullet point. You can also create a custom implementation for the span. Check out “Create custom spans” section below to find out how.");
//                        wordtoSpan.setSpan(QuoteSpan(), 0, wordtoSpan.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

//                        wordtoSpan.setSpan(
//                            ForegroundColorSpan(Color.BLUE),
//                            15,
//                            30,
//                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                        )

                        textElement.setText(wordtoSpan)
//                        for(i:Int in 0 until 100) {
//                            val imageElement = ImageElement(documentPage)
//                            imageElement.rawWidth = 200
//                            imageElement.rawHeight = 200
//                            imageElement.x = 10
//                            imageElement.y = 10
//                            imageElement.widthMatchParent = true
//                            imageElement.heightMatchParent = true
//                            elements.add(imageElement)
//                        }


//                        val textElement1 = StaticTextElement(page = documentPage)
//                        textElement1.widthSpec = 200
//                        textElement1.heightSpec = 400
//                        textElement1.x = 300F
//                        textElement1.y = 500f
//                        textElement1.setText("This is the dummy text amigo gringo and mamiro and we are" +
//                                " going to paste a long line text that you may cannot handle properly and" +
//                                " that is the reason for such banevalounch")
//
//
//                        val textElement2 = StaticTextElement(page = documentPage)
//                        textElement2.widthSpec = 200
//                        textElement2.heightSpec = 400
//                        textElement2.x = 300F
//                        textElement2.y = 500f
//                        textElement2.setText("This is the dummy text amigo gringo and mamiro and we are" +
//                                " going to paste a long line text that you may cannot handle properly and" +
//                                " that is the reason for such banevalounch")
//
//
//                        val textElement3 = StaticTextElement(page = documentPage)
//                        textElement3.widthSpec = 200
//                        textElement3.heightSpec = 400
//                        textElement3.x = 300F
//                        textElement3.y = 500f
//                        textElement3.setText("This is the dummy text amigo gringo and mamiro and we are" +
//                                " going to paste a long line text that you may cannot handle properly and" +
//                                " that is the reason for such banevalounch")
//
//
//                        val textElement4 = StaticTextElement(page = documentPage)
//                        textElement4.widthSpec = 200
//                        textElement4.heightSpec = 400
//                        textElement4.x = 300F
//                        textElement4.y = 500f
//                        textElement4.setText("This is the dummy text amigo gringo and mamiro and we are" +
//                                " going to paste a long line text that you may cannot handle properly and" +
//                                " that is the reason for such banevalounch")
//
//
//
//                        val textElement5 = StaticTextElement(page = documentPage)
//                        textElement5.widthSpec = 200
//                        textElement5.heightSpec = 400
//                        textElement5.x = 300F
//                        textElement5.y = 500f
//                        textElement5.setText("This is the dummy text amigo gringo and mamiro and we are" +
//                                " going to paste a long line text that you may cannot handle properly and" +
//                                " that is the reason for such banevalounch")
//
//
//                        val textElement6 = StaticTextElement(page = documentPage)
//                        textElement6.widthSpec = 200
//                        textElement6.heightSpec = 400
//                        textElement6.x = 300F
//                        textElement6.y = 500f
//                        textElement6.setText("This is the dummy text amigo gringo and mamiro and we are" +
//                                " going to paste a long line text that you may cannot handle properly and" +
//                                " that is the reason for such banevalounch")
//
//
//                        val textElement7 = StaticTextElement(page = documentPage)
//                        textElement7.widthSpec = 200
//                        textElement7.heightSpec = 400
//                        textElement7.x = 300F
//                        textElement7.y = 500f
//                        textElement7.setText("This is the dummy text amigo gringo and mamiro and we are" +
//                                " going to paste a long line text that you may cannot handle properly and" +
//                                " that is the reason for such banevalounch")
//                        if(i == 5) {
//                            val documentPage1 = DocumentPage(
//                                uniqueId = i,
//                                elements,
//                                documentRenderView = documentPageRequestHandler,
//                                originalSize = Size(
//                                    /*secureRandom.nextInt(1000) + 10*/200,
//                                    /*secureRandom.nextInt(1000) + 10*/200
//                                )
//                            )
//                            val pageElement = PageElement(page = documentPage1, PageElement(paddings = RectF(80F,80F,80F,80F)))
//                            pageElement.rawWidth = 200
//                            pageElement.rawHeight = 200
//                            pageElement.widthMatchParent = true
//                            pageElement.heightMatchParent = true
//                            pageElement.x = 0F
//                            pageElement.y = 0F
//                            elements.add(pageElement)
//                            document!!.addPage(documentPage1)
//                        }
                        elements.addAll(arrayOf(textElement/*,textElement1,textElement2,textElement3,textElement4,textElement5,textElement6,textElement7*/))
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