package com.dkarakaya.songify

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.MediaController.MediaPlayerControl
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dkarakaya.songify.MusicService.MusicBinder

class LibraryFragment : Fragment(), MediaPlayerControl {
    private lateinit var recyclerView: RecyclerView
    private lateinit var mediaController: FrameLayout

    private lateinit var musicController: MusicController
    private lateinit var musicService: MusicService
    private lateinit var songAdapter: SongAdapter
    private var songList: MutableList<SongInfo> = emptyList<SongInfo>().toMutableList()

    private var musicBound = false
    private var paused = false
    private var playbackPaused = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)
        render(view)
        checkUserPermission()
        return view
    }

    private fun render(view: View) {
        val linearLayoutManager = LinearLayoutManager(requireActivity())
        recyclerView = view.findViewById(R.id.songList)
        mediaController = view.findViewById(R.id.mediaController)

        setController()

        songAdapter = SongAdapter(requireActivity(), songList)
        songAdapter.setOnItemClickListener(object : SongAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, obj: SongInfo?, position: Int) {
                try {
                    songPicked(position)
                } catch (e: Exception) {
                    Log.e(requireActivity().packageName, e.stackTrace.toString())
                }
            }
        })

        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = songAdapter
    }

    override fun onPause() {
        super.onPause()
        paused = true
    }

    override fun onResume() {
        super.onResume()
        if (paused) {
            setController()
            paused = false
        }
    }

    override fun onStop() {
        musicController.hide()
        super.onStop()
    }

    //connect to the service
    private val musicConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as MusicBinder
            //get service
            musicService = binder.service
            //pass list
            musicService.setList(songList)
            musicBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            musicBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        val playIntent = Intent(requireContext(), MusicService::class.java)
        requireContext().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
        requireContext().startService(playIntent)
    }

    private fun songPicked(songPos: Int) {
        musicService.setSong(songPos)
        musicService.playSong()
        if (playbackPaused) {
            setController()
            playbackPaused = false
        }
        mediaController.visibility = View.VISIBLE
        musicController.show(0)
    }

    // set the controller up
    private fun setController() {
        musicController = MusicController(requireActivity())

//        musicController.setAnchorView(recyclerView)
//        mediaController.visibility = View.GONE

        musicController.setPrevNextListeners({ playNext() }) { playPrev() }
        musicController.setMediaPlayer(this)
        musicController.isEnabled = true
        musicController.setAnchorView(mediaController)
        musicController.alpha = .7f
    }

    override fun start() {
        musicService.go()
    }

    override fun pause() {
        playbackPaused = true
        musicService.pausePlayer()
    }

    private fun playNext() {
        musicService.playNext()
        if (playbackPaused) {
            setController()
            playbackPaused = false
        }
        musicController.show(0)
    }

    private fun playPrev() {
        musicService.playPrev()
        if (playbackPaused) {
            setController()
            playbackPaused = false
        }
        musicController.show(0)
    }

    override fun getDuration(): Int {
        return if (musicBound && musicService.isPng) musicService.dur else 0
    }

    override fun getCurrentPosition(): Int {
        return if (musicBound && musicService.isPng) musicService.position else 0
    }

    override fun seekTo(pos: Int) {
        musicService.seek(pos)
    }

    override fun isPlaying(): Boolean {
        return if (musicBound) musicService.isPng else false
    }

    override fun getBufferPercentage(): Int {
        return 0
    }

    override fun canPause(): Boolean {
        return true
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    override fun canSeekForward(): Boolean {
        return true
    }

    override fun getAudioSessionId(): Int {
        return 0
    }

    /** */
    private fun checkUserPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 123)
            return
        }
        loadSongs()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            123 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadSongs()
            } else {
                Toast.makeText(activity, "Permission Denied", Toast.LENGTH_SHORT).show()
                checkUserPermission()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun loadSongs() {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Audio.Media.IS_MUSIC}!=0 AND ${MediaStore.Audio.Media.DATA} LIKE '%music%'"
        val cursor = requireActivity().contentResolver.query(uri, null, selection, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    var name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    var duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val s = name.split("-").toTypedArray()
                    if (s.size == 2) name = s[1].trim { it <= ' ' }
                    val artist = s[0].trim { it <= ' ' }
                    val d = duration.toInt() / 1000
                    val min = d / 60
                    val sec = d % 60
                    duration = String.format("%02d:%02d", min, sec)
                    val songInfo = SongInfo(name, artist, url, duration)
                    songList.add(songInfo)
                } while (cursor.moveToNext())
            }
            cursor.close()
            songAdapter = SongAdapter(requireContext(), songList)
        }
    }
}
