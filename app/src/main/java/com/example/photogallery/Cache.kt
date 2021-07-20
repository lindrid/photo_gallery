package com.example.photogallery

import android.graphics.Bitmap
import androidx.collection.LruCache

class Cache private constructor() {

  private object HOLDER {
    val INSTANCE = Cache()
  }

  companion object {
    val instance: Cache by lazy { HOLDER.INSTANCE }
  }

  val lru: LruCache<Any, Any> = LruCache(1024)

  fun saveBitmapToCache(key: String, bitmap: Bitmap) {
    try {
      Cache.instance.lru.put(key, bitmap)
    }
    catch (e: Exception) {
    }
  }

  fun retrieveBitmapFromCache(key: String): Bitmap? {
    try {
      return Cache.instance.lru.get(key) as Bitmap?
    }
    catch (e: Exception) {
    }

    return null
  }

}