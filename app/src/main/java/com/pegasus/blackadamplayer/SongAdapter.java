package com.pegasus.blackadamplayer;


import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.LayoutInflater;
import android.content.Context;

import java.util.ArrayList;

public class SongAdapter extends BaseAdapter {

    private ArrayList<Song> songs;
    private LayoutInflater songInflater;

    public SongAdapter(Context context, ArrayList<Song> theSongs){
        songs = theSongs;
        songInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LinearLayout songLayout = (LinearLayout)songInflater.inflate(R.layout.song, parent, false);

        TextView songView = (TextView)songLayout.findViewById(R.id.songTitle);
        TextView artistView = (TextView)songLayout.findViewById(R.id.songArtist);
        TextView albumView = (TextView)songLayout.findViewById(R.id.songAlbum);
        TextView durationView = (TextView)songLayout.findViewById(R.id.songDuration);

        ImageView coverView = (ImageView)songLayout.findViewById(R.id.albumCover);


        Song currSong = songs.get(position);

        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());
        albumView.setText(currSong.getAlbum());
        durationView.setText(currSong.getDuration());

        coverView.setImageDrawable(Drawable.createFromPath(currSong.getCover()));

        songLayout.setTag(position);
        return songLayout;

    }




}
