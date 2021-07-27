package com.example.photogallery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

private const val TAG = "PhotoPageActivity"

class PhotoPageActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Log.i (TAG, "asdasdasdasdas")
    setContentView(R.layout.activity_photo_page)

    val fm = supportFragmentManager
    val currentFragment = fm.findFragmentById(R.id.fragment_container)
    Log.i (TAG, "intent data: ${intent.data!!}")
    Log.i (TAG, "fragment: ${currentFragment.toString()}")

    if (currentFragment == null) {
      Log.i (TAG, "intent data: ${intent.data!!}")
      val fragment = PhotoPageFragment.newInstance(intent.data!!)
      fm.beginTransaction()
        .add(R.id.fragment_container, fragment)
        .commit()
    }
  }

  companion object {
    fun newIntent (context: Context, photoPageUri: Uri) : Intent {
      return Intent (context, PhotoPageActivity::class.java).apply {
        data = photoPageUri
      }
    }
  }

}