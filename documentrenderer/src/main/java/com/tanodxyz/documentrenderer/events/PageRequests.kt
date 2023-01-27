package com.tanodxyz.documentrenderer.events

interface PageRequests {
    fun redraw()
    fun enableDisableScroll(enable: Boolean)
    fun stopFling()
    fun enableDisableFling(enable: Boolean)
    fun enableDisableScale(enable: Boolean)
    fun getCurrentX(): Float
    fun getCurrentY(): Float
    fun setCurrentX(x: Float)
    fun setCurrentY(y: Float)
    fun getPageCount(): Int
    fun jumpToPage(pageNumber: Int, withAnimation: Boolean = false)
}