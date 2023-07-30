package com.tanodxyz.documentrenderer.extensions

import com.tanodxyz.documentrenderer.DocumentRenderView

/**
 * As [DocumentRenderView] is a [android.widget.FrameLayout] and that's why child views can be attached to it.
 * this simple interface defines set of rules.
 */
interface ViewExtension {
    /**
     * This method will be invoked by [DocumentRenderView] when [DocumentRenderView]s [DocumentRenderView.attachViewExtension] is called.
     * Attach this [ViewExtension] to [DocumentRenderView] by calling proper [android.widget.FrameLayout.addView] methods.
     */
    fun attachTo(view: DocumentRenderView, onLayout: (() -> Unit)? = null)
    fun detach(view:DocumentRenderView)
    fun show()
    fun hide(delayed: Boolean = false)
}