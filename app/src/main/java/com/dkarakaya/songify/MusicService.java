package com.dkarakaya.songify;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.media.AudioManager;
import android.os.Binder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import android.app.Notification;
import android.app.PendingIntent;

import java.util.ArrayList;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    //media player
    private MediaPlayer player;
    //song list
    private ArrayList<SongInfo> songs;
    //current position
    private int songPosn;

    private SongInfo curSong;
    String NOTIFICATION_CHANNEL_ID = "com.dkarakaya.songify";
    String CHANNEL_NAME = "Songify";
    private static final int NOTIFY_ID = 1;
    NotificationManager manager;
    MediaSessionCompat mediaSession;

    private final IBinder musicBind = new MusicBinder();

    public void onCreate() {
        //create the service
        super.onCreate();
        //initialize position
        songPosn = 0;
        //create player
        player = new MediaPlayer();

        mediaSession = new MediaSessionCompat(getApplication(), "mediaSession");

        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mNoisyReceiver, filter);

        initMusicPlayer();
    }

    public void createNotificationChannel(){
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context
                .NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context
                .NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFY_ID);

        unregisterReceiver(mNoisyReceiver);
    }

    public void initMusicPlayer() {
        //set player properties
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<SongInfo> songs) {
        this.songs = songs;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public void playSong() {
        //play song
        player.reset();
        //get song
        SongInfo playSong = songs.get(songPosn);
        curSong = playSong;
        try {
            player.setDataSource(playSong.getSongUrl());
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        mp.start();

        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        createNotificationChannel();

        Bitmap artwork = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_songify);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,
                NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder
                .setContentIntent(pendInt)
                .setSmallIcon(R.drawable.ic_note)
                .setLargeIcon(artwork)
                .setOngoing(true)
                .setContentTitle(curSong.getSongTitle())
                .setContentText(curSong.getArtistName())
                .addAction(R.drawable.ic_previous, "Previous", null)
                .addAction(R.drawable.ic_pause, "Pause", null)
                .addAction(R.drawable.ic_next, "Next", null)
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken()))
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(NOTIFY_ID, notification);

        MainActivity.setCurSongDetails(curSong.getSongTitle(), curSong.getArtistName());
    }


    private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if( player != null && player.isPlaying() ) {
                pausePlayer();
            }
        }
    };

    public void setSong(int songIndex) {
        songPosn = songIndex;
    }

    public int getPosn() {
        return player.getCurrentPosition();
    }

    public int getDur() {
        return player.getDuration();
    }

    public boolean isPng() {
        return player.isPlaying();
    }

    public void pausePlayer() {
        player.pause();
    }

    public void seek(int posn) {
        player.seekTo(posn);
    }

    public void go() {
        player.start();
    }

    public void playPrev() {
        songPosn--;
        if (songPosn < 0) songPosn = songs.size() - 1;
        playSong();
    }

    //skip to next
    public void playNext() {
        songPosn++;
        if (songPosn >= songs.size()) songPosn = 0;
        playSong();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (player.getCurrentPosition() > 0) {
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFY_ID);
        player.stop();
        player.release();
        return false;
    }
}
