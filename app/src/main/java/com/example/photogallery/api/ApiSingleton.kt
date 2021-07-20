package com.example.photogallery.api

import com.example.photogallery.PhotoInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiSingleton private constructor(val flickr: FlickrApi) {

  companion object {
    private var instance: ApiSingleton? = null

    fun initialize() {
      val client = OkHttpClient.Builder()
        .addInterceptor(PhotoInterceptor())
      .build()

      val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://www.flickr.com/")
        // Factory is for convert okhttp3.ResponseBody to String (See FlickrApi)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
      .build()

      val flickr = retrofit.create(FlickrApi::class.java)

      if (instance == null) {
        instance = ApiSingleton(flickr)
      }
    }

    fun get(): ApiSingleton {
      return instance ?:
      throw IllegalStateException("ApiSingleton must be initialized")
    }
  }
}