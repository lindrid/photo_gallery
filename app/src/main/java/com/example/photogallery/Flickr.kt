package com.example.photogallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.photogallery.api.FlickrApi
import com.example.photogallery.api.FlickrResponse
import com.example.photogallery.api.PhotoResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "Flickr"

class Flickr (private val api: FlickrApi) {

  fun fetchPhotos() : LiveData<List<GalleryItem>> {
    return fetchPhotoMetadata(fetchPhotosRequest())
  }

  fun searchPhotos(query: String) : LiveData<List<GalleryItem>> {
    return fetchPhotoMetadata(searchPhotosRequest(query))
  }

  fun fetchPhotosRequest(): Call<FlickrResponse> {
    return api.fetchPhotos()
  }

  fun searchPhotosRequest(query: String): Call<FlickrResponse> {
    return api.searchPhotos(query)
  }

  @WorkerThread
  fun fetchPhoto (url: String): Bitmap? {
    val response: Response<ResponseBody> = api.fetchUrlBytes(url).execute()
    val bitmap = response.body()?.byteStream()?.use(BitmapFactory::decodeStream)
    Log.i (TAG, "Decoded bitmap = $bitmap from Response = $response")
    return bitmap
  }

  /* ****** private ****** */
  private fun fetchPhotoMetadata (request: Call<FlickrResponse>) :
      LiveData<List<GalleryItem>> {

    val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()

    request.enqueue (object : Callback<FlickrResponse> {
      override fun onFailure (call: Call<FlickrResponse>, t: Throwable) {
        Log.e (TAG, "Failed to fetch photos", t)
      }

      override fun onResponse (call: Call<FlickrResponse>, response: Response<FlickrResponse>) {
        Log.d (TAG, "Response received")

        val flickrResponse: FlickrResponse? = response.body()
        val photoResponse: PhotoResponse? = flickrResponse?.photos
        var galleryItems: List<GalleryItem> = photoResponse?.galleryItems?: mutableListOf()

        galleryItems = galleryItems.filterNot { it.url.isBlank() }
        responseLiveData.value = galleryItems
      }
    })

    return responseLiveData
  }
}