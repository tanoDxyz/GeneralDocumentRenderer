package com.tanodxyz.generaldocumentrenderer.pdfRenderer

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
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
import com.tanodxyz.generaldocumentrenderer.pdfRenderer.PdfElement.Companion.isValid
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Stack
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.LockSupport
import kotlin.math.abs
import kotlin.math.roundToInt


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)

class PDFRenderer(val renderView: DocumentRenderView) {
    private var parcelFileDescriptor: ParcelFileDescriptor? = null
    private var pdfRenderer: PdfRenderer? = null
    private var handler = Handler(Looper.getMainLooper())
    private var pageRendererThread = PageRendererThread()

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
        pageRendererThread.start()
    }

    @Synchronized
    fun close() {
        pdfRenderer?.close()
        parcelFileDescriptor?.closeResource()
        pageRendererThread.shutDown()
        handler.removeCallbacksAndMessages(null)
    }

    @Synchronized
    fun getPageCount(): Int {
        return pdfRenderer?.pageCount ?: 0
    }


    inner class PageRendererThread : java.lang.Thread("PageRenderThread") {
        private val requestStack = Stack<PageRenderOperationWrapper>()
        private val iCanRun = AtomicBoolean(true)
        override fun run() {
            while (iCanRun.get()) {
                LockSupport.park(this)
                synchronized(requestStack) {
                    val mostRecentPageRequestNumber = if(requestStack.empty().not()) requestStack.peek().pageNumber else -1
                    while (requestStack.empty().not()) {
                        val request = requestStack.pop()
                        if(mostRecentPageRequestNumber <=  -1 ||
                            abs(request.pageNumber - mostRecentPageRequestNumber) > 10) {
                            return@synchronized
                        }
                        if (request.isValid()) {
                            val pageBitmap = getPageBitmapFromCacheOrCreateNew(
                                request.pageNumber,
                                request.pageBounds
                            )
                            postToHandler {
                                if (pageBitmap.isValid()) {
                                    request.callback?.invoke(pageBitmap!!)
                                }
                            }
                        }
                    }
                    requestStack.clear()
                }
            }
        }

        fun postToHandler(callback: () -> Unit) {
            handler.post(callback)
        }

        fun shutDown() {
            iCanRun.set(false)
            LockSupport.unpark(this)
        }

        fun loadPage(pageNumber: Int, pageBounds: RectF, callback: (Bitmap?) -> Unit) {
            renderView.threadPoolExecutor?.submit {
                synchronized(requestStack) {
                    PageRenderOperationWrapper(pageNumber, pageBounds).apply {
                        if(requestStack.contains(this)) {
                            println("marko: duplicate of $this")
                            return@submit
                        }
                        this.callback = callback
                        requestStack.push(this)
                        LockSupport.unpark(this@PageRendererThread)
                    }
                }
            }
        }
    }

    fun loadPageNew(pageNumber: Int, pageBounds: RectF, callback: (Bitmap?) -> Unit) {
        pageRendererThread.loadPage(pageNumber, pageBounds, callback)
    }


    private fun getPageBitmapFromCacheOrCreateNew(pageNo: Int, pageBounds: RectF): Bitmap? {
        val blob = getFromCache(pageNo, pageBounds)
        val bitmap = if (!blob.isValid()) {
            createNewPage(pageNo, pageBounds)
        } else {
            blob?.getPayLoad() as Bitmap
        }
        return bitmap
    }

    fun getFromCache(pageNumber: Int, pageBounds: RectF): BitmapBlob? {
        val cache = renderView.cache
        return cache.get(key(pageNumber, pageBounds)) as BitmapBlob?
    }


    @Synchronized
    fun createNewPage(pageNumber: Int, pageBounds: RectF, putInCache: Boolean = true): Bitmap? {
        var bitmap: Bitmap? = null
        var scaledBitmap: Bitmap? = null
        pdfRenderer?.apply {
            val page = openPage(pageNumber)
            val pdfPageWidth = page.width
            val pdfPageHeight = page.height
            val desiredWidth = (pageBounds.getWidth())
            val desiredHeight = (pageBounds.getHeight())
            if (desiredWidth > 0 && desiredHeight > 0) {
                val matrix = Matrix()
                matrix.setTranslate(0F, 0F)
                matrix.postScale(desiredWidth / pdfPageWidth, desiredHeight / pdfPageHeight)
                // Create a bitmap with the desired dimensions
                bitmap = Bitmap.createBitmap(
                    renderView.resources.displayMetrics,
                    desiredWidth.roundToInt(),
                    desiredHeight.roundToInt(),
                    Bitmap.Config.ARGB_8888
                )
                // Render the page with the transformation matrix
                page.render(bitmap!!, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
//                val compressedBitmap = Bitmap.createBitmap(
//                    bitmap!!.getWidth(),
//                    bitmap!!.getHeight(),
//                    Bitmap.Config.ARGB_8888
//                )
//                val canvas = Canvas(compressedBitmap)
//                canvas.drawBitmap(bitmap!!, 0F, 0F, null)
//                val bos = ByteArrayOutputStream()
//                bitmap!!.compress(Bitmap.CompressFormat.JPEG, PAGE_JPEG_IMAGE_QUALITY, bos)
                scaledBitmap = bitmap
//                    BitmapFactory.decodeStream(ByteArrayInputStream(bos.toByteArray()))
//                bos.close()
//                bitmap.recycleSafely()
//                compressedBitmap.recycleSafely()
                if (putInCache) {
                    renderView.cache.offer(BitmapBlob(key(pageNumber, pageBounds), scaledBitmap))
                }
            }
            page.close()
        }
        return scaledBitmap
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

    fun remove(key: String) {
        renderView.threadPoolExecutor?.submit {
            renderView.cache.remove(key)
        }
    }

    class BitmapBlob(private val uniqueID: String, private var bitmap: Bitmap?) :
        CacheManager.Blob {

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
            bitmap.recycleSafely()
            bitmap = null
        }

        override fun getPayLoad(): Any? {
            return bitmap
        }
    }

    class PageRenderOperationWrapper(val pageNumber: Int, val pageBounds: RectF) {
        var callback: ((Bitmap) -> Unit)? = null
        fun isValid(): Boolean {
            return pageNumber > -1 && pageBounds.getWidth() > 0 && pageBounds.getHeight() > 0
        }
    }

    companion object {
        const val PAGE_JPEG_IMAGE_QUALITY = 30
        fun key(pageNumber: Int, pageBounds: RectF): String {
            return "$pageNumber;${pageBounds.getWidth()}- ${pageBounds.getHeight()}"
        }

        fun BitmapBlob?.isValid(): Boolean {
            return this != null && this.getPayLoad() != null && this.getPayLoad() is Bitmap && (!(this.getPayLoad() as Bitmap).isRecycled)
        }
    }
}