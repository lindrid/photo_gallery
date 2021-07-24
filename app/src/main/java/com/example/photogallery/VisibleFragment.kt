package com.example.photogallery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import androidx.fragment.app.Fragment

abstract class VisibleFragment: Fragment() {

  private val onShowNotification = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      Toast.makeText (requireContext(),
        "Got a broadcast: ${intent?.action}",
        Toast.LENGTH_LONG
      ).show()
    }
  }

  override fun onStart() {
    super.onStart()
    requireActivity().registerReceiver(
      onShowNotification,
      // этот фильтр идентичен тому, что объявлен в Манифесте для автономного приемника <receiver>
      // только здесь этот фильтр создается в коде для нашего динамического приемника
      IntentFilter(PollWorker.ACTION_SHOW_NOTIFICATION),
      PollWorker.PERMISSION_PRIVATE,
      null
    )
  }

  override fun onStop() {
    super.onStop()
    requireActivity().unregisterReceiver(onShowNotification)
  }

}