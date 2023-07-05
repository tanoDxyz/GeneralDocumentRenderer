package com.tanodxyz.generaldocumentrenderer.pdfRenderer

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import androidx.annotation.RequiresApi
import com.tanodxyz.documentrenderer.CacheManager
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.Size
import com.tanodxyz.documentrenderer.closeResource
import com.tanodxyz.documentrenderer.getHeight
import com.tanodxyz.documentrenderer.getWidth
import com.tanodxyz.documentrenderer.recycleSafely
import java.io.File
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)

class PDFRenderer(val renderView: DocumentRenderView) {
    private var parcelFileDescriptor: ParcelFileDescriptor? = null
    private var pdfRenderer: PdfRenderer? = null
    private var handler = Handler(Looper.getMainLooper())

    @Synchronized
    fun openPdfFile(filePath: String) {
        val fileDescriptor =
            ParcelFileDescriptor.open(File(filePath), ParcelFileDescriptor.MODE_READ_ONLY)
        val parcelFileDescriptor = fileDescriptor.dup()
        readPdfFile(parcelFileDescriptor)
        fileDescriptor.close()
    }

    @Synchronized
    fun openPdfFromAssets(
        assetManager: AssetManager,
        fileName: String
    ) {
        val fileDescriptor = assetManager.openFd(fileName)
        val parcelFileDescriptor = fileDescriptor.parcelFileDescriptor.dup()
        readPdfFile(parcelFileDescriptor)
        fileDescriptor.close()
    }

    private fun readPdfFile(parcelFileDescriptor: ParcelFileDescriptor) {
        pdfRenderer = PdfRenderer(parcelFileDescriptor)
        this.parcelFileDescriptor = parcelFileDescriptor
    }

    @Synchronized
    fun close() {
        pdfRenderer?.close()
        parcelFileDescriptor?.closeResource()
        handler.removeCallbacksAndMessages(null)
    }

    @Synchronized
    fun getPageCount(): Int {
        return pdfRenderer?.pageCount ?: 0
    }

    var currentPageAndRemovablePagesDifference =
        10 // from current page if page is at position (currPage - 10) or (currPage + 10) remove it from cache
    var maxPageRenderingOperations = 16
    var runningPageRenderingOperationsCount = AtomicInteger(0)
    var runningPageRenderingOperations = mutableListOf<Pair<String, Future<*>?>>()
    fun loadPage(pageNo: Int, pageBounds: RectF, callback: (Bitmap?) -> Unit) {
        renderView.threadPoolExecutor?.submit {
            if (runningPageRenderingOperationsCount.get() < maxPageRenderingOperations) {
                fetchPage(pageNo, pageBounds, callback)
            } else {
                removeRunningPageRenderCallbacks(pageNo)
                fetchPage(pageNo, pageBounds, callback)
            }
        }
    }

    private fun removeRunningPageRenderCallbacks(pageNumber: Int) {
        synchronized(runningPageRenderingOperations) {
            val removablePageRenderingCallbacks = mutableListOf<Pair<String, Future<*>?>>()
            runningPageRenderingOperations.forEach { pair ->
                val (key, runningOp) = pair
                if (runningOp != null && (runningOp.isDone || runningOp.isCancelled)) {
                    removablePageRenderingCallbacks.add(pair)
                }
                val (runningOperationPageNumber, _) = key.pageNumberScaleLevelPair()
                if (abs(runningOperationPageNumber - pageNumber) >= currentPageAndRemovablePagesDifference) {
                    runningOp?.cancel(true)
                    removablePageRenderingCallbacks.add(pair)
                }
            }
            runningPageRenderingOperations.removeAll(removablePageRenderingCallbacks)
        }

    }

    private fun fetchPage(pageNo: Int, pageBounds: RectF, callback: (Bitmap?) -> Unit) {
        val rpro = renderView.threadPoolExecutor?.submit {
            runningPageRenderingOperationsCount.incrementAndGet()
            val pageBitmap =
                getPageBitmapFromCacheOrCreateNew(pageNo, pageBounds)
            handler.post { callback(pageBitmap) }
            runningPageRenderingOperationsCount.decrementAndGet()
        }
        synchronized(runningPageRenderingOperations) {
            runningPageRenderingOperations.add(Pair(key(pageNo, pageBounds), rpro))
        }

    }

    private fun getPageBitmapFromCacheOrCreateNew(pageNo: Int, pageBounds: RectF): Bitmap? {
        val blob = getFromCache(pageNo, pageBounds)
        val bitmap = if (blob == null) {
            createNewPage(pageNo, pageBounds)
        } else {
            blob.getPayLoad() as Bitmap
        }
        return bitmap
    }

    private fun key(pageNumber: Int, pageBounds: RectF): String {
        return "$pageNumber;$pageBounds"
    }

    private fun String.pageNumberScaleLevelPair(): Pair<Int, Float> {
        val scIdx = indexOf(';')
        return if (scIdx > -1) {
            val pageNumber = this.substring(0, scIdx).toInt()
            val scaleLevel = this.substring((scIdx + 1), this.length).toFloat()
            Pair(pageNumber, scaleLevel)
        } else {
            Pair(0, 0F)
        }
    }

    fun getFromCache(pageNumber: Int, pageBounds: RectF): BitmapBlob? {
        val cache = renderView.cache
        return cache.get(key(pageNumber, pageBounds)) as BitmapBlob?
    }

    @Synchronized
    fun createNewPage(pageNumber: Int, pageBounds: RectF, putInCache: Boolean = true): Bitmap? {
        var bitmap: Bitmap? = null
        pdfRenderer?.apply {
            val page = openPage(pageNumber)
            val pdfPageWidth = page.width
            val pdfPageHeight = page.height
            val desiredWidth = pageBounds.getWidth()
            val desiredHeight = pageBounds.getHeight()

            val matrix = Matrix();
            matrix.setTranslate(0F, 0F)
            matrix.postScale(desiredWidth / pdfPageWidth, desiredHeight / pdfPageHeight)
            // Create a bitmap with the desired dimensions
            bitmap = Bitmap.createBitmap(
                desiredWidth.roundToInt(),
                desiredHeight.roundToInt(),
                Bitmap.Config.ARGB_8888
            )
            // Render the page with the transformation matrix
            page.render(bitmap!!, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            page.close()

            if (putInCache) {
                renderView.cache.offer(BitmapBlob(key(pageNumber, pageBounds), bitmap))
            }
        }
        return bitmap
    }

    @Synchronized
    fun getPageSizes(threadMain: Boolean = false, callback: (MutableList<Size>) -> Unit) {
        renderView.threadPoolExecutor?.submit {
            val pageSizes = mutableListOf<Size>()
            for (i: Int in 0 until getPageCount()) {
                val page = pdfRenderer?.openPage(i)
                page?.let { pageSizes.add(Size(page.width, page.height)) }
                page?.close()
            }
            val c = { callback(pageSizes) }
            if (threadMain) {
                handler.post(c)
            } else {
                c.invoke()
            }
        }
    }

    class BitmapBlob(private val uniqueID: String, private var bitmap: Bitmap?) :
        CacheManager.Blob {
        init {
            println("blober: created $uniqueID")
        }

        override fun getUniqueID(): String {
            return uniqueID
        }

        override fun getSize(): Int {
            return if (bitmap != null) {
                (bitmap!!.allocationByteCount) / 1024
            } else {
                0
            }
        }

        override fun onRemove() {
            println("blobler: remove $uniqueID")
            bitmap.recycleSafely()
            bitmap = null
        }

        override fun getPayLoad(): Any? {
            return bitmap
        }

    }
}