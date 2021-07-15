package com.example.photogallery

import android.annotation.SuppressLint
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.example.photogallery.api.ApiSingleton
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "ThumbnailDownloader"
private const val MESSAGE_DOWNLOAD = 0

class ThumbnailDownloader<in T> : HandlerThread(TAG), LifecycleObserver {
  private var hasQuit = false
  private lateinit var requestHandler: Handler
  private val requestMap = ConcurrentHashMap<T, String>()
  private val flickr = Flickr(ApiSingleton.get().flickr)

  override fun quit(): Boolean {
    hasQuit = true
    return super.quit()
  }

  fun queueThumbnail (target: T, url: String) {
    Log.i (TAG, "Got a url: $url")
    requestMap[target] = url
    requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget()
  }

  // Сообщает Lint, что вы приводите msg.obj к типу T (у нас это PhotoHolder)
  // без предварительной проверки того, относится ли msg.obj к этому типу на самом деле
  @Suppress("UNCHECKED_CAST")
  // внутренние классы содержат ссылку на свой внешний класс, что в свою очередь
  // может привести к утечке внешнего класса, если время жизни внутреннего класса больше,
  // чем предполагаемое время жизни внешнего класса. Проблемы тут получаются только
  // в том случае, если обработчик прикреплен к объекту Looper основного потока.
  // Предупреждение HandlerLeak убирается следующей аннотацией, так как создаваемый
  // обработчик прикреплен к looper фонового потока.
  @SuppressLint("HandlerLeak")
  override fun onLooperPrepared() {
    requestHandler = object : Handler() {
      override fun handleMessage(msg: Message) {
        if (msg.what == MESSAGE_DOWNLOAD) {
          val target = msg.obj as T
          Log.i (TAG, "Got a request for URL: ${requestMap[target]}")
          handleRequest(target)
        }
      }
    }
  }

  private fun handleRequest(target: T) {
    val url = requestMap[target] ?: return
    val bitmap = flickr.fetchPhoto(url) ?: return
  }

  @OnLifecycleEvent (Lifecycle.Event.ON_CREATE)
  fun setup() {
    Log.i (TAG,"Starting background thread")
  }

  @OnLifecycleEvent (Lifecycle.Event.ON_DESTROY)
  fun tearDown() {
    Log.i (TAG, "Destroying background thread")
  }
}