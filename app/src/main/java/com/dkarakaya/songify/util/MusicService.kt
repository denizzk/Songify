package com.dkarakaya.songify.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dkarakaya.songify.MainActivity
import com.dkarakaya.songify.MainActivity.Companion.setCurSongDetails
import com.dkarakaya.songify.R
import com.dkarakaya.songify.model.SongInfo

class MusicService : Service(), OnPreparedListener, MediaPlayer.OnErrorListener, OnCompletionListener {
    private var notificationManager: NotificationManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private val musicBinder = MusicBinder()

    //media player
    private lateinit var player: MediaPlayer

    //song list
    private var songs: MutableList<SongInfo>? = null

    //current position
    private var songPosition = 0
    private var playingSong: SongInfo? = null

    override fun onCreate() {
        //create the service
        super.onCreate()

        //initialize position
        songPosition = 0

        //create player
        player = MediaPlayer()
        mediaSession = MediaSessionCompat(applicationContext, MEDIA_SESSION_TAG)

        applyFilterToReceiver()

        initMusicPlayer()
    }

    private fun applyFilterToReceiver() {
        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(noisyReceiver, filter)
        val filterHeadphoneButton = IntentFilter(Intent.ACTION_MEDIA_BUTTON)
        registerReceiver(headphoneButton, filterHeadphoneButton)
    }

    private fun createNotificationChannel() {
        val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_NONE)
        notificationChannel.lightColor = Color.BLUE
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(notificationChannel)
    }

    override fun onDestroy() {
        stopForeground(true)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(NOTIFY_ID)

        unregisterReceiver(noisyReceiver)
        unregisterReceiver(headphoneButton)
    }

    private fun initMusicPlayer() {
        //set player properties
        player.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        player.setAudioStreamType(AudioManager.STREAM_MUSIC)
        player.setOnPreparedListener(this)
        player.setOnCompletionListener(this)
        player.setOnErrorListener(this)
    }

    fun setList(songs: MutableList<SongInfo>) {
        this.songs = songs
    }

    inner class MusicBinder : Binder() {
        val service: MusicService
            get() = this@MusicService
    }

    fun playSong() {
        //play song
        player.reset()
        //get song
        val playSong = songs!![songPosition]
        playingSong = playSong
        try {
            player.setDataSource(playSong.songUrl)
        } catch (e: Exception) {
            Log.e("MUSIC SERVICE", "Error setting data source", e)
        }
        player.prepareAsync()
    }

    override fun onPrepared(mp: MediaPlayer) {
        //start playback
        mp.start()
        val notIntent = Intent(this, MainActivity::class.java)
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        createNotificationChannel()
        val artwork = BitmapFactory.decodeResource(resources, R.mipmap.ic_songify)
        val notificationBuilder = NotificationCompat.Builder(this,
                NOTIFICATION_CHANNEL_ID)
        val notification = notificationBuilder
                .setContentIntent(pendInt)
                .setSmallIcon(R.drawable.ic_note)
                .setLargeIcon(artwork)
                .setOngoing(true)
                .setContentTitle(playingSong!!.songTitle)
                .setContentText(playingSong!!.artistName)
                .addAction(R.drawable.ic_previous, "Previous", null)
                .addAction(R.drawable.ic_pause, "Pause", null)
                .addAction(R.drawable.ic_next, "Next", null)
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession!!.sessionToken))
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
        startForeground(NOTIFY_ID, notification)
        setCurSongDetails(playingSong!!.songTitle, playingSong!!.artistName)
    }

    private val noisyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (player.isPlaying) {
                pausePlayer()
            }
        }
    }
    private val headphoneButton: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (player.isPlaying) {
                pausePlayer()
            } else if (!player.isPlaying) {
                go()
            }
        }
    }

    fun setSong(songIndex: Int) {
        songPosition = songIndex
    }

    val position: Int
        get() = player.currentPosition

    val dur: Int
        get() = player.duration

    val isPng: Boolean
        get() = player.isPlaying

    fun pausePlayer() {
        player.pause()
    }

    fun seek(position: Int) {
        player.seekTo(position)
    }

    fun go() {
        player.start()
    }

    fun playPrev() {
        songPosition--
        if (songPosition < 0) songPosition = songs!!.size - 1
        playSong()
    }

    //skip to next
    fun playNext() {
        songPosition++
        if (songPosition >= songs!!.size) songPosition = 0
        playSong()
    }

    override fun onCompletion(mp: MediaPlayer) {
        if (player.currentPosition > 0) {
            mp.reset()
            playNext()
        }
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        mp.reset()
        return false
    }

    override fun onBind(intent: Intent): IBinder? {
        return musicBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager!!.cancel(NOTIFY_ID)
        player.stop()
        player.release()
        return false
    }

    companion object {
        private const val NOTIFY_ID = 1

        const val NOTIFICATION_CHANNEL_ID = "com.dkarakaya.songify"
        const val CHANNEL_NAME = "Songify"
        const val MEDIA_SESSION_TAG = "mediaSession"
    }
}
