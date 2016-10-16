package developer.shivam.joyplayer.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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
import developer.shivam.joyplayer.model.Songs;
import developer.shivam.joyplayer.service.PlayerService;
import developer.shivam.joyplayer.util.Collector;
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

    private PlayerService mPlayerService;
    private Context mContext = NowPlaying.this;
    private boolean mBound = false;
    List<Songs> songsList = new ArrayList<>();
    Handler handler;
    boolean isPlaying = true;

    @Override
    protected void onResume() {
        super.onResume();

        Intent playServiceIntent = new Intent(mContext, PlayerService.class);
        bindService(playServiceIntent, mConnection, Context.BIND_AUTO_CREATE);

        nowPlayView.setAmplitude(1);
        if (isPlaying) {
            nowPlayView.start();
        } else {
            nowPlayView.stop();
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) iBinder;
            mBound = true;
            mPlayerService = binder.getService();
            mPlayerService.isRunningInBackground = false;
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
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }

    private void updateView(PlayerService service) {
        this.mPlayerService = service;

        /**
         * If this client is connected to mPlayerService then
         *  only perform mediaPlayer operation
         */
        if (mPlayerService != null) {
            setCurrentSong();
            mPlayerService.mPlayer.setOnCompletionListener(this);

            handler = new Handler();

            NowPlaying.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    seekBar.setProgress(mPlayerService.getPlayerPosition());
                    tvCurrentDuration.setText(HelperMethods.getSongDuration(mPlayerService.getPlayerPosition()));
                    handler.postDelayed(this, 100);
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
        tvTotalDuration.setText(HelperMethods.getSongDuration(Integer.parseInt(track.getDuration())));
        Uri albumArtUri = Collector.getAlbumArtUri(Long.parseLong(track.getAlbumId()));
        Picasso.with(mContext).load(albumArtUri).placeholder(R.drawable.default_album_art).error(R.drawable.default_album_art).into(ivAlbumArt);
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
        mPlayerService.playPause();
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
        mPlayerService.playPrevious();
        setCurrentSong();
    }

    @OnClick(R.id.ivNext)
    public void playNextSong() {
        mPlayerService.playNext();
        setCurrentSong();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBound) {
            mPlayerService.isRunningInBackground = true;
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
            case android.R.id.home : onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
