package developer.shivam.joyplayer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

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
    private final int READ_EXTERNAL_CARD_REQUEST = 100;
    final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapping();
        setSupportActionBar(toolbar);

        checkPermissionForExternalCard();
    }

    private void mapping() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        rvSongsList = (RecyclerView) findViewById(R.id.rvSongsList);
    }

    public void checkPermissionForExternalCard() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_EXTERNAL_CARD_REQUEST);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_CARD_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
