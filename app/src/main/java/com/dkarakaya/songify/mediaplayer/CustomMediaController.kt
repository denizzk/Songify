package com.dkarakaya.songify.mediaplayer

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.widget.MediaController
import com.dkarakaya.songify.model.SongInfo

class CustomMediaController(context: Context) : MediaController(context), MediaController.MediaPlayerControl {

    lateinit var musicService: MusicService
    private var playbackPaused = false
    private var musicBound = false

    companion object {
        private lateinit var controller: CustomMediaController

        fun initMediaController(context: Context): CustomMediaController {
            if (!::controller.isInitialized) {
                controller = CustomMediaController(context)
            }
            controller.apply {
                controller.apply {
                    setPrevNextListeners({ playNext() }) { playPrev() }
                    setMediaPlayer(this)
                    isEnabled = true
                    alpha = .7f
                }
            }
            return controller
        }
    }

    override fun hide() {}

    fun hideController() {
        super.hide()
    }

    /**
     * Connects to the service
     */
    fun musicConnection(songList: MutableList<SongInfo>): ServiceConnection {
        return object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                bindMusicService(binder)
                //pass list
                musicService.setList(songList)
                musicBound = true
                show(0)
            }

            override fun onServiceDisconnected(name: ComponentName) {
                musicBound = false
            }
        }
    }

    fun bindMusicService(binder: IBinder) {
        val musicBinder = binder as MusicService.MusicBinder
        //get service
        musicService = musicBinder.service
    }

    fun songPicked(songPos: Int) {
        musicService.setSong(songPos)
        musicService.playSong()
        if (playbackPaused) {
            playbackPaused = false
        }
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
            playbackPaused = false
        }
    }

    private fun playPrev() {
        musicService.playPrev()
        if (playbackPaused) {
            playbackPaused = false
        }
    }

    override fun getDuration(): Int {
        return if (isBoundAndPlaying()) musicService.duration else 0
    }

    override fun getCurrentPosition(): Int {
        return if (isBoundAndPlaying()) musicService.position else 0
    }

    private fun isBoundAndPlaying() = musicBound && musicService.isPlaying

    override fun seekTo(pos: Int) {
        musicService.seek(pos)
    }

    override fun isPlaying(): Boolean {
        return if (musicBound) {
            musicService.isPlaying
        } else false
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
}
