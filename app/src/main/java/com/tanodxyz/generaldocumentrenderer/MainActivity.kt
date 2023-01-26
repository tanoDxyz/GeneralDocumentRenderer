package com.tanodxyz.generaldocumentrenderer

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.TextView
import com.tanodxyz.documentrenderer.*
import com.tanodxyz.documentrenderer.document.Document
import com.tanodxyz.documentrenderer.elements.DefaultCircularProgressBarElement

import com.tanodxyz.documentrenderer.page.DocumentPage
import java.security.SecureRandom

class MainActivity : AppCompatActivity() {
    private lateinit var findViewById: DocumentRenderView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.findViewById = findViewById<DocumentRenderView>(R.id.cvxd)
        val getseesa = getseesa(this)
        getseesa[Document.PROPERTY_DOCUMENT_PAGE_FIT_POLICY] = Document.PageFitPolicy.BOTH
        getseesa.swipeVertical = true
        getseesa[Document.PROPERTY_DOCUMENT_FIT_EACH_PAGE] = true
        getseesa[Document.PROPERTY_DOCUMENT_PAGE_FLING] = false
        findViewById.loadDocument(getseesa)
        findViewById.setBuzyStateIndicator(DefaultCircularProgressBarElement(this))
        findViewById.addScrollHandle(DefaultScrollHandle(this))
//        findViewById.buzy()
        //todo test to get current page.
//        val handler = Handler()
//        thread(start = true) {
//            while(true) {
//                Thread.sleep(2000)
//                handler.post {
//                    println("Xender: ====================================================")
//                    println("Xender: current page is ${findViewById.currentPage}")
//                    val documentPages = getseesa.getDocumentPages()
//                    for(i:Int in documentPages.indices) {
//                        val page = documentPages.get(i)
//                        val viewBounds = RectF(0F,0F,findViewById.width.toFloat(),findViewById.height.toFloat())
//
//                        val pageVisibleOnScreen =
//                            findViewById.isPageVisibleOnScreen(page.pageBounds)
//                        println("Xender: page $i visibility is $pageVisibleOnScreen                         viewBounds = $viewBounds ||| pageBounds = ${page.pageBounds}")
//                    }
//
//                    println("Xender: ====================================================")
//                }
//            }
//        }

    }

    companion object {
        var document: Document? = null
        fun getseesa(context: Context): Document {
            if (document != null) {
                return document!!
            } else {
                document = Document(context,DefaultPageSizeCalculator())

                for(i:Int in 0 until 1000) {
                    document!!.addPage(DocumentPage(uniquieID = i))
                }
                val rnd = SecureRandom()
                document!!.addPage(DocumentPage(uniquieID = rnd.nextInt()+1000, originalSize = Size(10000, 10000)))
                document!!.addPage(DocumentPage(uniquieID = rnd.nextInt()+1001,originalSize = Size(2342, 420)))
                document!!.addPage(DocumentPage(uniquieID = rnd.nextInt()+1002,originalSize = Size(320, 420)))
                document!!.addPage(DocumentPage(uniquieID = rnd.nextInt()+1003,originalSize = Size(32, 42)))
            }

            return document!!
        }
    }

    fun switchSwipeModeClicked(view: View) {
        findViewById.changeSwipeMode()
    }
}