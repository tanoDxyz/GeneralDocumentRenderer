package com.tanodxyz.documentrenderer

interface ViewExtension {
    fun attachTo(view: DocumentRenderView, onLayout: (() -> Unit)? = null)
    fun detach()
    fun show()
    fun hide(delayed: Boolean = false)
}