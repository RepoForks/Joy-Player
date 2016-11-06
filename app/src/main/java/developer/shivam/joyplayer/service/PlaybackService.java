package developer.shivam.joyplayer.service;

/**
 * This class is an implementation of a Bound Service
 * which acts like a Server in a Client-Server architecture
 * where the clients are the another Application components
 * like Activities
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import developer.shivam.joyplayer.R;
import developer.shivam.joyplayer.activity.NowPlaying;
import developer.shivam.joyplayer.listener.PlaybackListener;
import developer.shivam.joyplayer.pojo.Songs;
import developer.shivam.joyplayer.util.Retriever;
import developer.shivam.joyplayer.util.State;

/**
 * A bound service that serves playing music in background
 * Its life is same of the application ie. start with the launch
 * of application and ends with the onDestroy(). It will not run
 * indefinitely in the background
 */
public class PlaybackService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private static final String TAG = "PlaybackService";
    private Context mContext = PlaybackService.this;

    /**
     * mPlayer is a media player object used to perform
     * basic MediaPlayer functionality
     */
    public MediaPlayer mPlayer = null;

    private Uri songUri;

    private int playerState = 0;

    private List<Songs> songsList = new ArrayList<>();

    public boolean isRunningInBackground = false;

    /**
     * Play, pause, next and previous song.
     * Intent action to control song playback from notification
     * button.
     */
    public static final String ACTION_PLAY = "developer.shivam.joyplayer.action.PLAY";
    public static final String ACTION_PAUSE = "developer.shivam.joyplayer.action.PAUSE";
    public static final String ACTION_NEXT = "developer.shivam.joyplayer.action.NEXT";
    public static final String ACTION_PREVIOUS = "developer.shivam.joyplayer.action.PREVIOUS";
    public static final String ACTION_STOP_SERVICE = "developer.shivam.joyplayer.action.STOP_SERVICE";

    /**
     * serviceHandler is used to print log in logcat after
     * every second to say - I'm running on the service behalf
     */
    Handler serviceHandler;

    /**
     * timeStamp used to record the current time in millis
     */
    long timeStamp = 0;

    private ServiceRunnable serviceRunnable;

    /**
     * NOTIFICATION_ID is the common notification id
     * used to display current running song name in status bar
     * and to set service running in foreground.
     */
    final int NOTIFICATION_ID = 200;

    PlaybackListener listener;

    public void setPlaybackListener(final PlaybackListener listener) {
        this.listener = listener;
    }

    private class ServiceRunnable implements Runnable {

        @Override
        public void run() {
            if (System.currentTimeMillis() - timeStamp > 1000) {
                timeStamp = System.currentTimeMillis();
                System.out.println("I'm running");
            }
            serviceHandler.post(this);
        }
    }

    /**
     * In a bound service binder is used to bind service
     * with clients
     */
    private IBinder mBinder = new PlayerBinder();

    /**
     * position is used to keep track on
     * song at which position is playing
     */
    private int position;

    private PhoneStateListener mPhoneStateListener;

    /**
     * Window manager is user to add view above all view
     * In this app the bubbleView is used for giving
     * shortcut options
     */
    private WindowManager mWindowManager;

    /**
     * bubbleImageView is the view which will be added to the view
     */
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
     * lifecycle to be called. We will initialize the mediaPlayer here
     */
    @Override
    public void onCreate() {
        super.onCreate();

        mPlayer = new MediaPlayer();
        initPlayer();
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnCompletionListener(this);

        serviceRunnable = new ServiceRunnable();
        serviceHandler = new Handler();
        serviceHandler.post(serviceRunnable);

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
     * which return the IBinder object so that client (such as Activities)
     * can interact.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        /**
         * When media player is prepared asynchronously
         *  mediaPlayer.start is called to start song.
         */
        if (playerState == State.PAUSE)
            mp.pause();
        else
            mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (isRunningInBackground) {
            listener.onCompletion();
            if (position < songsList.size() - 1) {
                position += 1;
                setPlayerPosition(0);
                setSongUri(songsList.get(position).getSongUri());
                playSong(State.PLAY);
            } else {
                position = 0;
                setPlayerPosition(0);
                setSongUri(songsList.get(position).getSongUri());
                playSong(State.PLAY);
            }

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_PLAY:
                        Log.d("ACTION", "PLAY");
                        playPause();
                        break;
                    case ACTION_PAUSE:
                        Log.d("ACTION", "PAUSE");
                        playPause();
                        break;
                    case ACTION_NEXT:
                        Log.d("ACTION", "NEXT");
                        playNext();
                        break;
                    case ACTION_PREVIOUS:
                        Log.d("ACTION", "PREVIOUS");
                        playPrevious();
                        break;
                    case ACTION_STOP_SERVICE:
                        Log.d("ACTION", "STOP");
                        break;
                }
            }
        }

        /**
         * START_STICKY means that system will try to restart the service when
         *  its force-closed.
         */
        return START_NOT_STICKY;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(TAG, "Error playing media");
        return false;
    }

    public void playSong(int state) {
        mPlayer.reset();
        playerState = state;
        try {
            mPlayer.setDataSource(mContext, getSongUri());
        } catch (IOException e) {
            Log.d(TAG, "Error playing in media content");
            e.printStackTrace();
        }
        /**
         * prepareAsync() will asynchronously prepare the media player
         *  and when its prepared it will call the onPrepared method in which we
         *  will call the mediaPlayer.start() to start the media playing
         */
        mPlayer.prepareAsync();
        showNotification();
    }

    /**
     * This method play/pause the media content
     * on the basis of playerState
     */
    public void playPause() {
        if (playerState == State.PLAY) {
            playerState = State.PAUSE;
            mPlayer.pause();
            listener.onMusicPause();
        } else if (playerState == State.PAUSE) {
            playerState = State.PLAY;
            mPlayer.start();
            listener.onMusicPlay();
        }
    }

    /**
     * This method is used to play the previous song
     * if present in the list
     */
    public void playPrevious() {
        if (playerState == State.PLAY) {
            /**
             * If the current player position is greater than 1000 ms
             *  then the current playing song will restart else the
             *  previous song in the list will play
             */
            if (getPlayerPosition() > 1500) {
                mPlayer.pause();
                setPlayerPosition(0);
                mPlayer.start();
            } else {
                setPlayerPosition(0);
                if (getPosition() != 0) {
                    setPosition(getPosition() - 1);
                    setSongUri(songsList.get(position).getSongUri());
                    playSong(State.PLAY);
                } else {
                    setPosition(songsList.size() - 1);
                    setSongUri(songsList.get(position).getSongUri());
                    playSong(State.PLAY);
                }
            }
        } else if (playerState == State.PAUSE) {
            setPlayerPosition(0);
            if (getPosition() != 0) {
                setPosition(getPosition() - 1);
                setSongUri(songsList.get(position).getSongUri());
                playSong(State.PAUSE);
            } else {
                setPosition(songsList.size() - 1);
                setSongUri(songsList.get(position).getSongUri());
                playSong(State.PAUSE);
            }
        }
    }

    /**
     * This method is used to play the next song
     * in the list but id the last song of the list is
     * playing then on clicking this button first song will
     * be played.
     */
    public void playNext() {
        if (playerState == State.PLAY) {
            setPlayerPosition(0);
            if (getPosition() != (songsList.size() - 1)) {
                setPosition(getPosition() + 1);
                setSongUri(songsList.get(position).getSongUri());
                playSong(State.PLAY);
            } else {
                setPosition(0);
                setSongUri(songsList.get(position).getSongUri());
                playSong(State.PLAY);
            }
        } else if (playerState == State.PAUSE) {
            setPlayerPosition(0);
            if (getPosition() != (songsList.size() - 1)) {
                setPosition(getPosition() + 1);
                setSongUri(songsList.get(position).getSongUri());
                playSong(State.PAUSE);
            } else {
                setPosition(0);
                setSongUri(songsList.get(position).getSongUri());
                playSong(State.PAUSE);
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
    }

    private Uri getSongUri() {
        return songUri;
    }

    public void setSongUri(Uri songUri) {
        this.songUri = songUri;
    }

    public void setPosition(int position) {
        this.position = position;
        setSongUri(songsList.get(position).getSongUri());
    }

    public int getPosition() {
        return position;
    }

    /**
     * When clients unbind the service onUnbind of Service
     * class is called. In this case we will stop
     * the media player and then release it.
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

        serviceHandler.removeCallbacks(serviceRunnable);

        /**
         * Finally we need to unregister the receiver that
         *  listen to close the service from the notification
         */
        unregisterReceiver(closeServiceReceiver);

        /**
         * Un-Linking the phoneStateListener that was used to pause the
         *  song when phone rings.
         */
        TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mTelephonyManager != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    public void showNotification() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        RemoteViews notificationView = new RemoteViews(getPackageName(), R.layout.notification_media);
        try {
            notificationView.setImageViewUri(R.id.ivAlbumArt, Retriever.getAlbumArtUri(Long.parseLong(songsList.get(position).getAlbumId())));
        } catch (Exception e) {
            notificationView.setImageViewResource(R.id.ivAlbumArt, R.drawable.default_album_art);
        }
        notificationView.setTextViewText(R.id.notify_song_name, songsList.get(position).getName());

        Intent intent = new Intent(this, NowPlaying.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                100,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        registerReceiver(closeServiceReceiver, new IntentFilter("CLOSE_SERVICE_INTENT_FILTER"));

        PendingIntent stopServicePendingIntent = PendingIntent.getBroadcast(mContext,
                100,
                new Intent("CLOSE_SERVICE_INTENT_FILTER"),
                PendingIntent.FLAG_UPDATE_CURRENT);

        notificationView.setOnClickPendingIntent(R.id.ivStopService, stopServicePendingIntent);

        mNotification = notificationBuilder
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_play_arrow_black_24dp)
                .setOngoing(true)
                .setWhen(System.currentTimeMillis())
                .setContent(notificationView)
                .build();

        startForeground(NOTIFICATION_ID, mNotification);
    }

    protected BroadcastReceiver closeServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopForeground(true);
            stopSelf();
        }
    };
}
