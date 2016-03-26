package developer.shivam.joyplayer.Utility;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import developer.shivam.joyplayer.Model.Song;
import developer.shivam.joyplayer.R;

public class GetSongs {

    static Context context;
    static Cursor cursor;
    static List<Song> songList;
    ContentResolver resolver;

    public GetSongs(Context context) {
        this.context = context;

        songList = new ArrayList<>();
        Uri externalContentUri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        resolver = context.getContentResolver();

        final String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION};

        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1";
        cursor = resolver.query(externalContentUri, projection, where, null, null);
    }

    public static List<Song> inList() {

        cursor.moveToFirst();
        if (cursor.getCount() != 0) {
            for (int i = 0; i < cursor.getCount(); i++) {

                cursor.moveToPosition(i);
                Song songModel = new Song();
                songModel.setDetails(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));

                songList.add(songModel);
            }
            return songList;
        } else {
            return null;
        }
    }

    public Uri getAlbumArtUri(long albumId) {

        Uri sArtworkUri = Uri.parse("content://media/internal/audio/albumart");
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
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.unknown_album);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }
}
