package com.example.photogallery

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.Image
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.example.photogallery.api.FlickrApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

private const val TAG = "PhotoGalleryFragment"
private const val POLL_WORK = "poll_work"

class PhotoGalleryFragment: Fragment() {
  private lateinit var photoRecyclerView: RecyclerView
  private lateinit var photoGalleryViewModel: PhotoGalleryViewModel
  private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoHolder>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    retainInstance = true
    setHasOptionsMenu(true) // регистрируем фрагмент для получения обратных вызовов меню
    photoGalleryViewModel = ViewModelProvider(this).get(PhotoGalleryViewModel::class.java)

    val responseHandler = Handler(Looper.getMainLooper())
    thumbnailDownloader = ThumbnailDownloader(responseHandler) { photoHolder, bitmap ->
      val drawable = BitmapDrawable(resources, bitmap)
      photoHolder.bindDrawable(drawable)
    }

    lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)
  }

  override fun onCreateView (inflater: LayoutInflater,
                             container: ViewGroup?,
                             savedInstanceState: Bundle?): View?
  {
    viewLifecycleOwner.lifecycle.addObserver(thumbnailDownloader.viewLifecycleObserver)

    val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)

    photoRecyclerView = view.findViewById(R.id.photo_recycler_view)
    photoRecyclerView.layoutManager = GridLayoutManager(context, 3)

    return view
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    photoGalleryViewModel.galleryItemLiveData.observe(viewLifecycleOwner,
      Observer { galleryItems ->
        photoRecyclerView.adapter = PhotoAdapter(galleryItems)
      }
    )
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    super.onCreateOptionsMenu(menu, inflater)
    inflater.inflate(R.menu.fragment_photo_gallery, menu)

    val searchItem : MenuItem = menu.findItem (R.id.menu_item_search)
    val searchView = searchItem.actionView as SearchView

    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

      override fun onQueryTextSubmit(query: String?): Boolean {
        Log.d (TAG, "QueryTextSubmit: $query")
        query?.let {
          photoGalleryViewModel.fetchPhotos(it)
        }
        return true
      }

      override fun onQueryTextChange(newText: String?): Boolean {
        Log.d (TAG, "QueryTextChange: $newText")
        return false // никак не обрабатываем действие ввода символов поискового запроса
      }

    })

    searchView.setOnSearchClickListener {
      searchView.setQuery (photoGalleryViewModel.searchTerm, false)
    }

    val toggleItem = menu.findItem(R.id.menu_item_toggle_polling)
    val isPolling = QueryPreferences.isPolling(requireContext())
    val toggleItemTitle = if (isPolling) {
      R.string.stop_polling
    }
    else {
      R.string.start_polling
    }
    toggleItem.setTitle(toggleItemTitle)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.menu_item_clear -> {
        photoGalleryViewModel.fetchPhotos("")
        true
      }
      R.id.menu_item_toggle_polling -> {
        if (QueryPreferences.isPolling(requireContext())) {
          stopPolling()
        }
        else {
          startPolling(15, TimeUnit.MINUTES)
        }
        activity?.invalidateOptionsMenu()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  companion object {
    fun newInstance() = PhotoGalleryFragment()
  }

  override fun onDestroy() {
    super.onDestroy()
    lifecycle.removeObserver(thumbnailDownloader.fragmentLifecycleObserver)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    viewLifecycleOwner.lifecycle.removeObserver(thumbnailDownloader.viewLifecycleObserver)
  }

  // ==== private ==== \\

  private fun stopPolling() {
    WorkManager.getInstance().cancelUniqueWork(POLL_WORK)
    QueryPreferences.setPolling(requireContext(), false)
  }

  private fun startPolling(repeatInterval: Long,  repeatIntervalTimeUnit: TimeUnit) {
    // будем требовать выполнения этих условий
    // перед работой нашего PollWorker
    // в условия входит: требуется безлимитная сеть
    val constraints = Constraints.Builder()
      .setRequiredNetworkType(NetworkType.UNMETERED)
      .build()

    val periodicRequest = PeriodicWorkRequest
      .Builder (PollWorker::class.java, repeatInterval, repeatIntervalTimeUnit)
      .setConstraints(constraints)
      .build()

    WorkManager.getInstance().enqueueUniquePeriodicWork (
      POLL_WORK,
      // KEEP означает - оставляем старый запрос, если запрос с именем POLL_WORK уже существует
      ExistingPeriodicWorkPolicy.KEEP,
      periodicRequest
    )

    QueryPreferences.setPolling(requireContext(), true)
  }

  private class PhotoHolder(imageView: ImageView) : RecyclerView.ViewHolder(imageView) {
    val bindDrawable: (Drawable) -> Unit = imageView::setImageDrawable
  }

  private inner class PhotoAdapter(private val galleryItems: List<GalleryItem>):
    RecyclerView.Adapter<PhotoHolder>()
  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder
    {
      val view = layoutInflater.inflate(R.layout.list_item_gallery, parent, false)
          as ImageView
      return PhotoHolder(view)
    }

    override fun getItemCount(): Int = galleryItems.size

    override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
      val galleryItem = galleryItems[position]
      thumbnailDownloader.queueThumbnail(holder, galleryItem.url)
    }
  }

}