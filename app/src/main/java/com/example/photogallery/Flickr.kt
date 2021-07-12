package com.example.photogallery

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.photogallery.api.FlickrApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

private const val TAG = "Flickr"

class Flickr {
  private val api: FlickrApi

  init {
    val retrofit: Retrofit = Retrofit.Builder()
      .baseUrl("https://www.flickr.com/")
      // Factory is for convert okhttp3.ResponseBody to String (See FlickrApi)
      .addConverterFactory(ScalarsConverterFactory.create())
      .build()

    api = retrofit.create(FlickrApi::class.java)
  }

  fun fetchContents(): LiveData<String> {
    val responseLiveData: MutableLiveData<String> = MutableLiveData()
    val flickrRequest: Call<String> = api.fetchContents()

    flickrRequest.enqueue(object : Callback<String>
    {
      override fun onFailure(call: Call<String>, t: Throwable) {
        Log.e (TAG, "Failed to fetch photos", t)
      }

      override fun onResponse(call: Call<String>, response: Response<String>) {
        val s: String = response.body().toString()
        Log.d (TAG, "Response received: ${s.length}")
        responseLiveData.value = s
      }
    })
  }
}