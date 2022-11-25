package com.tanodxyz.generaldocumentrenderer

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.Size
import com.tanodxyz.documentrenderer.document.Document

import com.tanodxyz.documentrenderer.page.DocumentPage

class MainActivity : AppCompatActivity() {
    private lateinit var findViewById: DocumentRenderView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.findViewById = findViewById<DocumentRenderView>(R.id.asd)
        val getseesa = getseesa(this)
        getseesa[Document.PROPERTY_DOOCUMENT_PAGE_FIT_POLICY] = Document.PageFitPolicy.BOTH
        getseesa.swipeVertical = false
        getseesa[Document.PROPERTY_DOCUMENT_FIT_EACH_PAGE] = true
        findViewById.loadDocument(getseesa)

    }

    companion object {
        var document: Document? = null
        fun getseesa(context: Context): Document {
            if (document != null) {
                return document!!
            } else {
                document = Document(context)
                document!!.addPage(DocumentPage())
                document!!.addPage(DocumentPage())
                document!!.addPage(DocumentPage())
                document!!.addPage(DocumentPage())
                document!!.addPage(DocumentPage())
                document!!.addPage(DocumentPage())
//                document!!.addPage(DocumentPage())
//                document!!.addPage(DocumentPage())
//                document!!.addPage(DocumentPage())
//                document!!.addPage(DocumentPage())
//                document!!.addPage(DocumentPage())
//                document!!.addPage(DocumentPage())
//                document!!.addPage(DocumentPage())
//                document!!.addPage(DocumentPage())
//                document!!.addPage(DocumentPage())
//                document!!.addPage(DocumentPage())
                document!!.addPage(DocumentPage(originalSize = Size(10000, 10000)))
                document!!.addPage(DocumentPage(originalSize = Size(2342, 420)))
                document!!.addPage(DocumentPage(originalSize = Size(320, 420)))
                document!!.addPage(DocumentPage(originalSize = Size(32, 42)))
            }

            return document!!
        }
    }

    fun switchSwipeModeClicked(view: View) {
        document?.swipeVertical = !document!!.swipeVertical
        findViewById.redraw()

    }
}