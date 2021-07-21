package com.example.photogallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.photogallery.api.ApiSingleton

class PhotoGalleryViewModel: ViewModel() {
  private val flickr: Flickr = Flickr(ApiSingleton.get().flickr)
  val galleryItemLiveData: LiveData<List<GalleryItem>>
  private val mutableSearchTerm = MutableLiveData<String>()

  init {
    mutableSearchTerm.value = "batman"
    // если введен новый searchTerm, то произойдет запрос и будут изменены скачанные картинки
    galleryItemLiveData = Transformations.switchMap(mutableSearchTerm) { searchTerm ->
      flickr.searchPhotos(searchTerm)
    }
  }

  fun fetchPhotos (searchTerm : String = "") {
    mutableSearchTerm.value = searchTerm
  }
}