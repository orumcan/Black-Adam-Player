package com.pegasus.blackadamplayer;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.widget.ImageView;

import java.util.concurrent.TimeUnit;

public class SongFactory {
    public final Cursor bapCursor;
    public final Cursor bapAlbumCursor;

    final int titleColumn;
    final int idColumn;
    final int artistColumn;
    final int albumColumn;
    final int durationColumn;
    final int coverColumn;

    public SongFactory(Cursor cursor, Cursor albumCursor){

        bapCursor = cursor;
        bapAlbumCursor = albumCursor;

        //get column index
        titleColumn = bapCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        idColumn = bapCursor.getColumnIndex(MediaStore.Audio.Media._ID);
        artistColumn = bapCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        albumColumn = bapCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        durationColumn = bapCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

        coverColumn = bapAlbumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);

    }

    public boolean cursorNext() { return bapCursor.moveToNext(); }
    public boolean albumCursorNext() {
        return bapAlbumCursor.moveToNext();
    }

    public Song generate() {

        int thisId = bapCursor.getInt(idColumn);
        String thisTitle = bapCursor.getString(titleColumn);
        String thisArtist = bapCursor.getString(artistColumn);
        String thisAlbum = bapCursor.getString(albumColumn);
        int thisDuration = bapCursor.getInt(durationColumn);

        String thisCover = bapAlbumCursor.getString(coverColumn);

        //milliseconds to duration
        long durationMin = TimeUnit.MINUTES.convert(thisDuration, TimeUnit.MILLISECONDS);
        long durationSec = TimeUnit.SECONDS.convert(thisDuration, TimeUnit.MILLISECONDS);

        String durationSeconds = Long.toString(durationSec % 60);
        if(durationSec%60 < 10) { durationSeconds = "0" + durationSeconds; }

        String durationStr = durationMin + ":" + durationSeconds;


        return new Song(thisId, thisTitle, thisArtist, thisAlbum, durationStr, thisCover);

    }
}
