package com.tanodxyz.documentrenderer.extensions

import com.tanodxyz.documentrenderer.DocumentRenderView

interface ViewExtension {
    fun attachTo(view: DocumentRenderView, onLayout: (() -> Unit)? = null)
    fun detach()
    fun show()
    fun hide(delayed: Boolean = false)
}