package com.example.photogallery

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.photogallery.api.ApiSingleton

private const val TAG = "PollWorker"

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
    }

    return Result.success()
  }

}