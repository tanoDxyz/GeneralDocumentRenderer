package com.tanodxyz.documentrenderer.page

import com.tanodxyz.documentrenderer.elements.PageElement

abstract class PageSnapshotElement(page: DocumentPage):PageElement(page) {
    abstract fun preparePageSnapshot(scaleLevel:Float)
    abstract fun isEmpty():Boolean
}