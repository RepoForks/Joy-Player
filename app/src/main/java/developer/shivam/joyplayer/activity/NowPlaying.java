package developer.shivam.joyplayer.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.FileNotFoundException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;
import developer.shivam.joyplayer.R;
import developer.shivam.joyplayer.listener.PlaybackListener;
import developer.shivam.joyplayer.pojo.Songs;
import developer.shivam.joyplayer.service.PlaybackService;
import developer.shivam.joyplayer.util.HelperMethods;
import developer.shivam.joyplayer.util.Retriever;
import developer.shivam.joyplayer.view.PlayPauseView;

public class NowPlaying extends AppCompatActivity implements PlaybackListener, View.OnClickListener {

    Context mContext = NowPlaying.this;

    CircleImageView ivAlbumArt;

    SeekBar seekBar;

    PlayPauseView btnPlayPause;

    ImageView btnPrevious;
    ImageView btnNext;

    TextView tvCurrentDuration;
    TextView tvTotalDuration;
    TextView tvSongName;
    TextView tvSongArtist;

    Toolbar toolbar;

    private PlaybackService mPlaybackService;

    boolean mBound = false;
    boolean isPlaying = true;

    Handler handler;
    SeekBarRunnable seekBarRunnable;

    @Override
    public void onMusicPlay() {
        btnPlayPause.toggle();
    }

    @Override
    public void onMusicPause() {
        btnPlayPause.toggle();
    }

    @Override
    public void onCompletion() {
        playNextSong();
    }

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

        handler = new Handler();
        seekBarRunnable = new SeekBarRunnable();
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlaybackService.PlayerBinder binder = (PlaybackService.PlayerBinder) iBinder;
            mBound = true;
            mPlaybackService = binder.getService();
            updateView();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ivAlbumArt = (CircleImageView) findViewById(R.id.ivAlbumArt);
        seekBar = (SeekBar) findViewById(R.id.seekBar);

        tvCurrentDuration = (TextView) findViewById(R.id.tvCurrentDuration);
        tvTotalDuration = (TextView) findViewById(R.id.tvTotalDuration);

        btnPrevious = (ImageView) findViewById(R.id.ivPrevious);
        btnPrevious.setOnClickListener(this);
        btnNext = (ImageView) findViewById(R.id.ivNext);
        btnNext.setOnClickListener(this);

        btnPlayPause = (PlayPauseView) findViewById(R.id.btnPlayPause);
        btnPlayPause.setOnClickListener(this);

        tvSongName = (TextView) findViewById(R.id.tvSongName);
        tvSongArtist = (TextView) findViewById(R.id.tvSongArtist);

    }

    private void updateView() {
        /**
         * If this client is connected to mPlaybackService then
         *  only perform mediaPlayer operation
         */
        if (mPlaybackService != null) {
            handler.post(seekBarRunnable);
            setCurrentSong();
            mPlaybackService.setPlaybackListener(this);
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
                    } else if (palette.getDarkVibrantSwatch() != null) {
                        int value = 0x000000;
                        String color = "#" + String.valueOf(Integer.toHexString(palette.getDarkVibrantColor(value)));
                    }
                }
            });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void playPause() {
        isPlaying = !isPlaying;
        mPlaybackService.playPause();
    }

    public void playPreviousSong() {
        mPlaybackService.playPrevious();
        setCurrentSong();
    }

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.ivNext : playNextSong(); break;
            case R.id.ivPrevious : playPreviousSong(); break;
            case R.id.btnPlayPause: playPause(); break;
        }
    }
}
