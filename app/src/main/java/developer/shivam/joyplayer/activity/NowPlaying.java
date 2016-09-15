package developer.shivam.joyplayer.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
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
    private Context mContext = NowPlaying.this;
    private boolean mBound = false;
    List<Songs> songsList = new ArrayList<>();

    @Override
    protected void onResume() {
        super.onResume();

        Intent playServiceIntent = new Intent(mContext, PlayerService.class);
        bindService(playServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) iBinder;
            playerService = binder.getService();
            Log.d("NowPlaying", "Service bounded");
            mBound = true;
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

    @Override
    protected void onPause() {
        super.onPause();
        if (mBound) {
            unbindService(mConnection);
        }
    }
}
