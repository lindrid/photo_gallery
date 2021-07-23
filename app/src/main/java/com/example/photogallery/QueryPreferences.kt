package com.example.photogallery

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

private const val SEARCH_QUERY_PARAM_NAME = "searchQuery"
private const val PREF_LAST_RESULT_ID = "lastResultId"
private const val PREF_IS_POLLING = "isPolling"

object QueryPreferences {

  fun getQuery (context: Context): String {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    return prefs.getString(SEARCH_QUERY_PARAM_NAME, "")!!
  }

  fun storeQuery (context: Context, query: String) {
    PreferenceManager.getDefaultSharedPreferences(context).edit {
      putString(SEARCH_QUERY_PARAM_NAME, query)
    }
  }

  fun getLastResultId (context: Context): String {
    return PreferenceManager.getDefaultSharedPreferences(context)
      .getString(PREF_LAST_RESULT_ID, "")!!
  }

  fun setLastResultId (context: Context, lastResultId: String) {
    PreferenceManager.getDefaultSharedPreferences(context).edit {
      putString(PREF_LAST_RESULT_ID, lastResultId)
    }
  }

  fun isPolling (context: Context): Boolean {
    return PreferenceManager.getDefaultSharedPreferences(context)
      .getBoolean(PREF_IS_POLLING, false)
  }

  fun setPolling (context: Context, isOn: Boolean) {
    PreferenceManager.getDefaultSharedPreferences(context).edit {
      putBoolean(PREF_IS_POLLING, isOn)
    }
  }

}