package developer.shivam.joyplayer.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import developer.shivam.joyplayer.R;
import developer.shivam.joyplayer.adapter.SongsAdapter;
import developer.shivam.joyplayer.listener.OnClickListener;
import developer.shivam.joyplayer.model.Songs;
import developer.shivam.joyplayer.service.PlayerService;
import developer.shivam.joyplayer.util.Collector;
import developer.shivam.joyplayer.util.PermissionManager;
import developer.shivam.joyplayer.listener.onPermissionListener;
import developer.shivam.joyplayer.util.Sorter;

public class MainActivity extends AppCompatActivity implements onPermissionListener, OnClickListener {

    private Toolbar toolbar;

    @BindView(R.id.rvSongsList)
    RecyclerView rvSongsList;

    /**
     * mBound boolean is used to maintain
     *  binding/unbinding of PlayerService
     */
    private boolean mBound = false;

    /**
     * PlayerService object helps to bind the
     *  client with the service which act like
     *  a server
     */
    private PlayerService mPlayerService;

    private Context mContext = MainActivity.this;
    private final String TAG = MainActivity.this.getClass().getSimpleName();
    private List<Songs> songsList = new ArrayList<>();

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            /**
             * We now bind client (activity) with service
             */
            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) service;
            mPlayerService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Intent playerServiceIntent = new Intent(mContext, PlayerService.class);
        bindService(playerServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mapping();
        setSupportActionBar(toolbar);
        setUpNavigationDrawer();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionManager.with(this)
                    .setPermissionListener(this)
                    .getPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            songsList = Collector.getSongs(mContext);
            LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
            rvSongsList.setLayoutManager(layoutManager);
            if (songsList.size() == 0) {
                Toast.makeText(mContext, "No media files", Toast.LENGTH_SHORT).show();
            } else {
                SongsAdapter adapter = new SongsAdapter(mContext, songsList);
                adapter.setOnClickListener(this);
                rvSongsList.setAdapter(adapter);
            }
        }
    }

    private void mapping() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    private void setUpNavigationDrawer() {
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView drawerNavigationView = (NavigationView) findViewById(R.id.mainActivityNavigationView);
        final ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                toolbar,
                R.string.open,
                R.string.close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        drawerNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nowPlaying : Intent nowPlayingIntent = new Intent(mContext, NowPlaying.class);
                        startActivity(nowPlayingIntent);
                }
                return false;
            }
        });
        mDrawerLayout.setDrawerListener(drawerToggle);
        mDrawerLayout.post(new Runnable() {

            @Override
            public void run() {
                drawerToggle.syncState();
            }
        });
    }

    @Override
    public void onPermissionGranted() {
        songsList = Collector.getSongs(mContext);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        rvSongsList.setLayoutManager(layoutManager);
        if (songsList.size() == 0) {
            Toast.makeText(mContext, "No media files", Toast.LENGTH_SHORT).show();
        } else {
            SongsAdapter adapter = new SongsAdapter(mContext, songsList);
            adapter.setOnClickListener(this);
            rvSongsList.setAdapter(adapter);
        }
    }

    @Override
    public void onPermissionDenied() {
        Log.d(TAG, "Permission denied");
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*if (mBound) {
            unbindService(mConnection);
        }*/
    }

    /**
     * A custom onClick() function is used to get adapter position
     *  to play the clicked song
     */
    @Override
    public void onClick(int position) {
        System.out.println(songsList.get(position).getSongUri());
        mPlayerService.setPosition(position);
        mPlayerService.setSongUri(songsList.get(position).getSongUri());
        mPlayerService.setSongsList(songsList);
        mPlayerService.playSong();
    }
}
