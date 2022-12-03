package com.tanodxyz.documentrenderer.page

enum class PageVisibility {
    VISIBLE, PARTIALLY_VISIBLE, INVISIBLE;

    fun isCompletelyVisible(): Boolean {
        return this == VISIBLE
    }

    fun isInvisible(): Boolean {
        return this == INVISIBLE
    }

    fun isPartiallyVisible(): Boolean {
        return this == PARTIALLY_VISIBLE
    }
}