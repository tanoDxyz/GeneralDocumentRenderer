package com.tanodxyz.documentrenderer.elements

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.SparseArray
import androidx.core.graphics.toRect
import com.tanodxyz.documentrenderer.getHeight
import com.tanodxyz.documentrenderer.getWidth
import com.tanodxyz.documentrenderer.page.DocumentPage
import com.tanodxyz.documentrenderer.page.PageSnapshotElement
import com.tanodxyz.documentrenderer.recycleSafely
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

open class PageSnapShotElementImpl(
    page: DocumentPage,
) : PageSnapshotElement(page) {
    protected var future: Future<*>? = null
    protected var canvas: Canvas? = null
    protected val busyCreatingSnap = AtomicBoolean(false)
    protected var bitmap: Bitmap? = null
    protected var scaleLevel = 0f
    protected val lock = Any()
    private var args = SparseArray<Any>(5).apply {
        this[DocumentPage.RE_DRAW_WITH_RELATIVE_TO_ORIGIN_SNAPSHOT_] = true
    }

    private fun createSnap(scaleLevel: Float) {
        future = page.documentRenderView.threadPoolExecutor?.submit {
            create(scaleLevel)
        }
    }

    override fun preparePageSnapshot(
        scaleLevel: Float,
    ) {
        if (busyCreatingSnap.get()) {
            return
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

    protected fun create(scaleLevel: Float) {
        busyCreatingSnap.set(true)
        if (scaleLevel != this.scaleLevel || bitmap == null) {
            recycle()
            this.scaleLevel = scaleLevel
            val pageBounds = page.pageBounds
            val pageMaxSize = max(pageBounds.getWidth(), pageBounds.getHeight()).roundToInt()
            var sdFactor:Float = 1F

            // scale down the algorithm.
            if(pageMaxSize > snapDimenRanges.last().second.last) {
                val displayMetrics = page.documentRenderView.context.resources.displayMetrics
                sdFactor = pageMaxSize / min(displayMetrics.widthPixels,displayMetrics.heightPixels).toFloat()
            } else {
                loop@ for(pair:Pair<Float,IntRange> in snapDimenRanges) {
                    if (pageMaxSize in pair.second) {
                        sdFactor = pair.first
                        break@loop
                    }
                }
            }
            page.snapScaleDownFactor = sdFactor
            val bitmap = Bitmap.createBitmap(
                pageBounds.getWidth().div(sdFactor)
                    .roundToInt(),
                pageBounds.getHeight().div(sdFactor)
                    .roundToInt(),
                Bitmap.Config.ARGB_8888
            )


            synchronized(lock) {
                canvas = Canvas(bitmap)
                this.bitmap = bitmap
                canvas?.let { page.dispatchDrawCallToIndividualElements(it, args) }
            }

        }
        busyCreatingSnap.set(false)
    }

    @Synchronized
    override fun recycle() {
        super.recycle()
        canvas?.setBitmap(null)
        bitmap.recycleSafely()
        bitmap = null
        canvas = null
    }

    companion object {
        val snapDimenRanges = mutableListOf<Pair<Float, IntRange>>().apply {
            addAll(
                arrayOf(
                    Pair(1.5f, IntRange(500, 1000)),
                    Pair(2.5f, IntRange(1001, 2000)),
                    Pair(3.5f, IntRange(2001, 3000)),
                    Pair(4.5f, IntRange(3001, 4000)),
                    Pair(5.5f, IntRange(4001, 5000)),
                    Pair(6.5f, IntRange(5001, 6000)),
                    Pair(7.5f, IntRange(6001, 7000)),
                    Pair(8.5f, IntRange(7001, 8000)),
                    Pair(9.5f, IntRange(8001, 9000)),
                    Pair(10.5f, IntRange(9001, 10000))
                )
            )
        }
    }
}