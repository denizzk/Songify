package com.dkarakaya.songify

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

class SearchFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: VideoPostAdapter
    private lateinit var data: ArrayList<YoutubeDataModel>
    private lateinit var mainLayout: LinearLayout
    private lateinit var mainProgressBar: ProgressBar
    private lateinit var textViewSearch: TextView
    private lateinit var editTextSearch: EditText
    private lateinit var btnSearch: ImageButton
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        checkUserPermission()
        render(view)
        btnSearch.setOnClickListener {
            val query = editTextSearch.text.toString().replace(" ", "+")
            videoUrl = URL + query
            mainLayout.visibility = View.VISIBLE

            RequestYoutubeAPI().execute()
        }
        return view
    }

    private fun render(view: View) {
        textViewSearch = view.findViewById(R.id.tvSearch)
        textViewSearch.visibility = View.VISIBLE
        editTextSearch = view.findViewById(R.id.edtSearch)
        mainLayout = view.findViewById(R.id.main_layout)
        mainProgressBar = view.findViewById(R.id.prgrBar)
        mainLayout.visibility = View.GONE
        recyclerView = view.findViewById(R.id.mList_videos)
        recyclerView.visibility = View.GONE
        btnSearch = view.findViewById(R.id.btnSearch)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, Objects.requireNonNull(permissions), grantResults)
//        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        // have the permission now.
//            getYoutubeDownloadUrl(youtubeLink);
//        }
    }

    private fun checkUserPermission() {
        if (ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 123)
            return
        }
        //        getYoutubeDownloadUrl(youtubeLink);
    }

    private fun initList(data: ArrayList<YoutubeDataModel>) {
        recyclerView.layoutManager = LinearLayoutManager(activity)
        textViewSearch.visibility = View.GONE
        mainLayout.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE

        adapter = VideoPostAdapter(data, object : OnItemClickListener {
            override fun onItemClick(item: YoutubeDataModel?) {
                videoId = item?.videoId
                youtubeLink = YOUTUBE_ + videoId
                getYoutubeDownloadUrl(requireNotNull(youtubeLink), item?.title)
            }
        })
        recyclerView.adapter = adapter
    }

    private fun getYoutubeDownloadUrl(youtubeLink: String, videoTitle: String?) {
        Extractor(requireNotNull(videoTitle)).extract(youtubeLink, true, false)
    }

    inner class Extractor(private val videoTitle: String) : YouTubeExtractor(requireActivity()) {
        public override fun onExtractionComplete(ytFiles: SparseArray<YtFile>, vMeta: VideoMeta) {
            // Iterate over itags
            var i = 0
            var itag: Int
            while (i < ytFiles.size()) {
                itag = ytFiles.keyAt(i)
                // ytFile represents one file with its url and meta data
                val ytFile = ytFiles[itag]
                println("ITAG: $itag")
                // Just add videos in a decent format => height -1 = audio
                if (ytFile.format.height == -1) {
                    val filename = setFileName(videoTitle, ytFile)
                    downloadFromUrl(ytFile.url, videoTitle, filename)
                }
                i++
            }
        }

        private fun setFileName(videoTitle: String, ytFile: YtFile): String {
            var filename = if (videoTitle.length > 55) {
                videoTitle.substring(0, 55) + "." + ytFile.format
                        .ext
            } else {
                videoTitle + "." + ytFile.format.ext
            }
            filename = filename.replace("[\\\\><\"|*?%:#/]".toRegex(), "")
            return filename
        }

        private fun downloadFromUrl(youtubeDlUrl: String, downloadTitle: String?, fileName: String) {
            val uri = Uri.parse(youtubeDlUrl)
            val request = DownloadManager.Request(uri)
            request.setTitle(downloadTitle)
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS + "/music",
                    fileName)
            val downloadManager = requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
        }
    }

    //create an asynctask to get all the data from youtube
    private inner class RequestYoutubeAPI : AsyncTask<Void?, String?, String?>() {
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: Void?): String? {
            val httpClient: HttpClient = DefaultHttpClient()
            val httpGet = HttpGet(videoUrl)
            Log.e("URL", videoUrl)
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
                    Log.e("response", jsonObject.toString())
                    data = parseVideoListFromResponse(jsonObject)
                    initList(data)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }

        fun parseVideoListFromResponse(jsonObject: JSONObject): ArrayList<YoutubeDataModel> {
            val mList = ArrayList<YoutubeDataModel>()
            if (jsonObject.has("items")) {
                try {
                    val jsonArray = jsonObject.getJSONArray("items")
                    for (i in 0 until jsonArray.length()) {
                        val json = jsonArray.getJSONObject(i)
                        if (json.has("id")) {
                            val jsonID = json.getJSONObject("id")
                            var video_id: String? = ""
                            if (jsonID.has("videoId")) {
                                video_id = jsonID.getString("videoId")
                            }
                            if (jsonID.has("kind")) {
                                if (jsonID.getString("kind") == "youtube#video") {
                                    val youtubeObject = YoutubeDataModel()
                                    val jsonSnippet = json.getJSONObject("snippet")
                                    val title = jsonSnippet.getString("title")
                                    val channelTitle = jsonSnippet.getString("channelTitle")
                                    val publishedAt = jsonSnippet.getString("publishedAt")
                                    val thumbnail = jsonSnippet.getJSONObject("thumbnails")
                                            .getJSONObject("medium").getString("url")
                                    youtubeObject.title = title
                                    youtubeObject.channelTitle = channelTitle
                                    youtubeObject.publishedAt = publishedAt
                                    youtubeObject.thumbnail = thumbnail
                                    youtubeObject.videoId = video_id
                                    mList.add(youtubeObject)
                                }
                            }
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            return mList
        }
    }

    companion object {
        private const val GOOGLE_YOUTUBE_API_KEY = "AIzaSyAdDix7i7a3an-gyXiquTV_14cIsr8-DZg"
        private const val URL = "https://www.googleapis" +
                ".com/youtube/v3/search?part=snippet&maxResults=25&key=" + GOOGLE_YOUTUBE_API_KEY + "&q="
        private const val YOUTUBE_ = "https://www.youtube.com/watch?v="

        private lateinit var videoUrl: String
        private var youtubeLink: String? = null
        private var videoId: String? = null
    }
}
