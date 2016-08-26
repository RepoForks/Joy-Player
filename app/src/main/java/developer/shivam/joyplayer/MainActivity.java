package developer.shivam.joyplayer;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import developer.shivam.joyplayer.adapter.SongsAdapter;
import developer.shivam.joyplayer.model.Songs;
import developer.shivam.joyplayer.util.Collector;

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
        songsList = Collector.with(mContext).getSongs("external");
        SongsAdapter songsAdapter = new SongsAdapter(mContext, songsList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        rvSongsList.setLayoutManager(linearLayoutManager);
        rvSongsList.setAdapter(songsAdapter);
    }

    private void mapping() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        rvSongsList = (RecyclerView) findViewById(R.id.rvSongsList);
    }
}
