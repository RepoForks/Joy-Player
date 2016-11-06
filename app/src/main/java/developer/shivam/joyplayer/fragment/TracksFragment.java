package developer.shivam.joyplayer.fragment;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import developer.shivam.joyplayer.R;
import developer.shivam.joyplayer.activity.NowPlaying;
import developer.shivam.joyplayer.adapter.SongsAdapter;
import developer.shivam.joyplayer.listener.OnClickListener;
import developer.shivam.joyplayer.listener.onPermissionListener;
import developer.shivam.joyplayer.pojo.Songs;
import developer.shivam.joyplayer.service.PlaybackService;
import developer.shivam.joyplayer.util.Global;
import developer.shivam.joyplayer.util.Retriever;
import developer.shivam.joyplayer.util.PermissionManager;
import developer.shivam.joyplayer.util.State;
import developer.shivam.joyplayer.view.SimpleDividerItemDecoration;

public class TracksFragment extends Fragment implements onPermissionListener, OnClickListener {

    /**
     * This is the tag used to log events.
     */
    final String TAG = "TRACKS_FRAGMENT";

    Context mContext;

    List<Songs> mSongsList = new ArrayList<>();

    RecyclerView rvSongsList;

    /**
     * Once list retrieved from storage will stored in
     *  this global variable.
     */
    Global globalVariable;

    private boolean mBound = false;

    private PlaybackService mPlaybackService;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            /**
             * We now bind client (fragment) with service
             */
            PlaybackService.PlayerBinder binder = (PlaybackService.PlayerBinder) service;
            mPlaybackService = binder.getService();
            mPlaybackService.setSongsList(mSongsList);
            mBound = true;
            Log.d(TAG, "Connection made to Player Service");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "Connection removed");
            mBound = false;
        }
    };

    public TracksFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracks, container, false);
        rvSongsList = (RecyclerView) view.findViewById(R.id.rvSongsList);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mContext = getActivity();
        globalVariable = (Global) mContext.getApplicationContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionManager.with(getActivity())
                    .setPermissionListener(this)
                    .getPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            if (globalVariable.getSongsList().size() != 0) {
                mSongsList = Retriever.getSongs(mContext);
                globalVariable.setSongsList(mSongsList);
                setUpRecyclerView(mSongsList);
            } else {
                mSongsList = globalVariable.getSongsList();
                setUpRecyclerView(mSongsList);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent playerServiceIntent = new Intent(mContext, PlaybackService.class);
        mContext.startService(playerServiceIntent);
        if (!mBound) {
            mContext.bindService(playerServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void setUpRecyclerView(List<Songs> list) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        rvSongsList.setLayoutManager(layoutManager);
        rvSongsList.addItemDecoration(new SimpleDividerItemDecoration(mContext));
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
        mSongsList = Retriever.getSongs(mContext);
        globalVariable.setSongsList(mSongsList);
        setUpRecyclerView(mSongsList);
    }

    @Override
    public void onPermissionDenied() {

    }

    @Override
    public void onClick(View view, int position) {
        mPlaybackService.setPosition(position);
        mPlaybackService.setSongsList(mSongsList);
        mPlaybackService.playSong(State.PLAY);

        Intent nowPlayingIntent = new Intent(mContext, NowPlaying.class);
        startActivity(nowPlayingIntent);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBound) {
            mBound = false;
            mContext.unbindService(mConnection);
        }
    }
}
