package com.tanodxyz.documentrenderer.elements

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.SparseArray
import androidx.core.graphics.toRect
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.getHeight
import com.tanodxyz.documentrenderer.getWidth
import com.tanodxyz.documentrenderer.page.DocumentPage
import com.tanodxyz.documentrenderer.page.PageSnapshotElement
import com.tanodxyz.documentrenderer.recycleSafely
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt

class PageSnapShotElementImpl(
    page: DocumentPage,
) : PageSnapshotElement(page) {
    private var future: Future<*>? = null
    private var canvas:Canvas? = null
    private val busyCreatingSnap = AtomicBoolean(false)
    private var bitmap: Bitmap? = null
    private var scaleLevel = 0f
    private val lock = Any()
    private val executor = page.documentRenderView.executor
    private var args = SparseArray<Any>(5).apply {
        this[DocumentPage.RE_DRAW_WITH_RELATIVE_TO_ORIGIN_SNAPSHOT_] = true
    }
    private val createSnap = { scaleLevel: Float ->
        future = executor?.submit {
            create(scaleLevel)
        }
    }

    override fun preparePageSnapshot(
        scaleLevel: Float,
    ) {
        if (busyCreatingSnap.get()) {
            future?.cancel(true)
        }
        createSnap(scaleLevel)
    }

    @Synchronized
    override fun isEmpty(): Boolean {
        return bitmap == null

    }

    override fun draw(canvas: Canvas, args: SparseArray<Any>?) {
        super.draw(canvas, args)
        canvas.apply {
            bitmap?.also { snap ->
                if (!busyCreatingSnap.get()) {
                    synchronized(lock) {
                        drawBitmap(snap, null, page.pageBounds.toRect(), null)
                    }
                }
            }
        }
    }

    private fun create(scaleLevel: Float) {
        busyCreatingSnap.set(true)
        if (scaleLevel != this.scaleLevel || bitmap == null) {
            recycle()
            this.scaleLevel = scaleLevel
            val pageBounds = page.pageBounds
            val bitmap = Bitmap.createBitmap(
                pageBounds.getWidth().div(DocumentRenderView.PAGE_SNAPSHOT_SCALE_DOWN_FACTOR)
                    .roundToInt(),
                pageBounds.getHeight().div(DocumentRenderView.PAGE_SNAPSHOT_SCALE_DOWN_FACTOR)
                    .roundToInt(),
                Bitmap.Config.ARGB_8888
            )
            synchronized(lock) {
                canvas = Canvas(bitmap)
                this.bitmap = bitmap
            }
            canvas?.let { page.dispatchDrawCallToIndividualElements(it, args) }
        }
        busyCreatingSnap.set(false)
    }

    private fun keyForBitmapCache(page: DocumentPage): String {
        return "snap-${page.uniqueId}"
    }

    @Synchronized
    override fun recycle() {
        super.recycle()
        canvas?.setBitmap(null)
        bitmap.recycleSafely()
        bitmap = null
        canvas = null
    }
}