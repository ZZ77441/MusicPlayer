package com.example.musicplayer;

public class MusicBean {
    private String id;//歌曲编号
    private String song;//歌曲名称
    private String singer;//歌手名称
    private String path;//路径
    private String duration;//时长
    /*private String album;//专辑名

    */

    public MusicBean() {
    }

    public MusicBean(String id, String song, String singer, String path, String duration) {
        this.id = id;
        this.song = song;
        this.singer = singer;
        this.path = path;
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSong() {
        return song;
    }

    public void setSong(String song) {
        this.song = song;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}


