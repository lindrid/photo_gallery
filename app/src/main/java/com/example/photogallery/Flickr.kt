package com.example.photogallery

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.photogallery.api.FlickrApi
import com.example.photogallery.api.FlickrResponse
import com.example.photogallery.api.PhotoResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

private const val TAG = "Flickr"

class Flickr {
  private val api: FlickrApi

  init {
    val retrofit: Retrofit = Retrofit.Builder()
      .baseUrl("https://www.flickr.com/")
      // Factory is for convert okhttp3.ResponseBody to String (See FlickrApi)
      .addConverterFactory(GsonConverterFactory.create())
      .build()

    api = retrofit.create(FlickrApi::class.java)
  }

  fun fetchPhotos(): LiveData<List<GalleryItem>> {
    val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()
    val flickrRequest: Call<FlickrResponse> = api.fetchPhotos()

    flickrRequest.enqueue(object : Callback<FlickrResponse> {
      override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
        Log.e (TAG, "Failed to fetch photos", t)
      }

      override fun onResponse(call: Call<FlickrResponse>, response: Response<FlickrResponse>) {
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