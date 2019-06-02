package com.dkarakaya.songify;

public class SongInfo {
    private String songTitle;
    private String artistName;
    private String songUrl;
    private String songDuration;
    private String songThumbnail;

    public SongInfo() {
    }

    public SongInfo(String songTitle, String artistName, String songUrl, String songDuration) {
        this.songTitle = songTitle;
        this.artistName = artistName;
        this.songUrl = songUrl;
        this.songDuration = songDuration;
    }

    public SongInfo(String songTitle, String artistName, String songUrl, String songDuration,
                    String songThumbnail) {
        this.songTitle = songTitle;
        this.artistName = artistName;
        this.songUrl = songUrl;
        this.songDuration = songDuration;
        this.songThumbnail = songThumbnail;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }

    public String getSongDuration() {
        return songDuration;
    }

    public void setSongDuration(String songDuration) {
        this.songDuration = songDuration;
    }

    public String getSongThumbnail() {
        return songThumbnail;
    }

    public void setSongThumbnail(String songThumbnail) {
        this.songThumbnail = songThumbnail;
    }
}
