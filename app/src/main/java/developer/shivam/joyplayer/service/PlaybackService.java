package developer.shivam.joyplayer.service;

/**
 * This class is an implementation of a Bound Service
 * which acts like a Server in a Client-Server architecture
 * where the clients are the another Application components
 * like Activities
 */

import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.daasuu.ei.Ease;
import com.daasuu.ei.EasingInterpolator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import developer.shivam.joyplayer.R;
import developer.shivam.joyplayer.activity.NowPlaying;
import developer.shivam.joyplayer.listener.PlaybackListener;
import developer.shivam.joyplayer.pojo.Songs;
import developer.shivam.joyplayer.util.HelperMethods;
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

    private int playerState = State.IDLE;

    private List<Songs> songsList = new ArrayList<>();

    /**
     * Play, pause, next and previous song.
     * Intent action to control song playback from notification
     * button.
     */
    public static final String ACTION_PLAY = "developer.shivam.joyplayer.action.PLAY";
    public static final String ACTION_PAUSE = "developer.shivam.joyplayer.action.PAUSE";
    public static final String ACTION_NEXT = "developer.shivam.joyplayer.action.NEXT";
    public static final String ACTION_PREVIOUS = "developer.shivam.joyplayer.action.PREVIOUS";

    /**
     * NOTIFICATION_ID is the common notification id
     * used to display current running song name in status bar
     * and to set service running in foreground.
     */
    final int NOTIFICATION_ID = 200;

    PlaybackListener listener;
    private CircleImageView floatingIcon;
    private boolean isRunning = false;

    public void setPlaybackListener(final PlaybackListener listener) {
        this.listener = listener;
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

        floatingIcon = new CircleImageView(mContext);
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
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (listener != null) {
            listener.onCompletion();
        } else {
            if (position < songsList.size() - 1) {
                position += 1;
                setPlayerPosition(0);
                setSongUri(songsList.get(position).getSongUri());
                playSong(playerState);
            } else {
                position = 0;
                setPlayerPosition(0);
                setSongUri(songsList.get(position).getSongUri());
                playSong(playerState);
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
        if (state == State.PLAY) {
            mPlayer.reset();
            try {
                mPlayer.setDataSource(mContext, getSongUri());
            } catch (IOException e) {
                e.printStackTrace();
            }
            /**
             * prepareAsync() will asynchronously prepare the media player
             *  and when its prepared it will call the onPrepared method in which we
             *  will call the mediaPlayer.start() to start the media playing
             */
            mPlayer.prepareAsync();
            playerState = state;
        } else if (state == State.PAUSE) {
            mPlayer.reset();
            playerState = state;
            try {
                mPlayer.setDataSource(mContext, getSongUri());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
        Log.d(TAG, "Position : " + position);
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

        /**
         * Finally we need to unregister the receiver that
         *  listen to close the service from the notification
         */
        unregisterReceiver(closeServiceReceiver);

        isRunning = false;

        if (floatingIcon != null) {
            mWindowManager.removeView(floatingIcon);
        }

        /**
         * Un-Linking the phoneStateListener that was used to pause the
         *  song when phone rings.
         */
        TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mTelephonyManager != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    /**
     * Below is the code for showing the notification in
     * the notification bar which shows name of the current playing
     * media file and on tapping that notification an intent will be
     * fired to the NowPlaying activity
     */
    public void showNotification() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        RemoteViews notificationView = new RemoteViews(getPackageName(), R.layout.layout_notification);
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
            if (playerState == State.PLAY) {
                mPlayer.pause();
                playerState = State.PAUSE;
            }
            stopForeground(true);
            stopSelf();
        }
    };

    private void showBubble() {
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        Glide.with(mContext)
                .load(Retriever.getAlbumArtUri(Long.parseLong(getSongsList().get(getPosition()).getAlbumId())))
                .into(floatingIcon);

        int floatingIconDimension = HelperMethods.getDpFromPixel(mContext, 60);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                floatingIconDimension,
                floatingIconDimension,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;

        if (isRunning) {
            Glide.with(mContext)
                    .load(Retriever.getAlbumArtUri(Long.parseLong(getSongsList().get(getPosition()).getAlbumId())))
                    .placeholder(R.drawable.default_album_art)
                    .into(floatingIcon);
        } else {
            mWindowManager.addView(floatingIcon, params);
            isRunning = true;
        }

        try {
            floatingIcon.setOnTouchListener(new View.OnTouchListener() {

                private WindowManager.LayoutParams mParams = params;

                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;

                int TOUCH_DELAY = 300;

                long tapMilliSeconds = 0;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {

                        case MotionEvent.ACTION_DOWN:
                            initialX = mParams.x;
                            initialY = mParams.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();

                            tapMilliSeconds = System.currentTimeMillis();

                            break;

                        case MotionEvent.ACTION_UP:
                            if (event.getRawX() < (mWindowManager.getDefaultDisplay().getWidth() / 2)) {
                                moveToBoundary(mWindowManager, mParams, 10);
                            } else {
                                moveToBoundary(mWindowManager, mParams, mWindowManager.getDefaultDisplay().getWidth() - 10);
                            }
                            break;

                        case MotionEvent.ACTION_MOVE:
                            mParams.x = (int) (event.getRawX() - initialTouchX) + initialX;
                            mParams.y = (int) (event.getRawY() - initialTouchY) + initialY;
                            mWindowManager.updateViewLayout(floatingIcon, mParams);
                            break;
                    }
                    return false;
                }

            });
        } catch (Exception e) {
            //Handle exception here
        }

    }

    public void moveToBoundary(final WindowManager manager, WindowManager.LayoutParams params, int finalPosition) {
        final WindowManager.LayoutParams mParams = params;
        int startPosition = params.x;
        ValueAnimator animator = ValueAnimator.ofInt(startPosition, finalPosition);
        animator.setDuration(100);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mParams.x = (int) valueAnimator.getAnimatedValue();
                ;
                manager.updateViewLayout(floatingIcon, mParams);
            }
        });
        animator.setInterpolator(new EasingInterpolator(Ease.BACK_OUT));
        animator.start();
    }
}
