package developer.shivam.joyplayer.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
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
import butterknife.OnClick;
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

    @BindView(R.id.fab)
    FloatingActionButton floatingActionButton;

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
    protected void onResume() {
        super.onResume();
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
        rvSongsList.setHasFixedSize(true);
        rvSongsList.setNestedScrollingEnabled(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionManager.with(this)
                    .setPermissionListener(this)
                    .getPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

            requestSystemAlertPermission(this, null, 100);
        } else {
            songsList = Collector.getSongs(mContext);
            setUpRecyclerView(songsList);
        }
    }

    public static void requestSystemAlertPermission(Activity context, Fragment fragment, int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;
        final String packageName = context == null ? fragment.getActivity().getPackageName() : context.getPackageName();
        final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName));
        if (fragment != null)
            fragment.startActivityForResult(intent, requestCode);
        else
            context.startActivityForResult(intent, requestCode);
    }

    @OnClick(R.id.fab)
    public void openNowPlaying() {
        Intent nowPlayingActivity = new Intent(this, NowPlaying.class);
        startActivity(nowPlayingActivity);
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

        mDrawerLayout.setDrawerListener(drawerToggle);
        mDrawerLayout.post(new Runnable() {

            @Override
            public void run() {
                drawerToggle.syncState();
            }
        });
    }

    public void setUpRecyclerView(List<Songs> list) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        rvSongsList.setLayoutManager(layoutManager);
        if (list.size() == 0) {
            Toast.makeText(mContext, "No media files", Toast.LENGTH_SHORT).show();
        } else {
            SongsAdapter adapter = new SongsAdapter(mContext, list);
            adapter.setOnClickListener(this);
            rvSongsList.setAdapter(adapter);
        }
    }

    @Override
    public void onPermissionGranted() {
        songsList = Collector.getSongs(mContext);
        setUpRecyclerView(songsList);
    }

    @Override
    public void onPermissionDenied() {
        Log.d(TAG, "Permission denied");
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*if (mBound) {
            unbindService(mConnection);
        }*/
    }

    /**
     * A custom onClick() function is used to get adapter position
     *  to play the clicked song
     */
    @Override
    public void onClick(View view, int position) {

        /**
         * By clicking the item the song for that position will be
         *  played and now playing activity will be opened.
         */

        mPlayerService.setPosition(position);
        mPlayerService.setSongUri(songsList.get(position).getSongUri());
        mPlayerService.setSongsList(songsList);
        mPlayerService.playSong();

        Intent nowPlayingIntent = new Intent(this, NowPlaying.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                    view,
                    getResources().getString(R.string.same_album_art));
            startActivity(nowPlayingIntent, optionsCompat.toBundle());
        } else {
            startActivity(nowPlayingIntent);
        }
    }
}
