package com.tanodxyz.generaldocumentrenderer.pdfparsing

import android.content.Context
import com.tanodxyz.documentrenderer.closeResource
import com.tanodxyz.generaldocumentrenderer.pdfparsing.ContentProcessor.printLocation
import com.tanodxyz.itext722g.kernel.geom.Rectangle
import com.tanodxyz.itext722g.kernel.pdf.PdfDocument
import com.tanodxyz.itext722g.kernel.pdf.PdfReader
import com.tanodxyz.itext722g.kernel.pdf.canvas.parser.PdfCanvasProcessor
import com.tanodxyz.itext722g.kernel.pdf.canvas.parser.data.TextRenderInfo
import com.tanodxyz.itext722g.kernel.pdf.canvas.parser.listener.TextChunk
import java.io.InputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PdfParser(val context: Context) {
    private val executor: ExecutorService = Executors.newCachedThreadPool()

    fun openDocument(assetsName: String) {
        executor.submit {
            var docStream: InputStream? = null
            try {
                docStream = context.assets.open(assetsName)
                val reader = PdfReader(docStream)
                val currentTimeMillis = System.currentTimeMillis()
                val pdfDocument = PdfDocument(reader)
                for (i: Int in 1..3/*pdfDocument.numberOfPages*/) {
                    val contentProcessor = ContentProcessor()
                    val parser = PdfCanvasProcessor(contentProcessor, HashMap())
                    parser.processPageContent(pdfDocument.getPage(i))
                    contentProcessor.processContent()
                    printSingleLineAndBoundsArrayContents(contentProcessor.textAndBoundsArray)
                }
                println("TIME = ${(System.currentTimeMillis() - currentTimeMillis) / 1000F}")
                pdfDocument.closeResource()
                docStream.closeResource()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun close() {
        executor.shutdownNow()
    }

    fun printSingleLineAndBoundsArrayContents(textAndBoundsArray: java.util.HashMap<String, ArrayList<TextChunk>>) {
        for (mutableEntry in textAndBoundsArray) {
            val singleLine: String = mutableEntry.key
            println("Bako: ------LINE__START")
            println("Bako: lineText= $singleLine")
            val textChunksForSingleLine: java.util.ArrayList<TextChunk> = mutableEntry.value
            println("Bako: line text chunks below")
            println()
            println('-')
            for (textChunk in textChunksForSingleLine) {
                println()
                println("Bako: " + textChunk.getText())
                printLocation(textChunk)
                println()
                println()
                println('-')
                println()
                println()
            }
            println("Bako: ------LINE__END\n")
        }
    }
}