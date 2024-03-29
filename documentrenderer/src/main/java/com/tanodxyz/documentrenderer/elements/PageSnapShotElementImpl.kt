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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * An implementation of [PageSnapshotElement] that create page snapshots on background thread.
 * while creating snapshot of pages this class uses a simple technique which is called
 * **Snapshot dimension ranges**
 * >
 * ### Snapshot Dimension Ranges ###
 * It is a List of [Pair]s.
 * As [DocumentRenderView] has [DocumentRenderView.MAXIMUM_ZOOM] level.
 * so basically we start from 0 until we reaches [DocumentRenderView.MAXIMUM_ZOOM] and
 * for each zoom/scale level we create a **Pair - (ScaleDownFactor,Page Size Range).**
 * >
 *### what is Pair (scaleDownFactor, PageSizeRange)?
 *
 * ***scaleDownFactor is the floating point value which will be used to divide the page.width and
 * page.height i.e. `snapShotWidth = PageWidth/scaleDownFactor , snapShotHeight = pageHeight/scaleDownFactor`***
 *
 * ***PageSizeRange is the range which has two values - `start , end` and in simple terms it is the size range for which scaleDownFactor will be used.***
 *
 */
open class PageSnapShotElementImpl(
    page: DocumentPage,
) : PageSnapshotElement(page) {
    protected var future: Future<*>? = null
    protected var canvas: Canvas? = null
    protected val busyCreatingSnap = AtomicBoolean(false)
    protected var bitmap: Bitmap? = null
    protected var scaleLevel = 0f
    protected val lock = Any()
    protected var useScalingForSnapshot = true
    private var args = SparseArray<Any>(5).apply {
        this[DocumentPage.RE_DRAW_WITH_RELATIVE_TO_ORIGIN_SNAPSHOT_] = true
    }

    override fun getBitmap(scaleDown: Boolean, callback: (Bitmap?) -> Unit) {
        val pageBounds = page.pageBounds
        val threadPool = page.documentRenderView.threadPoolExecutor
        if (threadPool == null) {
            callback(null)
        } else {
            threadPool.submit {
                var sdFactor = 1F
                if(scaleDown) {
                    val pageMaxSize =
                        max(pageBounds.getWidth(), pageBounds.getHeight()).roundToInt()
                    if(useScalingForSnapshot) {
                        sdFactor = getScaleDownFactor(pageMaxSize)
                    }
                }

                val bitmap = Bitmap.createBitmap(
                    pageBounds.getWidth().div(sdFactor)
                        .roundToInt(),
                    pageBounds.getHeight().div(sdFactor)
                        .roundToInt(),
                    Bitmap.Config.ARGB_8888
                )
                canvas = Canvas(bitmap)
                this.bitmap = bitmap
                canvas?.let { page.dispatchDrawCallToIndividualElements(it, args) }
                callback(bitmap)
            }
        }
    }

    override fun useScalingForSnapshot(useScalingForSnapshot: Boolean) {
        this.useScalingForSnapshot = useScalingForSnapshot
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

    open fun getScaleDownFactor(pageMaxSize: Int): Float {
        // scale down the algorithm.
        var sdFactor = 1F
        if (pageMaxSize > snapDimenRanges.last().second.last) {
            val displayMetrics = page.documentRenderView.context.resources.displayMetrics
            sdFactor = pageMaxSize / min(
                displayMetrics.widthPixels,
                displayMetrics.heightPixels
            ).toFloat()
        } else {
            loop@ for (pair: Pair<Float, IntRange> in snapDimenRanges) {
                if (pageMaxSize in pair.second) {
                    sdFactor = pair.first
                    break@loop
                }
            }
        }
        return sdFactor
    }

    protected fun create(scaleLevel: Float) {
        busyCreatingSnap.set(true)
        if (scaleLevel != this.scaleLevel || bitmap == null) {
            if (snapDimenRanges.isEmpty()) {
                setSnapDimenRanges()
            }
            recycle()
            this.scaleLevel = scaleLevel
            val pageBounds = page.pageBounds
            val pageMaxSize = max(pageBounds.getWidth(), pageBounds.getHeight()).roundToInt()
            val sdFactor: Float = if(useScalingForSnapshot) {
                getScaleDownFactor(pageMaxSize)
            } else {
                1F
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
        canvas?.setBitmap(null)
        bitmap.recycleSafely()
        bitmap = null
        canvas = null
    }

    companion object {
        val snapDimenRanges: MutableList<Pair<Float, IntRange>> =
            mutableListOf()
        @Synchronized
        fun setSnapDimenRanges(
            level: Int = DocumentRenderView.MAXIMUM_ZOOM.roundToInt(),
            pageStartSize: Int = 500,
            pageSizeDifference: Int = 1000,
            divisionFactor: Float = 1.5F
        ) {
            for (i: Int in 0 until level) {
                val i1 =
                    if (i == 0) pageStartSize else snapDimenRanges[(i - 1)].second.last + 1
                val df = divisionFactor + i
                val pair = Pair(
                    df,
                    IntRange(i1, (pageSizeDifference * (i + 1)))
                )
                snapDimenRanges.add(pair)
            }
            DocumentRenderView.MAXIMUM_ZOOM = level.toFloat()
        }
    }
}