package com.tanodxyz.documentrenderer

import android.graphics.Canvas
import android.view.SurfaceHolder

interface DrawingSource {
    fun onDrawFrame(canvas: Canvas?)
    fun getSurfaceHolder(): SurfaceHolder?
}