package com.tanodxyz.documentrenderer.elements

import android.graphics.Canvas
import android.util.SparseArray

/**
 * An element or graphic that can be drawn on canvas.
 */
interface IElement {
    fun draw(canvas: Canvas, args: SparseArray<Any>? = null)
}