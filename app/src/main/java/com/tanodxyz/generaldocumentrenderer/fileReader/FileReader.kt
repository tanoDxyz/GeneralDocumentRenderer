package com.tanodxyz.generaldocumentrenderer.fileReader

import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import androidx.core.text.toSpannable
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.Size
import com.tanodxyz.documentrenderer.document.Document
import com.tanodxyz.documentrenderer.elements.PageElement
import com.tanodxyz.documentrenderer.elements.SimpleTextElement
import com.tanodxyz.documentrenderer.page.DocumentPage
import com.tanodxyz.documentrenderer.pagesizecalculator.FixPageSizeCalculator
import com.tanodxyz.documentrenderer.pagesizecalculator.UnBoundedPageSizeCalculator
import kotlinx.coroutines.processNextEventInCurrentThread
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.concurrent.thread

/**
 * This example class basically reads a file from assets and display the content in it.
 * it uses pretty simple algorithm which work as follows.
 *
 * Read whole file line by line and then fit each line on the page until all page height is consumed.
 * if so than create another page repeat the process.
 */
class FileReader(val renderView: DocumentRenderView) {

    private var document: Document? = null
    fun prepareDocument(loadInRenderView: Boolean, onFinish: () -> Unit) {
        val handler = Handler(Looper.getMainLooper())

        infix fun PageElement.applyLeftTopMargin(margin: Float) {
            margins.left = margin
            margins.top = margin
        }

        infix fun SimpleTextElement.setup(margins: Float) {
            this applyLeftTopMargin margins
            this.symmetric = true
        }

        fun SimpleTextElement.getSpannableStringBuilder(): SpannableStringBuilder {
            return SpannableStringBuilder(this.getText() ?: "")
        }

        thread {
            val displayMetrics = renderView.resources.displayMetrics
            var linesList: List<String>? = null
            BufferedReader(InputStreamReader(renderView.resources.assets.open("textFile.txt"))).useLines {
                linesList = it.toList()
            }

            document = Document(renderView.context, UnBoundedPageSizeCalculator())
            document?.swipeVertical = true
            document?.documentFitPagePolicy = Document.PageFitPolicy.BOTH
            document?.fitEachPage = true

            val textElementMargins = 32F // left,top,right,bottom
            var pageUniqueId = 0

            val maxScreenHeight = displayMetrics.heightPixels
            val maxScreenWidth = displayMetrics.widthPixels

            val maxAvailableWidthToPageElements =
                maxScreenWidth - document!!.pageMargins.left - document!!.pageMargins.right
            val maxAvailableHeightToPageElements =
                maxScreenHeight - document!!.pageMargins.top - document!!.pageMargins.bottom

            val availableWidthToTextElementWithMargins = maxAvailableWidthToPageElements - (textElementMargins.times(2))
            val availableHeightToTextElementWithMargins = maxAvailableHeightToPageElements - (textElementMargins.times(2))

            val textMeasuringElement = SimpleTextElement(
                DocumentPage(
                    0,
                    originalSize = Size(
                        maxAvailableWidthToPageElements.toInt(),
                        maxAvailableHeightToPageElements.toInt()
                    )
                )
            )

            textMeasuringElement setup textElementMargins


            var textHeightUsed = 0
            val pageSize = Size(maxScreenWidth, maxScreenHeight)
            var currentPage = DocumentPage(uniqueId = pageUniqueId, originalSize = pageSize)
            var currentTextElement = SimpleTextElement(currentPage)

            fun addCurrentElementsToDocument() {
                currentTextElement setup textElementMargins
                currentTextElement.desiredWidth = availableWidthToTextElementWithMargins + textElementMargins.times(2) // we are
                // adding this because textElement internally handles these margins. and also availableWidthToTextElementWithMargins was created to be used externally out of the text element.
                currentTextElement.desiredHeight = availableHeightToTextElementWithMargins + textElementMargins.times(2)
                currentPage.elements.add(currentTextElement)
                currentPage.setUseScalingFactorForSnapshot(false)
                document!!.addPage(currentPage)
            }

            /**
             * work as follows
             * for each line add it to textMeasuring element until it reaches certain height (max element height)
             * once that is reached add text element to page and add page to document.
             * then create new page and text element and continue adding lines.
             * it is an example and in real world scenarios text should not be handled based on lines
             */
            linesList?.forEach { line ->
                textMeasuringElement.setText(
                    textMeasuringElement.getSpannableStringBuilder().append('\n').append(line)
                )
                textHeightUsed = SimpleTextElement.calculateTextHeight(textMeasuringElement,availableWidthToTextElementWithMargins.toInt())

                if(textHeightUsed + textElementMargins.times(2) < availableHeightToTextElementWithMargins) {
                    currentTextElement.setText(currentTextElement.getSpannableStringBuilder().append('\n').append(line))
                } else {
                    addCurrentElementsToDocument()
                    currentPage = DocumentPage(++pageUniqueId, originalSize = pageSize)
                    currentTextElement = SimpleTextElement(currentPage)
                    currentTextElement.setText(line.toSpannable())
                    textHeightUsed = 0
                    textMeasuringElement.setText("".toSpannable())
                }
            }
            if(textHeightUsed < availableHeightToTextElementWithMargins ) {
                addCurrentElementsToDocument()
            }
            if (loadInRenderView) {
                renderView.loadDocument(document!!, onFinish)
            } else {
                handler.post(onFinish)
            }
        }
    }
}