package com.dkarakaya.songify.library

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dkarakaya.songify.R
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
    private val songListLoader = SongListLoader()

    private lateinit var playIntent: Intent

    private var paused = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().verifyStoragePermissions()
        render()
    }

    private fun render() {
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        setController()
        songAdapter = songListLoader(requireContext(), songList)
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
                    songAdapter = songListLoader(requireContext(), songList)
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
}
