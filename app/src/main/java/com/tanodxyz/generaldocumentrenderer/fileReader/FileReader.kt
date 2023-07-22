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


class FileReader(val renderView: DocumentRenderView) {

    private var document: Document? = null
    fun prepareDocument(loadInRenderView: Boolean, onFinish: () -> Unit) {

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

        val handler = Handler(Looper.getMainLooper())
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
                currentTextElement.desiredWidth = availableWidthToTextElementWithMargins + textElementMargins.times(2)
                currentTextElement.desiredHeight = availableHeightToTextElementWithMargins + textElementMargins.times(2)
                currentPage.elements.add(currentTextElement)
                currentPage.setUseScalingFactorForSnapshot(false)
                document!!.addPage(currentPage)
            }

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
//            val leftMargin = 32
//            val topMargin = 32
//
//            var pageUniqueId = 0
//
//            val maxHeight = displayMetrics.heightPixels
//            val maxWidth = displayMetrics.widthPixels
//
//            val textElementWidth = maxWidth - leftMargin - topMargin.toFloat()
//            val textElementHeight = maxHeight - (topMargin.times(2)).toFloat()
//
//            val pageSize = Size(maxWidth,maxHeight)
//
//            var currentDocumentPage = DocumentPage(uniqueId = pageUniqueId, originalSize = pageSize)
//            var currentTextElement = SimpleTextElement(currentDocumentPage)
//
//            val textMeasuringElement = SimpleTextElement(currentDocumentPage)
//
//            var textHeightUsed = 0
//            textMeasuringElement.symmetric = true
//            textMeasuringElement applyLeftTopMargin leftMargin.toFloat()
//
//            linesList?.forEach { line->
//
//                textMeasuringElement.setText(SpannableStringBuilder(textMeasuringElement.getText()?:"").append('\n').append(line))
//                textHeightUsed =
//                    SimpleTextElement.calculateTextHeight(textMeasuringElement, textElementWidth.roundToInt())
//
//                if((textHeightUsed + (leftMargin + topMargin)) < textElementHeight) {
//                    currentTextElement.setText(SpannableStringBuilder(currentTextElement.getText()?:"").append('\n').append(line))
//                } else {
//                    currentTextElement applyLeftTopMargin leftMargin.toFloat()
//                    currentTextElement.desiredHeight = maxHeight.toFloat()
//                    currentTextElement.symmetric = true
//                    currentDocumentPage.elements.add(currentTextElement)
//                    currentDocumentPage.setUseScalingFactorForSnapshot(false)
//                    document!!.addPage(currentDocumentPage)
//                    currentDocumentPage = DocumentPage(++pageUniqueId, originalSize = pageSize)
//                    currentTextElement = SimpleTextElement(currentDocumentPage)
//                    currentTextElement.setText(line.toSpannable())
//                    textHeightUsed = 0
//                    textMeasuringElement.setText("".toSpannable())
//                }
//            }
//
//            if(textHeightUsed < maxHeight) {
//                currentTextElement applyLeftTopMargin leftMargin.toFloat()
//                currentTextElement.desiredHeight = maxHeight.toFloat()
//                currentTextElement.symmetric = true
//                currentDocumentPage.elements.add(currentTextElement)
//                currentDocumentPage.setUseScalingFactorForSnapshot(false)
//                document!!.addPage(currentDocumentPage)
//            }
            if (loadInRenderView) {
                renderView.loadDocument(document!!, onFinish)
            } else {
                handler.post(onFinish)
            }
        }
    }


}