package com.tanodxyz.generaldocumentrenderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.SparseArray
import android.view.WindowInsetsAnimation
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.Size
import com.tanodxyz.documentrenderer.document.Document
import com.tanodxyz.documentrenderer.elements.InteractiveElement
import com.tanodxyz.documentrenderer.elements.PageElement
import com.tanodxyz.documentrenderer.page.DocumentPage
import com.tanodxyz.documentrenderer.pagesizecalculator.DefaultPageSizeCalculator

class CustomElementActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_element)
        init()
    }

    private fun init() {
        val document = Document(this,DefaultPageSizeCalculator())
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
        val customElement = CustomElement(documentPage)
        customElement.margins.apply {
            left = 300f
            top = 100f
        }
        /////////////////////////////////////////////
        documentPage.elements.add(customElement)
        document.addPage(documentPage)
        renderView.loadDocument(document)
    }


    open class CustomElement(page: DocumentPage) : PageElement(page) {
        val width = 128 //px
        val height = 128 // px

        init {
            debugPaint.apply {
                color = Color.GREEN
                style = Paint.Style.FILL
            }
        }

        override fun getContentHeight(args: SparseArray<Any>?): Float {
            return page.documentRenderView.toCurrentScale(width)
        }

        override fun getContentWidth(args: SparseArray<Any>?): Float {
            return page.documentRenderView.toCurrentScale(height)
        }

        override fun draw(canvas: Canvas, args: SparseArray<Any>?) {
            super.draw(canvas, args)
            // args.shouldDrawSnapShot indicates whether page is currently scaled and page is drawing snapshot instead of original content.
            val contentBounds = getContentBounds(args.shouldDrawSnapShot())

            canvas.drawRect(contentBounds, debugPaint)
        }
    }
}