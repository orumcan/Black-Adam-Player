package com.pegasus.blackadamplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import android.content.ContentUris;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.PowerManager;
import android.provider.MediaStore;

import java.util.ArrayList;

import java.util.Random;
import android.app.Notification;
import android.app.PendingIntent;


public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosition;

    private String songTitle = "";
    private String songArtist = "";

    private boolean isShuffle = false;
    private boolean isRepeat = true;
    private Random rnd;


    private static final int NOTIFY_ID = 1;

    public void onCreate(){

        super.onCreate();
        songPosition = 0;
        player = new MediaPlayer();

        initMusicPlayer();

    }

    public void initMusicPlayer(){

        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> theSongs){
        songs = theSongs;
    }

    public class MusicBinder extends Binder {

        MusicService getService() {
            return MusicService.this;
        }

    }

    private final IBinder musicBind = new MusicBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    public void playSong(){
        player.reset();

        Song playSong = songs.get(songPosition);
        long currentSong = playSong.getID();

        songTitle = playSong.getTitle();
        songArtist = playSong.getArtist();

        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentSong);

        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            //Log.e("MusicService.java", "setDataSource error", e);
        }

        player.prepareAsync();

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(player.getCurrentPosition()> 0){
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
    public void onPrepared(MediaPlayer mp) {

        mp.start();

        Intent ntIntent = new Intent(this, MainActivity.class);
        ntIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, ntIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder ntBuilder = new Notification.Builder(this);

        ntBuilder.setContentIntent(pendingIntent).setSmallIcon(R.drawable.bapl_note1).setTicker(songTitle).setOngoing(true)
                .setContentText(songTitle + " - " + songArtist).setContentTitle("Now Playing...")
                .setAutoCancel(true)
                .addAction(R.drawable.bapl_prev,"Prev", pendingIntent)
                .addAction(R.drawable.bapl_play, "Play", pendingIntent)
                .addAction(R.drawable.bapl_next, "Next", pendingIntent).build();
        Notification ntf = ntBuilder.build();

        startForeground(NOTIFY_ID, ntf);


    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    public void setSong(int songIndex){

        songPosition = songIndex;
    }

    public void switchShuffleMode() {

        if (!isShuffle){
            isShuffle = true;
            isRepeat = false;
        }
        else {isShuffle = false;}
    }

    public void switchRepeatMode() {

        if (!isRepeat){
            isRepeat = true;
            isShuffle = false;
        }
        else {isRepeat = false;}
    }

    public boolean getShuffle() {

        return isShuffle;
    }

    public boolean getRepeat() {

        return isRepeat;
    }


    public void startPlayer(){
        player.start();
    }

    public void pausePlayer(){
        player.pause();
    }

    public boolean isPlaying(){
        return player.isPlaying();
    }

    public int getPosition(){
        return player.getCurrentPosition();
    }

    public int getDuration(){
        return player.getDuration();
    }

    public void seek(int position){
        player.seekTo(position);
    }

    public void playPrev(){
        if(isShuffle){
            int prevSong;
            rnd = new Random();

            prevSong = rnd.nextInt(songs.size());
            songPosition = prevSong;
            playSong();

        } else if(isRepeat){
            songPosition--;
            if (songPosition < 0)
                songPosition = songs.size() - 1;
            playSong();

        } else if(!isRepeat){
            songPosition--;
            if (songPosition > 0) {
                playSong();
            } else
                songPosition = 0;
        } else
            playSong();
    }

    public void playNext() {

        if(isShuffle){
            int nextSong;
            rnd = new Random();

            nextSong = rnd.nextInt(songs.size());
            songPosition = nextSong;
            playSong();

        } else if(isRepeat){
            songPosition++;
            if (songPosition >= songs.size())
                songPosition = 0;
            playSong();

        } else if(!isRepeat){
            songPosition++;
            if (songPosition < songs.size()) {
                playSong();
            } else
                songPosition = songs.size() -1;
        } else
            playSong();
    }

}
