package developer.shivam.joyplayer.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;

/**
 * This is MusicPlayerService to play media content
 */
public class MusicPlayerService extends Service implements MediaPlayer.OnCompletionListener {

    MediaPlayer mediaPlayer;
    Uri songUri;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(MusicPlayerService.this, getSong());
        mediaPlayer.start();
    }

    public void setSong(Uri songUri) {
        this.songUri = songUri;
    }

    public Uri getSong() {
        return songUri;
    }

    public void stop() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopSelf();
    }
}
