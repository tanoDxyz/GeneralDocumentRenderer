package com.tanodxyz.documentrenderer.events


class EventManager {
    private val handlers: MutableSet<IEventHandler> = LinkedHashSet()

    /**
     * Handles the event.
     *
     * @param event to handle
     */
    fun onEvent(event: IMotionEventMarker) {
        val caughtExceptions: MutableList<RuntimeException> = ArrayList()
        for (handler in handlers) {
            try {
                handler.onEvent(event)
            } catch (ex: RuntimeException) {
                caughtExceptions.add(ex)
            }
        }
    }

    /**
     * Add new [IEventHandler] to the event handling process.
     *
     * @param handler is a handler to add
     */
    fun register(handler: IEventHandler?) {
        if (handler != null) {
            handlers.add(handler)
        }
    }

    /**
     * Check if the handler was registered for event handling process.
     *
     * @param handler is a handler to check
     * @return true if handler has been already registered and false otherwise
     */
    fun isRegistered(handler: IEventHandler?): Boolean {
        return if (handler != null) {
            handlers.contains(handler)
        } else false
    }

    /**
     * Removes handler from event handling process.
     *
     * @param handler is a handle to remove
     * @return true if the handler had been registered previously and was removed. False if the
     * handler was not found among registered handlers
     */
    fun unregister(handler: IEventHandler?): Boolean {
        return if (handler != null) {
            handlers.remove(handler)
        } else false
    }

    fun close() {
        handlers.clear()
    }
}
