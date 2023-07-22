package com.tanodxyz.documentrenderer.elements

import android.graphics.RectF
import android.util.SparseArray
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.Thread
import com.tanodxyz.documentrenderer.events.IEventHandler
import com.tanodxyz.documentrenderer.events.IMotionEventMarker
import com.tanodxyz.documentrenderer.pagesizecalculator.PageSizeCalculator

interface InteractiveElement : IElement,IEventHandler {
    fun getContentWidth(args: SparseArray<Any>?): Float
    fun getContentHeight(args: SparseArray<Any>?): Float
    fun getScaledMargins(drawSnapShot: Boolean): RectF
    fun setContentBounds(bounds: RectF)
    fun getContentBounds(drawSnapShot: Boolean): RectF

    fun reset()
    fun recycle()
    fun pageMeasurementDone(documentRenderView: DocumentRenderView){}
    fun onScale(currentZoom: Float){}
}