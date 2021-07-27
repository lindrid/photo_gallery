package com.example.photogallery

import android.net.Uri
import com.google.gson.annotations.SerializedName

data class GalleryItem (var title: String = "",
                        var id: String = "",
                        @SerializedName("url_s") var url: String = "",
                        @SerializedName("owner") var owner: String = "")
{
  // получаем Uri для каждой фотки : https://www.flickr.com/photos/owner/photo_id
  val photoPageUri: Uri
    get() {
      return Uri.parse("https://www.flickr.com/photos/")
        .buildUpon()
        .appendPath(owner)
        .appendPath(id)
        .build()
    }
}