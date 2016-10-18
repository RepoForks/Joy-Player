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

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import developer.shivam.joyplayer.R;
import developer.shivam.joyplayer.activity.NowPlaying;
import developer.shivam.joyplayer.adapter.SongsAdapter;
import developer.shivam.joyplayer.listener.OnClickListener;
import developer.shivam.joyplayer.listener.onPermissionListener;
import developer.shivam.joyplayer.model.Songs;
import developer.shivam.joyplayer.service.PlayerService;
import developer.shivam.joyplayer.util.Collector;
import developer.shivam.joyplayer.util.PermissionManager;

public class TracksFragment extends Fragment implements onPermissionListener, OnClickListener {

    final String TAG = "TRACKS_FRAGMENT";
    Context mContext;
    List<Songs> songsList = new ArrayList<>();
    RecyclerView rvSongsList;

    private boolean mBound = false;

    private PlayerService mPlayerService;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            /**
             * We now bind client (fragment) with service
             */
            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) service;
            mPlayerService = binder.getService();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionManager.with(getActivity())
                    .setPermissionListener(this)
                    .getPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

        } else {
            songsList = Collector.getSongs(mContext);
            setUpRecyclerView(songsList);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent playerServiceIntent = new Intent(mContext, PlayerService.class);
        if (!mBound) {
            mContext.bindService(playerServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void setUpRecyclerView(List<Songs> list) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        rvSongsList.setLayoutManager(layoutManager);
        if (list.size() == 0) {
            Toast.makeText(mContext, "No media files", Toast.LENGTH_SHORT).show();
        } else {
            SongsAdapter adapter = new SongsAdapter(mContext, list);
            adapter.setOnClickListener(this);
            rvSongsList.setAdapter(adapter);
        }
    }

    public void loadHorizontalRecentlyAddedItems(LinearLayout linearLayoutRecentlyAddedItem) {
        for (int i = 0; i < 5; i++) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.view_recently_song_card, null);

            View spacing = new View(mContext);
            spacing.setLayoutParams(new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.horizontal_card_margin), LinearLayout.LayoutParams.MATCH_PARENT));

            ((TextView) view.findViewById(R.id.tvSongName)).setText(songsList.get(i).getName());
            Picasso.with(mContext).load(Collector.getAlbumArtUri(Long.parseLong(songsList.get(i).getAlbumId()))).placeholder(R.drawable.default_album_art).into((ImageView) view.findViewById(R.id.ivAlbumArt));

            linearLayoutRecentlyAddedItem.addView(spacing);
            linearLayoutRecentlyAddedItem.addView(view);
        }

        View spacing = new View(mContext);
        spacing.setLayoutParams(new LinearLayout.LayoutParams(32, LinearLayout.LayoutParams.MATCH_PARENT));
        linearLayoutRecentlyAddedItem.addView(spacing);
    }

    @Override
    public void onPermissionGranted() {
        songsList = Collector.getSongs(mContext);
        setUpRecyclerView(songsList);
    }

    @Override
    public void onPermissionDenied() {

    }

    @Override
    public void onClick(View view, int position) {

        mPlayerService.setPosition(position);
        mPlayerService.setSongUri(songsList.get(position).getSongUri());
        mPlayerService.setSongsList(songsList);
        mPlayerService.playSong();

        Intent nowPlayingIntent = new Intent(mContext, NowPlaying.class);
        startActivity(nowPlayingIntent);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBound) {
            mContext.unbindService(mConnection);
        }
    }
}
