package com.tanodxyz.documentrenderer

interface ViewExtension {
    fun attachTo(view: DocumentRenderView)
    fun detach()
    fun show()
    fun hide(delayed:Boolean = false)
}