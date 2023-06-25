package com.tanodxyz.documentrenderer.elements

import com.tanodxyz.documentrenderer.page.DocumentPage

class PageElementContainer(page: DocumentPage) : PageElementImpl(page) {
    protected val elements:MutableList<PageElementImpl> = mutableListOf()
}