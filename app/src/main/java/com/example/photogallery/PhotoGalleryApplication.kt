package com.example.photogallery

import android.app.Application
import com.example.photogallery.api.ApiSingleton

class PhotoGalleryApplication: Application() {
  override fun onCreate() {
    super.onCreate()
    ApiSingleton.initialize()
  }
}