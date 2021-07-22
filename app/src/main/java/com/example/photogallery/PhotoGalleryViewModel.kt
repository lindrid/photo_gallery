package com.example.photogallery

import android.app.Application
import androidx.lifecycle.*
import com.example.photogallery.api.ApiSingleton

class PhotoGalleryViewModel (private val app: Application): AndroidViewModel(app) {
  private val flickr: Flickr = Flickr(ApiSingleton.get().flickr)
  val galleryItemLiveData: LiveData<List<GalleryItem>>
  private val mutableSearchTerm = MutableLiveData<String>()

  val searchTerm: String
    get() = mutableSearchTerm.value ?: ""

  init {
    mutableSearchTerm.value = QueryPreferences.getQuery(app)
    // если введен новый searchTerm, то произойдет запрос и будут изменены скачанные картинки
    galleryItemLiveData = Transformations.switchMap(mutableSearchTerm) { searchTerm ->
      if (searchTerm.isBlank()) {
        flickr.fetchPhotos()
      }
      else {
        flickr.searchPhotos(searchTerm)
      }
    }
  }

  fun fetchPhotos (query : String = "") {
    QueryPreferences.storeQuery(app, query)
    mutableSearchTerm.value = query
  }
}