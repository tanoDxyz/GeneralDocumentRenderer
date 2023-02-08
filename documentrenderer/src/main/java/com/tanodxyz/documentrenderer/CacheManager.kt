package com.tanodxyz.documentrenderer

import android.graphics.Bitmap
import android.os.Bundle
import android.util.LruCache
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
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


    interface Blob {
        fun getUniqueID(): String
        fun getSize(): Int
        fun onRemove()
        fun <T> getPayLoad(): T
    }
}