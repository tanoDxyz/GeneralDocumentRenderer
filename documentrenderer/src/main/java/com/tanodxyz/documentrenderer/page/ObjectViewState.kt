package com.tanodxyz.documentrenderer.page

enum class ObjectViewState {
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

    fun isObjectPartiallyOrCompletelyVisible():Boolean {
        return this == VISIBLE || this == PARTIALLY_VISIBLE
    }
}