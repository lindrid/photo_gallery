package com.example.photogallery

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment

private const val TAG = "VisibleFragment"

abstract class VisibleFragment: Fragment() {

  // данный динамичекий приемник выполнится РАНЬШЕ автономного приемника из Манифеста,
  // потому что, приоритет у автономного приемника установлен самый низкий (-999)
  // это нужно для того, чтобы не показывать уведомление пользователю, когда пользователь
  // находится и запущенной программе, поэтому динамический приемник будет отменять
  // дальнейший сигнал и он не будет доходить до автономного приемника, который и показывает
  // уведомление в меню телефона
  private val onShowNotification = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      Log.i(TAG, "canceling notification")
      resultCode = Activity.RESULT_CANCELED
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