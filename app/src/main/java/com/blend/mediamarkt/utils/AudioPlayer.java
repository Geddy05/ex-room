package com.blend.mediamarkt.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

import com.blend.mediamarkt.R;
import com.blend.mediamarkt.enumerations.Sounds;

/**
 * Created by geddy on 19/05/16.
 */
public class AudioPlayer {

    private MediaPlayer mMediaPlayer;
    private static Context context;
    private boolean mIsPause =false;
    private int mPausePosition = 0;
    private Sounds sound;

    public AudioPlayer(Context context,Sounds sound){
        this.context = context;
        mMediaPlayer = MediaPlayer.create(context, sound.getSound());
        mMediaPlayer.setLooping(true);
    }

    public  void startAudio(){
        mMediaPlayer.start();
    }

    public void pauseAudio(){
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mIsPause = true;
            mPausePosition = mMediaPlayer.getCurrentPosition();
            mMediaPlayer.pause();
        }
    }

    public void resumeAudio(){
        if(mMediaPlayer != null && mIsPause) {
            mMediaPlayer.seekTo(mPausePosition);
            mMediaPlayer.start();
            mIsPause = false;
        }
    }

    public void destroyAudio(){
        mMediaPlayer.stop();
        mMediaPlayer = null;
    }

    public void newSong(int song){
        mMediaPlayer.stop();
        mMediaPlayer = MediaPlayer.create(context,song);
    }
}
