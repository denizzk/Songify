package com.dkarakaya.songify.mediaplayer

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
import androidx.core.app.NotificationCompat
import com.dkarakaya.songify.MainActivity
import com.dkarakaya.songify.MainActivity.Companion.setPlayingSongDetails
import com.dkarakaya.songify.R
import com.dkarakaya.songify.model.SongInfo
import timber.log.Timber

class MusicService : Service(), OnPreparedListener, MediaPlayer.OnErrorListener, OnCompletionListener {
    private lateinit var notificationManager: NotificationManager
    private lateinit var mediaSession: MediaSessionCompat
    private val musicBinder = MusicBinder()

    //media player
    private lateinit var player: MediaPlayer

    //song list
    private lateinit var songs: MutableList<SongInfo>

    private lateinit var playingSong: SongInfo

    //current position
    private var playingSongPosition = 0

    override fun onCreate() {
        //create the service
        super.onCreate()
        playingSongPosition = 0
        initMusicPlayer()
    }

    /**
     *Sets player properties
     */
    private fun initMusicPlayer() {
        //create player
        if (!::player.isInitialized) {
            player = MediaPlayer()
        }
        applyFilterToReceiver()
        mediaSession = MediaSessionCompat(applicationContext, MEDIA_SESSION_TAG)
        player.apply {
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setOnPreparedListener(this@MusicService)
            setOnCompletionListener(this@MusicService)
            setOnErrorListener(this@MusicService)
        }
    }

    private fun applyFilterToReceiver() {
        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        val filterHeadphoneButton = IntentFilter(Intent.ACTION_MEDIA_BUTTON)

        registerReceiver(noisyReceiver, filter)
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
        mediaSession.release()
        player.release()
    }

    fun setList(songs: MutableList<SongInfo>) {
        this.songs = songs
    }

    inner class MusicBinder : Binder() {
        val service: MusicService
            get() = this@MusicService
    }

    fun playSong() {
        player.apply {
            reset()
            //get song
            try {
                playingSong = songs[playingSongPosition]
                setDataSource(playingSong.songUrl)
            } catch (e: Exception) {
                Timber.e(e, "Error setting data source")
            }
            prepareAsync()
        }
    }

    override fun onPrepared(mediaPlayer: MediaPlayer) {
        //start playback
        mediaPlayer.start()
        setPlayingSongDetails(playingSong.songTitle, playingSong.artistName)
//        createNotification()
    }

    private fun createNotification() {
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
                .setContentTitle(playingSong.songTitle)
                .setContentText(playingSong.artistName)
                .addAction(R.drawable.ic_previous, "Previous", null)
                .addAction(R.drawable.ic_pause, "Pause", null)
                .addAction(R.drawable.ic_next, "Next", null)
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.sessionToken))
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
        startForeground(NOTIFY_ID, notification)
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
            } else {
                go()
            }
        }
    }

    fun setSong(songIndex: Int) {
        playingSongPosition = songIndex
    }

    val position: Int
        get() = player.currentPosition

    val duration: Int
        get() = player.duration

    val isPlaying: Boolean
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
        playingSongPosition--
        if (playingSongPosition < 0) playingSongPosition = songs.size - 1
        playSong()
    }

    //skip to next
    fun playNext() {
        playingSongPosition++
        if (playingSongPosition >= songs.size) playingSongPosition = 0
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
        notificationManager.cancel(NOTIFY_ID)
        player.apply {
            stop()
            reset()
            release()
        }
        return false
    }

    companion object {
        private const val NOTIFY_ID = 1

        const val NOTIFICATION_CHANNEL_ID = "com.dkarakaya.songify"
        const val CHANNEL_NAME = "Songify"
        const val MEDIA_SESSION_TAG = "mediaSession"
    }
}
