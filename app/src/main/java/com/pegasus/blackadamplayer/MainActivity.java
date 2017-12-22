package com.pegasus.blackadamplayer;


import android.content.IntentFilter;
import android.content.res.Resources;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.widget.MediaController.MediaPlayerControl;

import android.widget.ListView;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.View;
import android.widget.PopupMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class MainActivity extends ActionBarActivity implements MediaPlayerControl, PopupMenu.OnMenuItemClickListener {

    private ArrayList<Song> songList;

    private Menu menu;

    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;

    private SongController controller;

    private boolean paused = false;
    private boolean playbackPaused = false;

    private MusicBroadcastReceiver musicBroadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ListView songView = (ListView) findViewById(R.id.songList);
        songList = new ArrayList<>();

        getSongList();
        sortSongs();


        SongAdapter songAdapter = new SongAdapter(this, songList);
        songView.setAdapter(songAdapter);

        setController();

        musicBroadcastReceiver = new MusicBroadcastReceiver(this);

    }

    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            musicService = binder.getService();
            musicService.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent == null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    public void songPicked(View view){

        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();
/*
        ListView songTitle = (ListView) findViewById(R.id.songTitle);
        songTitle.setBackgroundColor(R.color.HighlightBackground);
*/
        if(playbackPaused){
            setController();
            playbackPaused = false;
        }
        controller.show(0);
        setController();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        this.menu = menu;

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.action_shuffle:
                musicService.switchShuffleMode();
                if(musicService.getShuffle()) {
                    menu.getItem(1).setIcon(getResources().getDrawable(R.drawable.bapl_shuffle1));
                    resetRepeatIcon();
                }
                else
                    resetShuffleIcon();
                break;

            case R.id.action_repeat:
                musicService.switchRepeatMode();
                if(musicService.getRepeat()) {
                    menu.getItem(2).setIcon(getResources().getDrawable(R.drawable.bapl_repeat1));
                    resetShuffleIcon();
                }
                else
                    resetRepeatIcon();
                break;

            case R.id.filter:
                showPopup();
                break;

            case R.id.action_end:
                //onDestroy
                stopService(playIntent);
                musicService = null;
                System.exit(0);
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    public void resetShuffleIcon(){
        menu.getItem(1).setIcon(getResources().getDrawable(R.drawable.bapl_shuffle0));
    }

    public void resetRepeatIcon(){
        menu.getItem(2).setIcon(getResources().getDrawable(R.drawable.bapl_repeat0));
    }

    public void showPopup(){
        PopupMenu popup = new PopupMenu(this, findViewById(R.id.filter));
        MenuInflater inflate = popup.getMenuInflater();
        inflate.inflate(R.menu.popup, popup.getMenu());
        popup.show();

    }

    public void getSongList(){

        ContentResolver bapResolver = getContentResolver();
        Uri bapUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri bapAlbumUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;

        Cursor bapCursor = bapResolver.query(bapUri, null, null, null, null);
        Cursor bapAlbumCursor = bapResolver.query(bapAlbumUri, null, null, null, null);

        if(bapCursor!=null && bapCursor.moveToFirst() && bapAlbumCursor!=null && bapAlbumCursor.moveToFirst()){

            SongFactory factory = new SongFactory(bapCursor, bapAlbumCursor);

            //add songs to list
            do { songList.add(factory.generate()); }
            while (factory.cursorNext() && factory.albumCursorNext());
        }

    }

    public void sortSongs(){

        Collections.sort    (songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });
    }
    public void onDestroy(){

        stopService(playIntent);
        musicService = null;
        super.onDestroy();
    }


    private void playNext(){
        musicService.playNext();
        if(playbackPaused){
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    private void playPrevious(){
        musicService.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    private void setController(){

        controller = new SongController(this);

        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.songList));
        controller.setEnabled(true);

        setListeners();
    }

    private void setListeners() {
        //Previous Next Buttons
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrevious();
            }
        });
    }

    @Override
    protected void onPause(){
        super.onPause();
        paused = true;
        unregisterReceiver(musicBroadcastReceiver);
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(paused){
            setController();
            paused = false;
        }
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(musicBroadcastReceiver, filter);
    }

    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }



    /*
    ** MediaPlayerControl
    */

    @Override
    public void start() {

        musicService.startPlayer();
    }

    @Override
    public void pause() {

        playbackPaused = true;
        musicService.pausePlayer();
    }

    @Override
    public int getDuration() {
        if (musicService != null && musicBound && musicService.isPlaying())
            return musicService.getDuration();
        else
            return 0;
    }

    @Override
    public int getCurrentPosition() {

        if (musicService != null && musicBound && musicService.isPlaying())
            return musicService.getPosition();
        else
            return 0;
    }

    @Override
    public void seekTo(int pos) {

        musicService.seek(pos);
    }

    @Override
    public boolean isPlaying() {

        return (musicService != null) && musicBound && musicService.isPlaying();
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

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }
}
