package com.tanodxyz.generaldocumentrenderer

import android.graphics.Canvas
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.SparseArray
import androidx.core.text.toSpannable
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.Size
import com.tanodxyz.documentrenderer.document.Document
import com.tanodxyz.documentrenderer.elements.PageElement
import com.tanodxyz.documentrenderer.elements.SimpleTextElement
import com.tanodxyz.documentrenderer.events.SingleTapConfirmedEvent
import com.tanodxyz.documentrenderer.page.DocumentPage
import com.tanodxyz.documentrenderer.pagesizecalculator.DefaultPageSizeCalculator

class CustomElementInteractiveActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_element_interactive)
        init()
    }

    private fun init() {

        supportActionBar?.setTitle(R.string.customElementInteractive)

        val document = Document(this, DefaultPageSizeCalculator())

        document.pageBackColor = Color.MAGENTA
        document.pageMargins.apply {
            left += 12
            top += 12
            right += 12
        }
        document.pageCorners = 16F

        val renderView = findViewById<DocumentRenderView>(R.id.renderView)

        val documentPage = DocumentPage(
            uniqueId = document.pageUniqueIdWrapper.pageUniqueId,
            originalSize = Size(
                renderView.displayMetrics!!.widthPixels,
                renderView.displayMetrics!!.heightPixels.div(2)
            )
        )

        /////////////////////////////////////////////////////
        val customElement = CustomInteractiveElement(documentPage)
        customElement.margins.apply {
            left = 300f
            top = 100f
        }
        /////////////////////////////////////////////
        documentPage.elements.add(customElement)
        document.addPage(documentPage)
        renderView.loadDocument(document)
    }

    class CustomInteractiveElement(page:DocumentPage):CustomElementActivity.CustomElement(page),PageElement.OnClickListener {

        private var textElement = SimpleTextElement(page).apply {
            textColor = Color.WHITE
            setText("Tap Green Rectangle".toSpannable())
            margins.apply {
                top = 10F
            }
        }
        init {
            // on Long press element can be moved across page.
            movable = true
            clickListener = this
        }

        override fun draw(canvas: Canvas, args: SparseArray<Any>?) {
            super.draw(canvas, args)
            textElement.draw(canvas,args)
        }

        override fun onClick(eventMarker: SingleTapConfirmedEvent?, pageElement: PageElement) {
            textElement.setText("${pageElement.type} clicked $eventMarker".toSpannable())
        }
    }
}