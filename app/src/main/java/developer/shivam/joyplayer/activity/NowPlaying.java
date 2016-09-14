package developer.shivam.joyplayer.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import developer.shivam.joyplayer.R;
import developer.shivam.joyplayer.model.Songs;
import developer.shivam.joyplayer.service.PlayerService;
import developer.shivam.joyplayer.util.Collector;

public class NowPlaying extends AppCompatActivity {

    @BindView(R.id.ivAlbumArt)
    ImageView ivAlbumArt;

    @BindView(R.id.seekBar)
    SeekBar seekBar;

    private PlayerService playerService;
    private boolean mBound = false;
    private Context mContext = NowPlaying.this;

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        /**
         * Bounding NowPlaying activity with PlayerService
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) service;
            playerService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Intent playerServiceIntent = new Intent(mContext, PlayerService.class);
        bindService(playerServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        ButterKnife.bind(this);

        List<Songs> songsList = playerService.getSongsList();
        if (songsList.size() == 0) {
            songsList = Collector.getSongs(mContext);
        } else {
            seekBar.setMax(Integer.parseInt(songsList.get(playerService.getPosition()).getDuration()));
        }

        Picasso.with(mContext).load(Collector.getAlbumArtUri(Long.parseLong(songsList.get(playerService.getPosition()).getAlbumId()))).into(ivAlbumArt);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                playerService.setPlayerPosition(progress);
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
