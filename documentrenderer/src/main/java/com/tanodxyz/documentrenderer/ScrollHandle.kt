package com.tanodxyz.documentrenderer

interface ScrollHandle:ViewExtension {
    fun scroll(position: Float)
    fun setPageNumber(pageNumber:String)
}