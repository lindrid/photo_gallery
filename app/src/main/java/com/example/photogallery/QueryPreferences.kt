package com.example.photogallery

import android.content.Context
import android.preference.PreferenceManager

private const val SEARCH_QUERY_PARAM_NAME = "searchQuery"

object QueryPreferences {

  fun getQuery (context: Context): String {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    return prefs.getString(SEARCH_QUERY_PARAM_NAME, "")!!
  }

  fun storeQuery (context: Context, query: String) {
    PreferenceManager.getDefaultSharedPreferences(context)
      .edit()
      .putString(SEARCH_QUERY_PARAM_NAME, query)
    .apply()
  }

}