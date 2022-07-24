package com.tanodxyz.documentrenderer

import android.graphics.Canvas
import java.util.*
import java.util.concurrent.locks.LockSupport

class DefaultReDrawer() : ReDrawer, Runnable {
    @Volatile
    private var rendererState: RendererState = RendererState.DEAD
    private var thread: Thread? = null
    private var drawingSource: DrawingSource? = null

    @Synchronized
    override fun start() {
        if (isAlive()) {
            return
        }
        rendererState = RendererState.STARTED
        thread = Thread(this, "Renderer-${System.currentTimeMillis()}")
        thread?.start()
    }

    @Synchronized
    override fun setSurface(drawingSource: DrawingSource) {
        this.drawingSource = drawingSource
    }

    @Synchronized
    override fun pause() {
        rendererState.apply {
            if (this == RendererState.STARTED) {
                rendererState = RendererState.PAUSE
                println("Running: paused at  ${Date(System.currentTimeMillis())}")
            }
        }
    }

    @Synchronized
    override fun resume() {
        rendererState.apply {
            if (this == RendererState.PAUSE) {
                rendererState = RendererState.STARTED
                LockSupport.unpark(thread)
                println("Running: resumed at  ${Date(System.currentTimeMillis())}")
            }
        }
    }

    @Synchronized
    override fun stop() {
        if (rendererState == RendererState.DEAD) {
            return
        } else {
            val t = rendererState
            rendererState = RendererState.DEAD
            if (t == RendererState.PAUSE) {
                LockSupport.unpark(thread)
            }
            try {
                thread?.join()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    @Synchronized
    override fun isRunning(): Boolean {
        return rendererState == RendererState.STARTED
    }

    @Synchronized
    override fun isPaused(): Boolean {
        return rendererState == RendererState.PAUSE
    }

    @Synchronized
    override fun isAlive(): Boolean = isPaused() || isRunning()

    @Synchronized
    override fun isDead(): Boolean = rendererState == RendererState.DEAD
    override fun run() {

        while (rendererState != RendererState.DEAD) {
            var canvas: Canvas? = null
            val surfaceHolder = drawingSource?.getSurfaceHolder()
            surfaceHolder?.apply {
                try {
                    canvas = surfaceHolder.lockCanvas()
                    synchronized(surfaceHolder) {
                        drawingSource?.onDrawFrame(canvas)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                } finally {
                    canvas?.apply {
                        surfaceHolder.unlockCanvasAndPost(this)
                    }
                }
            }
            if (rendererState == RendererState.PAUSE) {
                LockSupport.park()
            }
        }
    }

}