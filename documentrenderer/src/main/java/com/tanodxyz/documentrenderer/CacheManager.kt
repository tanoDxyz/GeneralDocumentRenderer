package com.tanodxyz.documentrenderer

import android.graphics.Bitmap
import android.os.Bundle
import android.util.LruCache
import java.util.UUID

class CacheManager(memoryFactor: Int) {
    private var lruCache: LruCache<String, Blob>

    init {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / memoryFactor
        lruCache = object : LruCache<String, Blob>(cacheSize) {
            override fun sizeOf(key: String?, value: Blob?): Int {
                return value?.getSize() ?: 0
            }

            override fun entryRemoved(
                evicted: Boolean,
                key: String?,
                oldValue: Blob?,
                newValue: Blob?
            ) {
                if (evicted) {
                    oldValue?.onRemove()
                }
                super.entryRemoved(evicted, key, oldValue, newValue)
            }
        }
    }

    @Synchronized
    fun offer(blob: Blob) {
        lruCache.put(blob.getUniqueID(), blob)
    }

    @Synchronized
    fun remove(key: String) {
        lruCache.remove(key)
    }

    @Synchronized
    fun recycle() {
        lruCache.evictAll()
    }

    @Synchronized
    fun get(key: String): Blob? {
        return lruCache.get(key)
    }

    @Synchronized
    override fun toString(): String {
        return lruCache.toString() + " -> createCount = ${lruCache.createCount()} -> evictCount = ${lruCache.evictionCount()} -> size${lruCache.size()}"
    }

    interface Blob {
        fun getUniqueID(): String

        /**
         * size in kbs
         */
        fun getSize(): Int
        fun onRemove()
        fun getPayLoad(): Any?
    }
}