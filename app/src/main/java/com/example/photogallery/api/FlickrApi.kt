package com.example.photogallery.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

const val API_KEY = "da5857f9d60ccea8f67944d135e7e7bd"

interface FlickrApi {
  @GET("/")
  fun fetchContents(): Call<String>

  @GET("services/rest/?method=flickr.interestingness.getList")
  fun fetchPhotos(): Call<FlickrResponse>

  @GET
  fun fetchUrlBytes (@Url url: String): Call<ResponseBody>

  @GET("services/rest?method=flickr.photos.search")
  fun searchPhotos (@Query("text") text: String): Call<FlickrResponse>
}