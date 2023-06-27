package com.tanodxyz.documentrenderer.elements

import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.Thread
import com.tanodxyz.documentrenderer.events.IMotionEventMarker
import com.tanodxyz.documentrenderer.pagesizecalculator.PageSizeCalculator

interface InteractiveElement : IElement {
    fun onEvent(iMotionEventMarker: IMotionEventMarker?): Boolean
    fun reset()
    fun recycle()
    fun pageMeasurementDone(documentRenderView: DocumentRenderView)
}