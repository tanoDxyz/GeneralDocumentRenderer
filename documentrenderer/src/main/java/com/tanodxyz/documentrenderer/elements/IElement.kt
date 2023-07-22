package com.tanodxyz.documentrenderer.elements

import android.graphics.Canvas
import android.util.SparseArray


interface IElement {
    fun draw(canvas: Canvas, args: SparseArray<Any>? = null)
}