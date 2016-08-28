package developer.shivam.joyplayer.activity;

import android.Manifest;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import developer.shivam.joyplayer.R;
import developer.shivam.joyplayer.adapter.SongsAdapter;
import developer.shivam.joyplayer.model.Songs;
import developer.shivam.joyplayer.util.Collector;
import developer.shivam.joyplayer.util.PermissionManager;
import developer.shivam.joyplayer.listener.onPermissionListener;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvSongsList;
    private List<Songs> songsList = new ArrayList<>();
    private Context mContext = MainActivity.this;
    final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapping();
        setSupportActionBar(toolbar);

        PermissionManager.with(this).setPermissionListener(new onPermissionListener() {

            @Override
            public void onPermissionGranted() {
                songsList = Collector.getSongs(mContext);
                if (songsList.size() == 0) {
                    Toast.makeText(mContext, "Sorry! No media content found", Toast.LENGTH_SHORT).show();
                } else {
                    SongsAdapter songsAdapter = new SongsAdapter(mContext, songsList);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
                    rvSongsList.setLayoutManager(linearLayoutManager);
                    rvSongsList.setAdapter(songsAdapter);
                }
            }

            @Override
            public void onPermissionDenied() {
                Toast.makeText(mContext, "Joy Player needs permission to get songs for you", Toast.LENGTH_LONG).show();
            }

        }).getPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    private void mapping() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        rvSongsList = (RecyclerView) findViewById(R.id.rvSongsList);
    }
}
