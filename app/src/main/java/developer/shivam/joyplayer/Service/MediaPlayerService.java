package developer.shivam.joyplayer.Service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

import developer.shivam.joyplayer.Activity.MainActivity;
import developer.shivam.joyplayer.Model.Song;
import developer.shivam.joyplayer.R;

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private boolean shuffle = false;
    private MediaPlayer player;
    private final IBinder musicBind = new MusicBinder();
    private int songPosition = 0;
    private List<Song> songsList;
    public boolean isPlaying = false;
    int songPlayedTime;

    private String songTitle = "";

    @Override
    public void onCreate() {
        super.onCreate();

        initMusicPlayer();
    }

    private void initMusicPlayer() {

        player = new MediaPlayer();
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);

        getSongsList();
    }

    public class MusicBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }


    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        mp.start();

        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.unknown_album)
                .setTicker(getSongTitle())
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(getSongTitle());
        Notification notification = builder.build();
        startForeground(1, notification);
    }

    public void playSong() {

        isPlaying = true;
        player.reset();
        Song playSong = songsList.get(songPosition);
        songTitle = playSong.getName();
        long currSong = playSong.getId();
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
                currSong);
        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }

    public List<Song> getSongsList() {
        return songsList;
    }

    public void setSongsList(List<Song> songsList) {
        this.songsList = songsList;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void playNext() {
        if (songPosition == songsList.size() - 1) {
            songPosition = 0;
        } else {
            songPosition += 1;
        }
        playSong();
    }

    public void playPrevious() {
        if (songPosition == 0) {
            songPosition = songsList.size() - 1;
        } else {
            songPosition -= 1;
        }
        playSong();
    }

    public void pause() {
        isPlaying = false;
        player.pause();
    }

    public void play() {
        isPlaying = true;
        player.start();
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public void setSongPosition(int position) {
        songPosition = position;
    }

    public int getSongPosition() {
        return songPosition;
    }

    public int getSongCurrentPlayingPosition() {
        Log.d("Current Position", player.getCurrentPosition()/1000 + "");
        Log.d("Song Duration", player.getDuration()/1000 + "");
        return player.getCurrentPosition()/1000;
    }

    public int getSongDuration () {
        return player.getDuration()/1000;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }
}
