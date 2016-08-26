package developer.shivam.joyplayer.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import developer.shivam.joyplayer.R;
import developer.shivam.joyplayer.model.Songs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Collector {

    Context context;
    static Collector collector;
    static ContentResolver resolver;

    public static Collector with(Context context) {
        collector = collector.Builder(context);
        return collector;
    }

    public Collector Builder(Context context) {
        collector.context = context;
        return collector;
    }

    public List<Songs> getSongs(String source) {

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        if(source == "internal") {
            uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        }

        List<Songs> songsList = new ArrayList<>();


        resolver = context.getContentResolver();

        final String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION};

        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1";

        Cursor cursor = resolver.query(uri, projection, where, null, null);
        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);

            Songs songs = new Songs();

            songs.setId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
            if (cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)) == null) {
                songs.setName("unknown");
            } else {
                songs.setName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            }
            songs.setName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            songs.setAlbumId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
            int index = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            if (index == -1){
                //Album name not exist
            } else {
                songs.setAlbumName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
            }

            songs.setSingerName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));

            songsList.add(songs);
        }

        return songsList;
    }

    public Uri getAlbumArtUri(long albumId) {

        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);

        return albumArtUri;
    }

    public Uri getSongsUri(int songId) {

        Uri sArtworkUri = Uri.parse("content://media/external/audio/media");
        Uri songUri = ContentUris.withAppendedId(sArtworkUri, songId);

        return songUri;
    }

    public Bitmap getAlbumArtBitmap(long albumId) {
        Bitmap bitmap = null;

        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);

        try {
            bitmap = MediaStore.Images.Media.getBitmap(resolver, albumArtUri);
            bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true);
        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_album_art);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }
}