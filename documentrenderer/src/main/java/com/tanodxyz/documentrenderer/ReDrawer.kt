package com.tanodxyz.documentrenderer

interface ReDrawer {
    fun setSurface(drawingSource:DrawingSource)
    fun start()
    fun pause()
    fun resume()
    fun stop()
    fun isRunning():Boolean
    fun isPaused():Boolean
    fun isDead():Boolean
    fun isAlive():Boolean
}