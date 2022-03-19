package com.rrwood.adfreecell;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * A quick and dirty wrapper class to support playing audio
 *
 * @author woodr2
 *
 */
public class AudioPlayer {
    private MediaPlayer mediaPlayer;

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void play(Context c, int soundRef) {
        stop();

        mediaPlayer = MediaPlayer.create(c, soundRef);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                stop();
            }
        });

        mediaPlayer.start();
    }
}