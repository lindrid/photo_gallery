package com.example.photogallery.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiSingleton private constructor(val flickr: FlickrApi) {

  companion object {
    private var instance: ApiSingleton? = null

    fun initialize() {
      val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://www.flickr.com/")
        // Factory is for convert okhttp3.ResponseBody to String (See FlickrApi)
        .addConverterFactory(GsonConverterFactory.create())
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