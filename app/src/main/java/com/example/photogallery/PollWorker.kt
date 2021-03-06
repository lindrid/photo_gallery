package com.example.photogallery

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.photogallery.api.ApiSingleton

private const val TAG = "PollWorker"

// класс опроса (poll) сервера на предмет новых фотографий
class PollWorker (private val context: Context, workerParams: WorkerParameters):
  Worker (context, workerParams)
{

  // работает из фонового потока, поэтому можно выполнять
  // любые долгосрочные задачи
  override fun doWork(): Result {
    val query = QueryPreferences.getQuery(context)
    val lastResultId = QueryPreferences.getLastResultId(context)
    val flickr = Flickr(ApiSingleton.get().flickr)

    val items: List<GalleryItem> = if (query.isEmpty()) {
      flickr.fetchPhotosRequest()
        .execute()
        .body()
        ?.photos
        ?.galleryItems
    }
    else {
      flickr.searchPhotosRequest(query)
        .execute()
        .body()
        ?.photos
        ?.galleryItems
    }
    ?: emptyList()

    if (items.isEmpty()) {
      return Result.success()
    }

    val resultId = items.first().id
    if (resultId == lastResultId) {
      Log.i(TAG, "Got an old result: $resultId")
    }
    else {
      Log.i(TAG, "Got a new result: $resultId")
      QueryPreferences.setLastResultId(context, resultId)
      notifyUserAboutNewPhotos()
      context.sendBroadcast(Intent(ACTION_SHOW_NOTIFICATION), PERMISSION_PRIVATE)
    }

    return Result.success()
  }

  companion object {
    const val ACTION_SHOW_NOTIFICATION = "com.example.photogallery.SHOW_NOTIFICATION"
    const val PERMISSION_PRIVATE = "com.example.photogallery.PRIVATE"
    const val REQUEST_CODE = "REQUEST_CODE"
    const val NOTIFICATION = "NOTIFICATION"
  }


  private fun notifyUserAboutNewPhotos() {
    // данные интенты нужны, чтобы при нажатии по уведомлению о новых фотографиях,
    // которое получит пользователь, произойдет запуск PhotoGalleryActivity
    // и это возможно только при установке .setContentIntent(pendintIntent) для Notification.Builder
    val intent = PhotoGalleryActivity.newIntent(context)
    val pendintIntent = PendingIntent.getActivity(context, 0, intent, 0)

    val resources = context.resources
  /*  NotificationCompat.Builder принимает ID канала и использует его для
      установки параметра канала уведомления, если пользователь запустил приложение
      на Oreo или выше. Если у пользователя запущена более ранняя версия Android, тогда
      Builer игнорирует канал (и, соответственно, пользователь не сможет применить стилизацию
      и получить контроль над уведомлением) */
    val notification = NotificationCompat
      .Builder(context, NOTIFICATION_CHANNEL_ID)
      .setTicker(resources.getString(R.string.new_pictures_title))
      .setSmallIcon(android.R.drawable.ic_menu_report_image)
      .setContentTitle(resources.getString(R.string.new_pictures_title))
      .setContentText(resources.getString(R.string.new_pictures_text))
      .setContentIntent(pendintIntent)
      .setAutoCancel(true)
      .build()

    val intentForBroadcast = Intent(ACTION_SHOW_NOTIFICATION).apply {
      putExtra(REQUEST_CODE, 0)
      putExtra(NOTIFICATION, notification)
    }

    // шлём упорядоченный широковещательный сигнал, это значит
    // что приемники получат и обработают его в установленном порядке:
    // сначала динамический, потом автономный, потому что у автономного установлен
    // самый низкий приоритет (-999)
    // это нужно для того, чтобы не показывать уведомление пользователю, когда пользователь
    // находится в запущенной программе, поэтому динамический приемник будет отменять дальнейший сигнал
    // и он не будет доходить до автономного приемника NotificationReceiver, который и показывает
    // уведомление в меню телефона
    context.sendOrderedBroadcast(intentForBroadcast, PERMISSION_PRIVATE)
  }

}