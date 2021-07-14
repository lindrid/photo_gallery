package com.example.photogallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.photogallery.api.ApiSingleton

class PhotoGalleryViewModel: ViewModel() {
  private val flickr: Flickr = Flickr(ApiSingleton.get().flickr)
  val galleryItemLiveData: LiveData<List<GalleryItem>> = flickr.fetchPhotos()
}