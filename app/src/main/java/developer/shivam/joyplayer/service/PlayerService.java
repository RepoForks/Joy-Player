package developer.shivam.joyplayer.service;

/**
 * This class is an implementation of a Bound Service
 *  which acts like a Server in a Client-Server architecture
 *  where the clients are the another Application components
 *  like Activities
 */

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;

import developer.shivam.joyplayer.util.State;

/**
 * A bound service that serves playing music in background
 *  Its life is same of the application ie. start with the launch
 *  of application and ends with the onDestroy(). It will not run
 *  indefinitely in the background
 */
public class PlayerService extends Service implements MediaPlayer.OnPreparedListener,
                                                                MediaPlayer.OnErrorListener,
                                                                    MediaPlayer.OnCompletionListener {

    private static final String TAG = "PlayerService";
    private Context mContext = PlayerService.this;

    private MediaPlayer mPlayer = null;
    private Uri songUri;
    private String playerState = "";

    /**
     * playerPosition is used to get the position
     *  of current playing song
     */
    private int playerPosition = 0;

    /**
     * In a bound service binder is used to bind service
     *  with clients
     */
    private IBinder mBinder = new PlayerBinder();

    public class PlayerBinder extends Binder {
        public PlayerService getService() {
            //This will return the object of the PlayerService
            return PlayerService.this;
        }
    }

    /**
     * onCreate() is the first method in the service
     *  lifecycle to be called. We will initialize the mediaPlayer here
     */
    @Override
    public void onCreate() {
        super.onCreate();

        mPlayer = new MediaPlayer();
        initPlayer();
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnCompletionListener(this);
    }

    private void initPlayer() {
        mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    /**
     * onBind() method is to be imported in a Bound Service
     *  which return the IBinder object so that client (such as Activities)
     *  can interact.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "Songs playing completed");
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(TAG, "Error playing media");
        return false;
    }

    public void playSong() {
        mPlayer.start();
        mPlayer.reset();
        playerState = State.PLAY;
        try {
            mPlayer.setDataSource(mContext, getSongUri());
        } catch (IOException e) {
            Log.d(TAG, "Error playing in media content");
            e.printStackTrace();
        }
        mPlayer.prepareAsync();
    }

    /**
     * This method play/pause the media content
     *  on the basis of playerState
     */
    public void playPause() {
        if (playerState.equals(State.PLAY)) {
            mPlayer.pause();
        } else if (playerState.equals(State.PAUSE)) {
            mPlayer.start();
        }
    }

    public int getPlayerPosition() {
        playerPosition = mPlayer.getCurrentPosition();
        return playerPosition;
    }

    public Uri getSongUri() {
        return songUri;
    }

    public void setSongUri(Uri songUri) {
        this.songUri = songUri;
    }

    /**
     * When clients unbind the service onUnbind of Service
     *  class is called. In this case we will stop
     *  the media player and then release it.
     */
    @Override
    public boolean onUnbind(Intent intent) {
        mPlayer.stop();
        mPlayer.release();
        return super.onUnbind(intent);
    }
}
