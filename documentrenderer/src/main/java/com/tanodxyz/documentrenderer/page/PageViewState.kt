package com.tanodxyz.documentrenderer.page

import com.tanodxyz.documentrenderer.DragPinchManager
import com.tanodxyz.documentrenderer.TouchEventsManager

enum class PageViewState {
    VISIBLE, PARTIALLY_VISIBLE, INVISIBLE;


    fun isPageCompletelyVisible(): Boolean {
        return this == VISIBLE
    }

    fun isPageInvisible(): Boolean {
        return this == INVISIBLE
    }

    fun isPagePartiallyVisible(): Boolean {
        return this == PARTIALLY_VISIBLE
    }

    fun isPagePartiallyOrCompletelyVisible():Boolean {
        return this == VISIBLE || this == PARTIALLY_VISIBLE
    }
}