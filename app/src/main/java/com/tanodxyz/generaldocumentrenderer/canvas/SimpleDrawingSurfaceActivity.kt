package com.tanodxyz.generaldocumentrenderer.canvas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.Size
import com.tanodxyz.documentrenderer.document.Document
import com.tanodxyz.documentrenderer.page.DocumentPage
import com.tanodxyz.generaldocumentrenderer.R

/**
 * Example demonstrate how a single page can be used to act like a drawing surface.
 */
open class SimpleDrawingSurfaceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_drawing_surface)
        init()
    }

    open fun init() {
        supportActionBar?.setTitle(R.string.canvas)

        val displayMetrics = resources.displayMetrics
        val desiredWidth = displayMetrics.widthPixels.toFloat()
        val desiredHeight = displayMetrics.heightPixels.toFloat().div(1.5f)

        val pageSize = Size(desiredWidth.toInt(),desiredHeight.toInt())

        Document(this).apply {

            val documentPage = DocumentPage(pageUniqueIdWrapper.pageUniqueId, originalSize = pageSize)

            val simpleDrawingElement = SimpleDrawingElement(resources, documentPage)

            documentPage.elements.add(simpleDrawingElement)

            addPage(documentPage)

            findViewById<DocumentRenderView>(R.id.documentRenderView).loadDocument(this)
        }
    }
}