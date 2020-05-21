package com.dkarakaya.songify.search

import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dkarakaya.songify.R
import com.dkarakaya.songify.adapter.YoutubeVideoAdapter
import com.dkarakaya.songify.model.YoutubeItem
import com.dkarakaya.songify.util.OnItemClickListener
import com.dkarakaya.songify.util.REQUEST_EXTERNAL_STORAGE
import com.dkarakaya.songify.util.toggleKeyboard
import com.dkarakaya.songify.util.verifyStoragePermissions
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_search.*
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class SearchFragment : Fragment(R.layout.fragment_search) {
    private val viewModel = SearchViewModel()

    private val disposable = CompositeDisposable()

    private lateinit var adapter: YoutubeVideoAdapter
    private lateinit var data: ArrayList<YoutubeItem>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        render()
        requireActivity().verifyStoragePermissions()
        registerSubscriptions()
    }

    private fun registerSubscriptions() {
        search_edit.textChanges()
                .map { it.toString().replace(" ", "+") }
                .debounce(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                        onNext = { query ->
                            videoUrl = URL + query
                        },
                        onError = Timber::e
                ).addTo(disposable)

        search_button.clicks()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {
                            RequestYoutubeAPI().execute()
                        },
                        onError = Timber::e
                ).addTo(disposable)

        viewModel.download()
                .subscribeBy(
                        onNext = { downloadFromUrl(it.first, it.second) },
                        onError = Timber::e
                ).addTo(disposable)
    }

    private fun render() {
        search_text.visibility = View.VISIBLE
        main_layout.visibility = View.GONE
        recycler_view.visibility = View.GONE
        search_edit.toggleKeyboard()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                    getYoutubeDownloadUrl(youtubeLink)
                } else {
                    requireActivity().verifyStoragePermissions()
                }
                return
            }
        }
    }

    private fun downloadFromUrl(url: String, title: String?) {
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)
        val filename = "$title.m4a"
        request.setTitle(title)
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/music/$filename")
        val downloadManager = requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }

    //create an async task to get the data from youtube
    private inner class RequestYoutubeAPI : AsyncTask<Void?, String?, String?>() {
        override fun doInBackground(vararg params: Void?): String? {
            val httpClient = HttpClientBuilder.create().build()
            val httpGet = HttpGet(videoUrl)
            Timber.e(videoUrl)
            try {
                val response = httpClient.execute(httpGet)
                val httpEntity = response.entity
                return EntityUtils.toString(httpEntity)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(response: String?) {
            super.onPostExecute(response)
            if (response != null) {
                try {
                    val jsonObject = JSONObject(response)
                    Timber.e(jsonObject.toString())
                    data = parseVideoListFromResponse(jsonObject)
                    initList(data)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }

        fun parseVideoListFromResponse(jsonObject: JSONObject): ArrayList<YoutubeItem> {
            val videoList = ArrayList<YoutubeItem>()
            if (jsonObject.has("items")) {
                try {
                    val jsonArray = jsonObject.getJSONArray("items")
                    for (i in 0 until jsonArray.length()) {
                        val json = jsonArray.getJSONObject(i)
                        if (json.has("id")) {
                            val jsonID = json.getJSONObject("id")
                            lateinit var videoId: String
                            if (jsonID.has("videoId")) {
                                videoId = jsonID.getString("videoId")
                            }
                            if (jsonID.has("kind")) {
                                if (jsonID.getString("kind") == "youtube#video") {
                                    val jsonSnippet = json.getJSONObject("snippet")
                                    val title = jsonSnippet.getString("title")
                                    val channelTitle = jsonSnippet.getString("channelTitle")
                                    val publishedAt = jsonSnippet.getString("publishedAt")
                                    val thumbnail = jsonSnippet.getJSONObject("thumbnails")
                                            .getJSONObject("medium").getString("url")
                                    val youtubeObject = YoutubeItem(title, channelTitle, publishedAt, thumbnail, videoId)
                                    youtubeObject.title = title
                                    youtubeObject.channelTitle = channelTitle
                                    youtubeObject.publishedAt = publishedAt
                                    youtubeObject.thumbnail = thumbnail
                                    youtubeObject.videoId = videoId
                                    videoList.add(youtubeObject)
                                }
                            }
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            return videoList
        }
    }

    private fun initList(data: ArrayList<YoutubeItem>) {
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        search_text.visibility = View.GONE
        main_layout.visibility = View.GONE
        recycler_view.visibility = View.VISIBLE

        adapter = YoutubeVideoAdapter(data, object : OnItemClickListener {
            override fun onItemClick(item: YoutubeItem?) {
                videoId = item?.videoId!!
//                getYoutubeDownloadUrl(youtubeLink, item.title!!)
                viewModel.videoId(videoId)
            }
        })
        recycler_view.adapter = adapter
    }


    companion object {
        private const val GOOGLE_YOUTUBE_API_KEY = "AIzaSyCtincZH47KLLmeDE5t5gEp9zzA-VgMKLE"
        private const val URL = "https://www.googleapis" +
                ".com/youtube/v3/search?part=snippet&maxResults=25&key=" + GOOGLE_YOUTUBE_API_KEY + "&q="

//        private const val URL = "https://www.googleapis" +
//                ".com/youtube/v3/search?part=snippet&q=YouTube+Data+API&type=video&key=" + GOOGLE_YOUTUBE_API_KEY

        private lateinit var videoUrl: String
        private lateinit var videoId: String
    }

}
