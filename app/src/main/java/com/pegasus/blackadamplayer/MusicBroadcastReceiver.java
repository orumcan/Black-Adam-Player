package com.pegasus.blackadamplayer;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MusicBroadcastReceiver extends BroadcastReceiver {

    MainActivity activity;

    public MusicBroadcastReceiver(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
            int state = intent.getIntExtra("state", -1);
            if(state == 0){

                Log.i("Unplugged", "Headphone Unplugged " + state);
                Log.i("Playing", "Music is playing " + activity.isPlaying());
                if(activity.isPlaying()) {
                    activity.pause();
                }
            }
        }
    }
}
