package developer.shivam.joyplayer.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import developer.shivam.joyplayer.R;
import developer.shivam.joyplayer.adapter.SongsAdapter;
import developer.shivam.joyplayer.model.Songs;
import developer.shivam.joyplayer.util.Collector;
import developer.shivam.joyplayer.util.PermissionManager;
import developer.shivam.joyplayer.listener.onPermissionListener;
import developer.shivam.joyplayer.util.Sorter;

public class MainActivity extends AppCompatActivity implements onPermissionListener {

    private Toolbar toolbar;

    @BindView(R.id.rvSongsList)
    RecyclerView rvSongsList;

    private Context mContext = MainActivity.this;
    private final String TAG = MainActivity.this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mapping();
        setSupportActionBar(toolbar);
        setUpNavigationDrawer();

        PermissionManager.with(this)
                .setPermissionListener(this)
                .getPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
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

    @Override
    public void onPermissionGranted() {
        List<Songs> songsList = Collector.getSongs(mContext);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        rvSongsList.setLayoutManager(layoutManager);
        if (songsList.size() == 0) {
            Toast.makeText(mContext, "No media files", Toast.LENGTH_SHORT).show();
        } else {
            rvSongsList.setAdapter(new SongsAdapter(mContext, songsList));
        }
    }

    @Override
    public void onPermissionDenied() {
        Log.d(TAG, "Permission denied");
    }
}
