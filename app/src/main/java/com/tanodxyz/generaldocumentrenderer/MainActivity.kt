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
import com.tanodxyz.documentrenderer.extensions.DefaultScrollHandle
import com.tanodxyz.documentrenderer.page.DocumentPage
import com.tanodxyz.documentrenderer.pagesizecalculator.FixPageSizeCalculator
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
        createAndAddPagesToDocument(this, renderView) { document ->

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

    companion object {
        val wordtoSpan: Spannable =
            SpannableString(
                "${3} You manually draw the text on Canvas when you haveeeeeee styling needs that are not supported by default by the platform, like writing text that follows a curved path." +
                        "Spans allow you to implement multi-style text with finer grained customisation. For example, you can define paragraphs of your text to have a bullet point by applying a BulletSpan." +
                        " You can customise the gap between the text margin and the bullet and the colour of the bullet. Starting with Android P, you can even set the radius of the bullet point. You can also " +
                        "create a custom implementation for the span. Check out “Create custom spans” section below to find out how."
            );
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
                val createFromAsset =
                    Typeface.createFromAsset(context.assets, "fonts/vinque_rg_regular.otf")
                val typeface =
                    com.mta.tehreer.graphics.Typeface(context.assets, "fonts/cavier_dreams.ttf")

                RectangleElement.load(context)
                thread(start = true) {
                    for (i: Int in 0 until 100) {
                        val elements = mutableListOf<PageElement>()

                        val documentPage = DocumentPage(
                            uniqueId = i,
                            elements,
                            originalSize = Size(
                                /*secureRandom.nextInt(1000) + 10*/712,
                                /*secureRandom.nextInt(1000) + 10*/712
                            )
                        )

                        val textElement = SimpleStaticTextElement(page = documentPage)
                        textElement.textSizePixels = 28f
                        textElement.setTypeFace(createFromAsset)
//                        textElement.setTypeFace(createFromAsset)
//                        textElement.paddings = RectF(32F,32F,32F,32F)
                        textElement.textColor = Color.BLACK
                        textElement.layoutParams.leftMargin = 100
//                        textElement.layoutParams.topBottomSymmetric = true
//                        textElement.layoutParams.startEndSymmetric = true
                        textElement.layoutParams.topMargin = 100
                        textElement.layoutParams.bottomMargin = 100
                        textElement.layoutParams.rightMargin = 50
                        textElement.useLineSpacingFromFallbacks = true
                        textElement.layoutParams.desiredWidth = -1
                        textElement.layoutParams.desiredHeight = -1
                        val rectangleElement = RectangleElement(page = documentPage)
//                        imageElement.layoutParams.topBottomSymmetric = true
//                        imageElement.layoutParams.startEndSymmetric = true
                        rectangleElement.layoutParams.leftMargin = 100
                        rectangleElement.layoutParams.topMargin = 1
                        rectangleElement.layoutParams.bottomMargin = 100
                        rectangleElement.layoutParams.rightMargin = 50
                        rectangleElement.layoutParams.desiredWidth = -1
                        rectangleElement.layoutParams.desiredHeight = -1

                        var o = 0
                        while(o < 20) {
                            val apply = RectangleElement(documentPage).apply {
                                layoutParams.leftMargin = 3 * (o)
                                layoutParams.topMargin = 2 * (o)
                                layoutParams.bottomMargin = 1
                                layoutParams.rightMargin = 50
                                layoutParams.desiredWidth = -1
                                layoutParams.desiredHeight = -1
                            }
//                            elements.add(apply)
                            ++o
                        }
                        val wordAnotherSpan =
                            SpannableString("مع تأسيس الحكم الإسلامي في شبه القارةEnglish  الهندية ، تم دمج شكل واحد أو أكثر من النص العربي ضمن مجموعة متنوعة من النصوص المستخدمة لكتابة اللغات الأصلية. في القرن العشرين ، تم استبدال النص العربي عمومًا بالأبجدية اللاتينية في البلقان ، [مشكوكًا فيه - ناقش] أجزاء من إفريقيا جنوب الصحراء ، وجنوب شرق آسيا ، بينما في الاتحاد السوفيتي ، بعد فترة وجيزة من اللاتينية ، [35 ] تم تفويض استخدام السيريلية. تغيرت تركيا إلى الأبجدية اللاتينية في عام 1928 كجزء من ثورة التغريب الداخلية. بعد انهيار الاتحاد السوفيتي في عام 1991 ، حاولت العديد من اللغات التركية في الاتحاد السوفيتي السابق أن تحذو حذو تركيا وتحولها إلى أبجدية لاتينية على الطراز التركي. ومع ذلك ، فقد حدث استخدام متجدد للأبجدية العربية إلى حدٍّ محدود في طاجيكستان ، التي يسمح تشابه لغتها مع اللغة الفارسية بالاستخدام المباشر للمنشورات من أفغانستان وإيران.")
                        val wordtoSpan: Spannable =
                            SpannableString(
                                "${sr.nextInt()} You manually draw the text on Canvas when you haveeeeeee styling needs that are not supported by default by the platform, like writing text that follows a curved path." +
                                        "Spans allow you to implement multi-style text with finer grained customisation. For example, you can define paragraphs of your text to have a bullet point by applying a BulletSpan." +
                                        " You can customise the gap between the text margin and the bullet and the colour of the bullet. Starting with Android P, you can even set the radius of the bullet point. You can also " +
                                        "create a custom implementation for the span. Check out “Create custom spans” section below to find out how."
                            );
//                        wordtoSpan.setSpan(QuoteSpan(), 0, wordtoSpan.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

//                        wordtoSpan.setSpan(
//                            ForegroundColorSpan(Color.BLUE),
//                            15,
//                            30,
//                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                        )
                        textElement.setText(wordAnotherSpan)

//                        textElement.setText("wordtoSpandkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkddddddddddddddddddddddddddddddddddd")
//                        for(i:Int in 0 until 100) {
//                            val imageElement = ImageElement(documentPage)
//                            imageElement.rawWidth = 50
//                            imageElement.rawHeight = 50
//                            imageElement.leftMargin = 10
//                            imageElement.topMargin = 10
////                            imageElement.widthMatchParent = true
////                            imageElement.heightMatchParent = true
//                            elements.add(imageElement)
//                        }


//                        val textElement1 = StaticTextElement(page = documentPage)
//                        textElement1.widthSpec = 200
//                        textElement1.heightSpec = 400
//                        textElement1.leftMargin = 300F
//                        textElement1.topMargin = 500f
//                        textElement1.setText("This is the dummy text amigo gringo and mamiro and we are" +
//                                " going to paste a long line text that you may cannot handle properly and" +
//                                " that is the reason for such banevalounch")
//
//
//                        val textElement2 = StaticTextElement(page = documentPage)
//                        textElement2.widthSpec = 200
//                        textElement2.heightSpec = 400
//                        textElement2.leftMargin = 300F
//                        textElement2.topMargin = 500f
//                        textElement2.setText("This is the dummy text amigo gringo and mamiro and we are" +
//                                " going to paste a long line text that you may cannot handle properly and" +
//                                " that is the reason for such banevalounch")
//
//
//                        val textElement3 = StaticTextElement(page = documentPage)
//                        textElement3.widthSpec = 200
//                        textElement3.heightSpec = 400
//                        textElement3.leftMargin = 300F
//                        textElement3.topMargin = 500f
//                        textElement3.setText("This is the dummy text amigo gringo and mamiro and we are" +
//                                " going to paste a long line text that you may cannot handle properly and" +
//                                " that is the reason for such banevalounch")
//
//
//                        val textElement4 = StaticTextElement(page = documentPage)
//                        textElement4.widthSpec = 200
//                        textElement4.heightSpec = 400
//                        textElement4.leftMargin = 300F
//                        textElement4.topMargin = 500f
//                        textElement4.setText("This is the dummy text amigo gringo and mamiro and we are" +
//                                " going to paste a long line text that you may cannot handle properly and" +
//                                " that is the reason for such banevalounch")
//
//
//
//                        val textElement5 = StaticTextElement(page = documentPage)
//                        textElement5.widthSpec = 200
//                        textElement5.heightSpec = 400
//                        textElement5.leftMargin = 300F
//                        textElement5.topMargin = 500f
//                        textElement5.setText("This is the dummy text amigo gringo and mamiro and we are" +
//                                " going to paste a long line text that you may cannot handle properly and" +
//                                " that is the reason for such banevalounch")
//
//
//                        val textElement6 = StaticTextElement(page = documentPage)
//                        textElement6.widthSpec = 200
//                        textElement6.heightSpec = 400
//                        textElement6.leftMargin = 300F
//                        textElement6.topMargin = 500f
//                        textElement6.setText("This is the dummy text amigo gringo and mamiro and we are" +
//                                " going to paste a long line text that you may cannot handle properly and" +
//                                " that is the reason for such banevalounch")
//
//
//                        val textElement7 = StaticTextElement(page = documentPage)
//                        textElement7.widthSpec = 200
//                        textElement7.heightSpec = 400
//                        textElement7.leftMargin = 300F
//                        textElement7.topMargin = 500f
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
//                            pageElement.leftMargin = 0F
//                            pageElement.topMargin = 0F
//                            elements.add(pageElement)
//                            document!!.addPage(documentPage1)
//                        }
//                        elements.add(textElement)
                        val label = RectangleElement(page = documentPage)
//                        label.setText(wordtoSpan.toString())
//                        label.setTypeFace(Typeface.createFromAsset(context.assets,"fonts/cavier_dreams.ttf"))
//                        val d:DynamicLayout? = null
//
//                        label.setText(wordtoSpan)
//                        label.layoutParams.desiredWidth = 200
//                        label.layoutParams.desiredHeight = 200
                        label.layoutParams.desiredWidth = 7000
                        label.layoutParams.desiredHeight = 10000
//                        label.initEngine(typeface)
                        elements.add(label)
//                        elements.addAll(arrayOf(/*textElement*/imageElement/*,textElement1,textElement2,textElement3,textElement4,textElement5,textElement6,textElement7*/))
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


    class Z(context: Context):Document(context) {
        override fun getPagesToBeDrawn(currentPage: Int, viewSize: Int): List<DocumentPage> {
            return super.getPagesToBeDrawn(currentPage, viewSize)
        }
    }
}
