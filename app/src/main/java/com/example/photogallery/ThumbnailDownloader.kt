package com.example.photogallery

import android.annotation.SuppressLint
import android.graphics.Bitmap
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

// понятно про многопоточность с помощью HandlerThread, Handler и Looper тут:
// https://ru.coursera.org/lecture/android-multithreading-and-network/hamer-j18yz

// в пред-предыдущем коммите (который, 577) не хватало start() в функции setup, а также
// Handler(looper) , а не просто Hanler(). И все заработало бы.

// responseHandler это Handler из главного потока, связанный с Looper из главного потока
// второй параметр - слушатель для передачи ответов (загруженных изображений)
// запрашивающей стороне (главному потоку) asasdasd
class ThumbnailDownloader<in T> (
    private val responseHandler: Handler,
    private val onThumbnailDownloaded: (T, Bitmap) -> Unit
  ) : HandlerThread(TAG), LifecycleObserver
{
  private var hasQuit = false
  private lateinit var requestHandler: Handler
  private val requestMap = ConcurrentHashMap<T, String>()
  private val flickr = Flickr(ApiSingleton.get().flickr)

  val fragmentLifecycleObserver: LifecycleObserver = object : LifecycleObserver {
    @OnLifecycleEvent (Lifecycle.Event.ON_CREATE)
    fun setup() {
      Log.i (TAG,"Starting background thread")
      start() // после этого в функции onLooperPrepared() инициализируется requestHandler
      looper
    }

    @OnLifecycleEvent (Lifecycle.Event.ON_DESTROY)
    fun tearDown() {
      Log.i (TAG, "Destroying background thread")
      quit()
    }
  }

  val viewLifecycleObserver: LifecycleObserver = object : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun clearQueue() {
      Log.i (TAG, "Clearing all requests from queue")
      requestHandler.removeMessages(MESSAGE_DOWNLOAD)
      requestMap.clear()
    }
  }

  override fun quit(): Boolean {
    hasQuit = true
    return super.quit()
  }

  fun queueThumbnail (target: T, url: String) {
    Log.i (TAG, "Got a url: $url")
    requestMap[target] = url
    // requestHandler уже был инициализирован после вызова start() в setup(), которая
    // в свою очередь была вызвана при создании фрагмента
    // а здесь мы просто загоняем сообщение - скачай мне картинку - в общий пул сообщений
    // по которому бегает looper
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
    requestHandler = object : Handler(looper) {
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

    responseHandler.post(Runnable {
      if (requestMap[target] != url || hasQuit) {
        return@Runnable
      }
      requestMap.remove(target)
      onThumbnailDownloaded(target, bitmap)
    })
  }
}