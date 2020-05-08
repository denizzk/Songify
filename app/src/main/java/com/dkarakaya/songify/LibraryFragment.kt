package com.dkarakaya.songify

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dkarakaya.songify.adapter.SongAdapter
import com.dkarakaya.songify.mediaplayer.CustomMediaController
import com.dkarakaya.songify.mediaplayer.MusicService
import com.dkarakaya.songify.model.SongInfo
import com.dkarakaya.songify.util.REQUEST_EXTERNAL_STORAGE
import com.dkarakaya.songify.util.verifyStoragePermissions
import kotlinx.android.synthetic.main.fragment_library.*
import timber.log.Timber

class LibraryFragment : Fragment(R.layout.fragment_library) {
    private lateinit var controller: CustomMediaController
    private lateinit var songAdapter: SongAdapter
    private var songList: MutableList<SongInfo> = emptyList<SongInfo>().toMutableList()

    private lateinit var playIntent: Intent

    private var paused = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        render()
        requireActivity().verifyStoragePermissions()
        loadSongs()
    }

    private fun render() {
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        setController()
        songAdapter = SongAdapter(requireContext(), songList)
        songAdapter.setOnItemClickListener(object : SongAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, obj: SongInfo?, position: Int) {
                try {
                    controller.songPicked(position)
                } catch (e: Exception) {
                    Timber.e(e.stackTrace.toString())
                }
            }
        })
        recycler_view.adapter = songAdapter
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    loadSongs()
                } else {
                    requireActivity().verifyStoragePermissions()
                }
                return
            }
        }
    }

    override fun onStart() {
        super.onStart()
        initializePlayIntent()
        requireContext().bindService(playIntent, controller.musicConnection(songList), Context.BIND_AUTO_CREATE)
        requireContext().startService(playIntent)
    }

    override fun onResume() {
        super.onResume()
        if (paused) {
            paused = false
        }
    }

    override fun onPause() {
        super.onPause()
        paused = true
    }

    override fun onStop() {
        super.onStop()
        controller.hideController()
        requireContext().stopService(playIntent)
    }

    private fun initializePlayIntent() {
        if (!::playIntent.isInitialized) {
            playIntent = Intent(requireContext(), MusicService::class.java)
        }
    }

    // set the controller up
    private fun setController() {
        controller = CustomMediaController.initMediaController(requireContext())
        controller.setAnchorView(media_controller)
    }

    private fun loadSongs() {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val audioType = "${MediaStore.Audio.Media.MIME_TYPE} == 'audio/mp4' OR ${MediaStore.Audio.Media.MIME_TYPE} == 'audio/mpeg'"
        val selection = "${MediaStore.Audio.Media.IS_MUSIC}!=0 AND $audioType AND ${MediaStore.Audio.Media.DATA} LIKE '%music%'"
        val sortOrder = "${MediaStore.Audio.Media.DATE_MODIFIED} ASC"
        val cursor = requireContext().contentResolver.query(uri, null, selection, null, sortOrder)
        cursor?.use {
            while (cursor.moveToNext()) {
                var title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                var artist: String? = null
                val url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                val duration = formatDuration(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)))

                val array = title.split("-").toTypedArray()
                if (array.size == 2) {
                    title = formatTitle(array)[1]
                    artist = formatTitle(array)[0]
                }
                val songInfo = SongInfo(title, artist, url, duration)
                songList.add(songInfo)
            }
        }
        songAdapter = SongAdapter(requireContext(), songList)
    }

    private fun formatDuration(duration: String): String {
        val d = duration.toInt() / 1000
        val min = d / 60
        val sec = d % 60
        return String.format("%02d:%02d", min, sec)
    }

    private fun formatTitle(array: Array<String>): Array<String> {
        array[1].trim { it <= ' ' }
        array[0].trim { it <= ' ' }
        return array
    }
}
