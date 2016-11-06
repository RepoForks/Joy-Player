package developer.shivam.joyplayer.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import developer.shivam.joyplayer.R;
import developer.shivam.joyplayer.pojo.Songs;
import developer.shivam.joyplayer.service.PlaybackService;
import developer.shivam.joyplayer.util.Retriever;
import developer.shivam.joyplayer.util.HelperMethods;
import developer.shivam.joyplayer.view.PlayPauseView;
import developer.shivam.library.WaveView;

public class NowPlaying extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    @BindView(R.id.ivAlbumArt)
    CircleImageView ivAlbumArt;

    @BindView(R.id.seekBar)
    SeekBar seekBar;

    @BindView(R.id.btnPlayPause)
    PlayPauseView btnPlayPause;

    @BindView(R.id.ivPrevious)
    ImageView btnPrevious;

    @BindView(R.id.ivNext)
    ImageView btnNext;

    @BindView(R.id.tvCurrentDuration)
    TextView tvCurrentDuration;

    @BindView(R.id.tvTotalDuration)
    TextView tvTotalDuration;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.waveView)
    WaveView nowPlayView;

    @BindView(R.id.tvSongName)
    TextView tvSongName;

    @BindView(R.id.tvSongArtist)
    TextView tvSongArtist;

    private PlaybackService mPlaybackService;
    private Context mContext = NowPlaying.this;
    private boolean mBound = false;
    List<Songs> songsList = new ArrayList<>();
    Handler handler;
    boolean isPlaying = true;
    SeekBarRunnable seekBarRunnable;

    public class SeekBarRunnable implements Runnable {

        @Override
        public void run() {
            seekBar.setProgress(mPlaybackService.getPlayerPosition());
            tvCurrentDuration.setText(HelperMethods.getSongDuration(mPlaybackService.getPlayerPosition()));
            handler.postDelayed(this, 100);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent playServiceIntent = new Intent(mContext, PlaybackService.class);
        bindService(playServiceIntent, mConnection, Context.BIND_AUTO_CREATE);

        nowPlayView.setAmplitude(1);
        if (isPlaying) {
            nowPlayView.start();
        } else {
            nowPlayView.stop();
        }

        handler = new Handler();
        seekBarRunnable = new SeekBarRunnable();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlaybackService.PlayerBinder binder = (PlaybackService.PlayerBinder) iBinder;
            mBound = true;
            mPlaybackService = binder.getService();
            updateView(mPlaybackService);
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
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void updateView(PlaybackService service) {
        this.mPlaybackService = service;

        /**
         * If this client is connected to mPlaybackService then
         *  only perform mediaPlayer operation
         */
        if (mPlaybackService != null) {
            handler.post(seekBarRunnable);
            setCurrentSong();
            mPlaybackService.mPlayer.setOnCompletionListener(this);

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (mPlaybackService != null && fromUser) {
                        mPlaybackService.setPlayerPosition(progress);
                        seekBar.setProgress(mPlaybackService.getPlayerPosition());
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
        Songs track = mPlaybackService.getSongsList().get(mPlaybackService.getPosition());
        tvTotalDuration.setText(HelperMethods.getSongDuration(Integer.parseInt(track.getDuration())));
        Uri albumArtUri = Retriever.getAlbumArtUri(Long.parseLong(track.getAlbumId()));
        Glide.with(mContext)
                .load(albumArtUri)
                .placeholder(R.drawable.default_album_art)
                .into(ivAlbumArt);
        seekBar.setMax(Integer.parseInt(track.getDuration()));
        ivAlbumArt.setAlpha(0.9f);
        tvSongName.setText(track.getName());
        tvSongArtist.setText(track.getSingerName());
        setCurrentAlbumArt(albumArtUri);
    }

    private void setCurrentAlbumArt(Uri albumArtUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(albumArtUri);
            Bitmap albumBitmap = BitmapFactory.decodeStream(inputStream);

            Palette.from(albumBitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    if (palette.getVibrantSwatch() != null) {
                        int value = 0x000000;
                        String color = "#" + String.valueOf(Integer.toHexString(palette.getVibrantColor(value)));
                        Log.d("Vibrant", color);
                        nowPlayView.setColor(color);
                    }

                    if (palette.getDarkVibrantSwatch() != null) {
                        int value = 0x000000;
                        String color = "#" + String.valueOf(Integer.toHexString(palette.getDarkVibrantColor(value)));
                        Log.d("Vibrant", color);
                        nowPlayView.setColorOne(color);
                    }
                }
            });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    HashMap<String, Palette.Swatch> processPalette(Palette p) {
        HashMap<String, Palette.Swatch> map = new HashMap<>();

        if (p.getVibrantSwatch() != null)
            map.put("Vibrant", p.getVibrantSwatch());
        if (p.getDarkVibrantSwatch() != null)
            map.put("DarkVibrant", p.getDarkVibrantSwatch());
        if (p.getLightVibrantSwatch() != null)
            map.put("LightVibrant", p.getLightVibrantSwatch());

        if (p.getMutedSwatch() != null)
            map.put("Muted", p.getMutedSwatch());
        if (p.getDarkMutedSwatch() != null)
            map.put("DarkMuted", p.getDarkMutedSwatch());
        if (p.getLightMutedSwatch() != null)
            map.put("LightMuted", p.getLightMutedSwatch());

        return map;
    }

    @OnClick(R.id.btnPlayPause)
    public void playPause() {
        isPlaying = !isPlaying;
        mPlaybackService.playPause();
        if (isPlaying) {
            btnPlayPause.toggle();
            nowPlayView.start();

        } else {
            btnPlayPause.toggle();
            nowPlayView.stop();
        }
    }

    @OnClick(R.id.ivPrevious)
    public void playPreviousSong() {
        mPlaybackService.playPrevious();
        setCurrentSong();
    }

    @OnClick(R.id.ivNext)
    public void playNextSong() {
        mPlaybackService.playNext();
        setCurrentSong();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(seekBarRunnable);
        if (mBound) {
            unbindService(mConnection);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playNextSong();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
