package com.tanodxyz.generaldocumentrenderer.fileReader

import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import androidx.core.text.toSpannable
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.Size
import com.tanodxyz.documentrenderer.document.Document
import com.tanodxyz.documentrenderer.elements.SimpleTextElement
import com.tanodxyz.documentrenderer.page.DocumentPage
import com.tanodxyz.documentrenderer.pagesizecalculator.FixPageSizeCalculator
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.concurrent.thread


class FileReader(val renderView: DocumentRenderView) {

    private var document:Document? = null
    fun prepareDocument(loadInRenderView: Boolean, onFinish: () -> Unit) {
        val handler = Handler(Looper.getMainLooper())
        thread {
            val displayMetrics = renderView.resources.displayMetrics
            var linesList: List<String>? = null
            BufferedReader(InputStreamReader(renderView.resources.assets.open("ideas.txt"))).useLines {
                linesList = it.toList()
            }

            document = Document(renderView.context,FixPageSizeCalculator())
            document?.swipeVertical = true
            document?.documentFitPagePolicy = Document.PageFitPolicy.BOTH
            document?.fitEachPage = true


            var pageUniqueId = 0
            val maxHeight = displayMetrics.heightPixels
            val maxWidth = displayMetrics.widthPixels

            val pageSize = Size(maxWidth,maxHeight)

            var currentDocumentPage = DocumentPage(uniqueId = pageUniqueId, originalSize = pageSize)
            var currentTextElement = SimpleTextElement(currentDocumentPage)
            val textMeasuringElement = SimpleTextElement(currentDocumentPage)
            var textHeightUsed = 0

            linesList?.forEach { line->
                textMeasuringElement.setText(line.toSpannable())
                val lineTextHeight =
                    SimpleTextElement.calculateTextHeight(textMeasuringElement, maxWidth)
                textHeightUsed += lineTextHeight
                if(textHeightUsed < maxHeight) {
                    currentTextElement.setText(SpannableStringBuilder(currentTextElement.getText()?:"").append('\n').append(line))
                } else {
                    textHeightUsed = 0
                    currentTextElement.margins.apply {
                        left = 32F
                        top = 32F
                    }
                    currentTextElement.symmetric = true
                    currentDocumentPage.elements.add(currentTextElement)
                    currentDocumentPage.setUseScalingFactorForSnapshot(false)
                    document!!.addPage(currentDocumentPage)
                    currentDocumentPage = DocumentPage(++pageUniqueId, originalSize = pageSize)
                    currentTextElement = SimpleTextElement(currentDocumentPage)
                }
            }
            if(textHeightUsed < maxHeight) {
                currentTextElement.symmetric = true
                currentDocumentPage.elements.add(currentTextElement)
                currentDocumentPage.setUseScalingFactorForSnapshot(false)
                document!!.addPage(currentDocumentPage)
            }
            if(loadInRenderView) {
                renderView.loadDocument(document!!,onFinish)
            } else {
                handler.post(onFinish)
            }
        }
    }


}