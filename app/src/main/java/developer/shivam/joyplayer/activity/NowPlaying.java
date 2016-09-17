package developer.shivam.joyplayer.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import developer.shivam.joyplayer.R;
import developer.shivam.joyplayer.model.Songs;
import developer.shivam.joyplayer.service.PlayerService;
import developer.shivam.joyplayer.util.Collector;

public class NowPlaying extends AppCompatActivity {

    @BindView(R.id.ivAlbumArt)
    ImageView ivAlbumArt;

    @BindView(R.id.seekBar)
    SeekBar seekBar;

    @BindView(R.id.btnPlayPause)
    Button btnPlayPause;

    @BindView(R.id.btnPrevious)
    Button btnPrevious;

    @BindView(R.id.btnNext)
    Button btnNext;

    private PlayerService mPlayerService;
    private Context mContext = NowPlaying.this;
    private boolean mBound = false;
    List<Songs> songsList = new ArrayList<>();
    Handler handler;

    @Override
    protected void onResume() {
        super.onResume();

        Intent playServiceIntent = new Intent(mContext, PlayerService.class);
        bindService(playServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) iBinder;
            mBound = true;
            mPlayerService = binder.getService();
            updateView(mPlayerService);
            Log.d("NowPlaying", "Service bounded with " + songsList.size() + " songs");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("NowPlaying", "Service unbounded");
            mBound = false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        ButterKnife.bind(this);
    }

    private void updateView(PlayerService service) {
        this.mPlayerService = service;

        /**
         * If this client is connected to mPlayerService then
         *  only perform mediaPlayer operation
         */
        if (mBound) {
            setCurrentSong();

            handler = new Handler();

            NowPlaying.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    seekBar.setProgress(mPlayerService.getPlayerPosition());
                    handler.postDelayed(this, 1000);
                }
            });

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (mPlayerService != null && fromUser) {
                        mPlayerService.setPlayerPosition(progress);
                        seekBar.setProgress(mPlayerService.getPlayerPosition());
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
    }

    public void setCurrentSong() {
        Songs track = mPlayerService.getSongsList().get(mPlayerService.getPosition());
        Picasso.with(mContext).load(Collector.getAlbumArtUri(Long.parseLong(track.getAlbumId()))).into(ivAlbumArt);
        seekBar.setMax(Integer.parseInt(track.getDuration()));
    }

    @OnClick(R.id.btnPlayPause)
    public void playPause() {
        mPlayerService.playPause();
    }

    @OnClick(R.id.btnPrevious)
    public void playPreviousSong() {
        mPlayerService.playPrevious();
        setCurrentSong();
    }

    @OnClick(R.id.btnNext)
    public void playNextSong() {
        mPlayerService.playNext();
        setCurrentSong();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBound) {
            unbindService(mConnection);
        }
    }
}
