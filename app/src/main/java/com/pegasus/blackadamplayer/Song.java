package com.pegasus.blackadamplayer;

public class Song {

    private int id;
    private String title;
    private String artist;
    private String album;
    private String duration;
    private String cover;

    public Song(int songID, String songTitle, String songArtist, String songAlbum, String songDuration, String albumCover) {

        id = songID;
        title = songTitle;
        artist = songArtist;
        album = songAlbum;
        duration = songDuration;
        cover = albumCover;
    }

    public int getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public String getAlbum(){return album;}
    public String getDuration(){return duration;}
    public String getCover(){return cover;}

}
