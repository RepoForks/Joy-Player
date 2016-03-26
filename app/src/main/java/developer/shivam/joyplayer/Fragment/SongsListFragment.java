package developer.shivam.joyplayer.Fragment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import developer.shivam.joyplayer.Activity.MainActivity;
import developer.shivam.joyplayer.Model.Song;
import developer.shivam.joyplayer.R;
import developer.shivam.joyplayer.Service.MediaPlayerService;
import developer.shivam.joyplayer.Utility.GetSongs;

public class SongsListFragment extends Fragment {


    List<Song> songsList;
    ListView songsListView;
    Intent playIntent;
    MediaPlayerService mediaPlayerService;
    public boolean musicBound = false;
    Button previous, playPause, next;
    AppCompatSeekBar seekBar;
    Timer timer;
    TimerTask task;
    Handler handler;

    public SongsListFragment() {
        // Required empty public constructor
    }

    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.MusicBinder binder = (MediaPlayerService.MusicBinder)service;
            mediaPlayerService = binder.getService();
            mediaPlayerService.setSongsList(songsList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(getActivity(), MediaPlayerService.class);
            getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        songsList = new ArrayList<>();
        songsList = new GetSongs(getActivity()).inList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_songs_list, container, false);

        previous = (Button) view.findViewById(R.id.previous);
        playPause = (Button) view.findViewById(R.id.play_pause);
        next = (Button) view.findViewById(R.id.next);

        seekBar = (AppCompatSeekBar) view.findViewById(R.id.seekBar);

        timer = new Timer();
        task = new TimerTask() {

            @Override
            public void run() {
                seekBar.setProgress(mediaPlayerService.getSongCurrentPlayingPosition());
            }
        };

        songsListView = (ListView) view.findViewById(R.id.listView_songs_list);
        if (songsList == null) {
            Toast.makeText(getActivity(), "No Songs", Toast.LENGTH_SHORT).show();
        } else {

            /**
             * To sort songs list in alphabetic order.
             */
            Collections.sort(songsList, new Comparator<Song>() {
                @Override
                public int compare(Song lhs, Song rhs) {
                    return lhs.getName().compareTo(rhs.getName());
                }
            });

            songsListView.setAdapter(new SongsCustomAdapter());
            songsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    playPause.setText("Pause");
                    mediaPlayerService.setSongPosition(position);
                    mediaPlayerService.playSong();

                    seekBar.setMax(mediaPlayerService.getSongDuration());
                    seekBar.setKeyProgressIncrement(1);
                }
            });

            /*handler = new Handler();
            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mediaPlayerService != null) {
                        int mCurrentPosition = mediaPlayerService.getSongCurrentPlayingPosition();
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });*/



        }

        playPause.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mediaPlayerService.isPlaying){
                    mediaPlayerService.pause();
                    playPause.setText("Play");
                } else {
                    mediaPlayerService.play();
                    playPause.setText("Pause");
                }
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mediaPlayerService.playPrevious();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mediaPlayerService.playNext();
            }
        });

        return view;
    }

    static class Holder {

        TextView songName;
        TextView songArtist;
        ImageView songAlbumArt;
    }

    public class SongsCustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return songsList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Holder holder;

            if (convertView == null) {

                holder = new Holder();
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.song_list_item, null);

                holder.songName = (TextView) convertView.findViewById(R.id.textView_song_name);
                holder.songArtist = (TextView) convertView.findViewById(R.id.textView_song_artist);
                holder.songAlbumArt = (ImageView) convertView.findViewById(R.id.imageView_song_image);

                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }

            holder.songName.setText(songsList.get(position).getName());
            holder.songArtist.setText(songsList.get(position).getSinger());
            Picasso.with(getActivity())
                    .load(new GetSongs(getActivity())
                            .getAlbumArtUri(songsList.get(position).getAlbumId()))
                    .resize(100, 100)
                    .placeholder(R.drawable.unknown_album)
                    .into(holder.songAlbumArt);

            return convertView;

            /*View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.song_list_item, null);
            TextView songName = (TextView) view.findViewById(R.id.textView_song_name);
            songName.setText(songsList.get(position).getName());
            ImageView songImageView = (ImageView) view.findViewById(R.id.imageView_song_image);

            if(songImageView.getDrawable() == null) {
                Picasso.with(MainActivity.this)
                        .load(new GetSongs(MainActivity.this)
                                .getAlbumArtUri(songsList.get(position).getAlbumId()))
                        .resize(100, 100)
                        .into(songImageView);
            }

            return view;*/
        }
    }
}
