package com.dkarakaya.songify;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.ArrayList;


import android.os.IBinder;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;

import com.dkarakaya.songify.MusicService.MusicBinder;

import android.widget.MediaController.MediaPlayerControl;

public class LibraryFragment extends Fragment implements MediaPlayerControl {

    private ArrayList<SongInfo> songList = new ArrayList<>();
    RecyclerView rvSongList;

    SongAdapter songAdapter;
    MediaPlayer mediaPlayer = new MediaPlayer();
    private Handler myHandler = new Handler();

    private boolean isPlaying = false;
    private int oldPos = -1;


    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;

    private MusicController controller;
    private boolean paused = false, playbackPaused = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_library, container, false);

        rvSongList = v.findViewById(R.id.songList);

        controller = new MusicController(getActivity());
        controller.setAnchorView(v.findViewById(R.id.songList));
        setController();

        songAdapter = new SongAdapter(getActivity(), songList);
        rvSongList.setAdapter(songAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        rvSongList.setLayoutManager(linearLayoutManager);
        songAdapter.setOnItemClickListener(new SongAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, final SongInfo
                    obj, final int position) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            songPicked(position);
                        } catch (Exception e) {
                        }
                    }

                };
                myHandler.postDelayed(runnable, 100);
            }
        });
        checkUserPermission();

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (paused) {
            setController();
            paused = false;
        }
    }

    @Override
    public void onStop() {
        controller.hide();
        super.onStop();
    }

    /***********************************service**************************************************/

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder) service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(getActivity(), MusicService.class);
            getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
        }
    }

    public void songPicked(int songPos) {
        musicSrv.setSong(songPos);
        musicSrv.playSong();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    /*****************************************controller*****************************************/

    private void setController() {
        //set the controller up
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        controller.setMediaPlayer(this);

        controller.setEnabled(true);
        FrameLayout.LayoutParams lpp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        controller.setLayoutParams(lpp);
        controller.setPadding(50, 0, 50,0);
        controller.setAlpha(.75f);
        controller.bringToFront();
    }

    @Override
    public void start() {
        musicSrv.go();
    }

    @Override
    public void pause() {
        playbackPaused=true;
        musicSrv.pausePlayer();
    }

    private void playNext(){
        musicSrv.playNext();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    private void playPrev(){
        musicSrv.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    @Override
    public int getDuration() {
        if (musicSrv != null && musicBound && musicSrv.isPng())
            return musicSrv.getDur();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicSrv != null && musicBound && musicSrv.isPng())
            return musicSrv.getPosn();
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if (musicSrv != null && musicBound)
            return musicSrv.isPng();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    /*********************************************************************************************/


    private void checkUserPermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission
                .READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
            return;
        }
        loadSongs();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 123:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadSongs();
                } else {
                    Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    checkUserPermission();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void loadSongs() {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0 AND " +
                MediaStore.Audio.Media.DATA + " LIKE '%music%'";
        Cursor cursor = getActivity().getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media
                            .TITLE));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media
                            .ARTIST));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media
                            .DATA));
                    String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio
                            .Media.DURATION));

                    String[] s = name.split("-");
                    if (s.length == 2)
                        name = s[1].trim();
                    artist = s[0].trim();

                    int d = Integer.parseInt(duration) / 1000;
                    int min = d / 60;
                    int sec = d % 60;

                    duration = String.format("%02d:%02d", min, sec);

                    SongInfo songInfo = new SongInfo(name, artist, url, duration);
                    songList.add(songInfo);

                } while (cursor.moveToNext());
            }

            cursor.close();
            songAdapter = new SongAdapter(getActivity(), songList);

        }
    }

}