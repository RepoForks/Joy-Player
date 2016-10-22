package developer.shivam.joyplayer.service;

/**
 * This class is an implementation of a Bound Service
 * which acts like a Server in a Client-Server architecture
 * where the clients are the another Application components
 * like Activities
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import developer.shivam.joyplayer.R;
import developer.shivam.joyplayer.model.Songs;
import developer.shivam.joyplayer.util.Collector;
import developer.shivam.joyplayer.util.State;

/**
 * A bound service that serves playing music in background
 *  Its life is same of the application ie. start with the launch
 *  of application and ends with the onDestroy(). It will not run
 *  indefinitely in the background
 */
public class PlaybackService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private static final String TAG = "PlaybackService";
    private Context mContext = PlaybackService.this;

    /**
     * mPlayer is a media player object used to perform
     *  basic MediaPlayer functionality
     */
    public MediaPlayer mPlayer = null;

    private Uri songUri;

    private int playerState = 0;

    private List<Songs> songsList = new ArrayList<>();

    public boolean isRunningInBackground = false;

    /**
     * In a bound service binder is used to bind service
     *  with clients
     */
    private IBinder mBinder = new PlayerBinder();

    /**
     * position is used to keep track on
     *  song at which position is playing
     */
    private int position;

    private PhoneStateListener mPhoneStateListener;

    /**
     * Window manager is user to add view above all view
     *  In this app the bubbleView is used for giving
     *  shortcut options
     */
    private WindowManager mWindowManager;

    /**
     * bubbleImageView is the view which will be added to the view
     */
    private ImageView bubbleImageView;
    private Notification mNotification;
    private Notification.Builder notificationBuilder;
    private NotificationManager notificationManager;

    public class PlayerBinder extends Binder {
        public PlaybackService getService() {
            //This will return the object of the PlaybackService
            return PlaybackService.this;
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

        /**
         * Here we will register the PHONE_STATE_LISTENER
         *  to pause/play songs according to the phone state
         */
        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    mPlayer.pause();
                } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                    mPlayer.start();
                } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    mPlayer.pause();
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };

        TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mTelephonyManager != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        notificationBuilder = new Notification.Builder(getApplicationContext());
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
        if (isRunningInBackground) {
            if (position < songsList.size() - 1) {
                position += 1;
                setPlayerPosition(0);
                setSongUri(songsList.get(position).getSongUri());
                playSong();
            } else {
                position = 0;
                setPlayerPosition(0);
                setSongUri(songsList.get(position).getSongUri());
                playSong();
            }
        }
        updateNotification(songsList.get(position).getName());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
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
        showNotification();
    }

    /**
     * This method play/pause the media content
     *  on the basis of playerState
     */
    public void playPause() {
        if (playerState == State.PLAY) {
            playerState = State.PAUSE;
            mPlayer.pause();
            mNotification = notificationBuilder.setAutoCancel(true).setOngoing(false).build();
            notificationManager.notify(1, mNotification);
        } else if (playerState == State.PAUSE) {
            playerState = State.PLAY;
            mPlayer.start();
        }
    }

    /**
     * This method is used to play the previous song
     *  if present in the list
     */
    public void playPrevious() {
        if (playerState == State.PLAY) {
            /**
             * If the current player position is greater than 1000 ms
             *  then the current playing song will restart else the
             *  previous song in the list will play
             */
            if (getPlayerPosition() > 1000) {
                mPlayer.pause();
                setPlayerPosition(0);
                mPlayer.start();
            } else {
                setPlayerPosition(0);
                if (getPosition() != 0) {
                    setPosition(getPosition() - 1);
                    setSongUri(songsList.get(position).getSongUri());
                    playSong();
                } else {
                    setPosition(songsList.size() - 1);
                    setSongUri(songsList.get(position).getSongUri());
                    playSong();
                }
            }
        } else if (playerState == State.PAUSE) {
            setPlayerPosition(0);
            if (getPosition() != 0) {
                setPosition(getPosition() - 1);
                setSongUri(songsList.get(position).getSongUri());
                playSong();
            } else {
                setPosition(songsList.size() - 1);
                setSongUri(songsList.get(position).getSongUri());
                playSong();
            }
        }
    }

    /**
     * This method is used to play the next song
     *  in the list but id the last song of the list is
     *  playing then on clicking this button first song will
     *  be played.
     */
    public void playNext() {
        if (playerState == State.PLAY) {
            setPlayerPosition(0);
            if (getPosition() != (songsList.size() - 1)) {
                setPosition(getPosition() + 1);
                setSongUri(songsList.get(position).getSongUri());
                playSong();
            } else {
                setPosition(0);
                setSongUri(songsList.get(position).getSongUri());
                playSong();
            }
        } else if (playerState == State.PAUSE) {
            setPlayerPosition(0);
            if (getPosition() != 0) {
                setPosition(getPosition() - 1);
                setSongUri(songsList.get(position).getSongUri());
                playSong();
            } else {
                setPosition(songsList.size() - 1);
                setSongUri(songsList.get(position).getSongUri());
                playSong();
            }
        }
    }

    public void setPlayerPosition(int playerPosition) {
        mPlayer.seekTo(playerPosition);
    }

    public int getPlayerPosition() {
        return mPlayer.getCurrentPosition();
    }

    /**
     * Below are the songsList getter and setter
     */
    public List<Songs> getSongsList() {
        return songsList;
    }

    public void setSongsList(List<Songs> songsList) {
        this.songsList = songsList;
        Log.d("Songs list size", String.valueOf(this.songsList.size()));
    }

    private Uri getSongUri() {
        return songUri;
    }

    public void setSongUri(Uri songUri) {
        this.songUri = songUri;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    /**
     * When clients unbind the service onUnbind of Service
     *  class is called. In this case we will stop
     *  the media player and then release it.
     */
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mPlayer.stop();
        mPlayer.release();


        TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mTelephonyManager != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        //stopForeground(true);
    }

    public void showNotification() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        RemoteViews notificationView = new RemoteViews(getPackageName(), R.layout.notification_media);
        notificationView.setImageViewUri(R.id.ivAlbumArt, Collector.getAlbumArtUri(Long.parseLong(songsList.get(position).getAlbumId())));
        notificationView.setTextViewText(R.id.notify_song_name, songsList.get(position).getName());

        mNotification = notificationBuilder
                .setSmallIcon(R.mipmap.ic_launcher).setOngoing(true)
                .setWhen(System.currentTimeMillis())
                .setContent(notificationView)
                .setOngoing(true)
                .setDefaults(Notification.FLAG_NO_CLEAR)
                .build();

        notificationManager.notify(1, mNotification);
    }

    private void updateNotification(String songName) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotification.contentView.setTextViewText(R.id.notify_song_name, songName);
        notificationManager.notify(200, mNotification);
    }
}
