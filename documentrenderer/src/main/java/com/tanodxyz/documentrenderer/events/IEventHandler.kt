package com.tanodxyz.documentrenderer.events

interface IEventHandler {
    /**
     * Handles the event.
     *
     * @param event to handle
     */
    fun onEvent(event: IMotionEventMarker?):Boolean
}