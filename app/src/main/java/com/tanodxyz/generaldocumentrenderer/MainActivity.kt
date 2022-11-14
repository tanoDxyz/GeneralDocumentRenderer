package com.tanodxyz.generaldocumentrenderer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.document.Document
import com.tanodxyz.documentrenderer.document.Size

import com.tanodxyz.documentrenderer.page.DocumentPage

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val findViewById = findViewById<DocumentRenderView>(R.id.asd)
        val getseesa = getseesa()
        getseesa[Document.PROPERTY_DOOCUMENT_PAGE_FIT_POLICY] = Document.PageFitPolicy.BOTH
        getseesa[Document.PROPERTY_DOCUMENT_FIT_EACH_PAGE] = false
        findViewById.loadDocument(getseesa)

    }

    companion object {
        var document:Document? = null
        fun getseesa(): Document {
            if(document != null) {
                return document!!
            } else {
                document = Document()
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
//                document!!.addPage(DocumentPage())
//                document!!.addPage(DocumentPage())
//                document!!.addPage(DocumentPage())
//                document!!.addPage(DocumentPage())
//                document!!.addPage(DocumentPage())
//                document!!.addPage(DocumentPage())
//                document!!.addPage(DocumentPage())
//                document!!.addPage(DocumentPage())
                document!!.addPage(DocumentPage(originalSize = Size(320f,420F)))
                document!!.addPage(DocumentPage(originalSize = Size(32f,42F)))
            }
            
            return document!!
        }
    }
}