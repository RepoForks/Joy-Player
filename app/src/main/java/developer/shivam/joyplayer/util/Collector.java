package developer.shivam.joyplayer.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import developer.shivam.joyplayer.R;
import developer.shivam.joyplayer.model.Songs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Collector {

    private static ContentResolver resolver;

    public static List<Songs> getSongs(Context context) {

        List<Songs> songsList = new ArrayList<>();

        Uri externalContextUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri internalContextUri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;

        resolver = context.getContentResolver();

        final String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION};

        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1";

        Cursor externalContentCursor = resolver.query(externalContextUri, projection, where, null, null);
        Cursor internalContentCursor = resolver.query(internalContextUri, projection, where, null, null);

        if (internalContentCursor != null) {
            for (int i = 0; i < internalContentCursor.getCount(); i++) {
                internalContentCursor.moveToPosition(i);

                Songs songs = new Songs();

                songs.setId(internalContentCursor.getString(internalContentCursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                if (internalContentCursor.getString(internalContentCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)) == null) {
                    songs.setName("unknown");
                } else {
                    songs.setName(internalContentCursor.getString(internalContentCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                }
                songs.setName(internalContentCursor.getString(internalContentCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                songs.setAlbumId(internalContentCursor.getString(internalContentCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                int index = internalContentCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                if (index != -1) {
                    songs.setAlbumName(internalContentCursor.getString(internalContentCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                }

                songs.setSongUri(ContentUris.withAppendedId(
                        internalContextUri,
                        internalContentCursor.getInt(internalContentCursor.getColumnIndex(MediaStore.Audio.Media._ID))));

                System.out.println(songs.getSongUri());
                songs.setSingerName(internalContentCursor.getString(internalContentCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                songs.setDuration(internalContentCursor.getString(internalContentCursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));

                if (Integer.parseInt(songs.getDuration()) > 2500 && !songs.getSingerName().toLowerCase().contains("samsung")) {
                    songsList.add(songs);
                }
            }
        }

        if (externalContentCursor != null) {
            for (int i = 0; i < externalContentCursor.getCount(); i++) {
                externalContentCursor.moveToPosition(i);

                Songs songs = new Songs();

                songs.setId(externalContentCursor.getString(externalContentCursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                if (externalContentCursor.getString(externalContentCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)) == null) {
                    songs.setName("unknown");
                } else {
                    songs.setName(externalContentCursor.getString(externalContentCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                }
                songs.setName(externalContentCursor.getString(externalContentCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                songs.setAlbumId(externalContentCursor.getString(externalContentCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                int index = externalContentCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                if (index != -1) {
                    songs.setAlbumName(externalContentCursor.getString(externalContentCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                }

                songs.setSongUri(ContentUris.withAppendedId(
                        externalContextUri,
                        externalContentCursor.getInt(externalContentCursor.getColumnIndex(MediaStore.Audio.Media._ID))));

                songs.setSingerName(externalContentCursor.getString(externalContentCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                songs.setDuration(externalContentCursor.getString(externalContentCursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
                if (Integer.parseInt(songs.getDuration()) > 2500 && !songs.getSingerName().toLowerCase().contains("samsung")) {
                    songsList.add(songs);
                }
            }
        }

        if (externalContentCursor != null) {
            externalContentCursor.close();
        }
        if (internalContentCursor != null) {
            internalContentCursor.close();
        }

        return songsList;
    }

    public static Uri getAlbumArtUri(long albumId) {
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        return ContentUris.withAppendedId(sArtworkUri, albumId);
    }

    public Uri getSongsUri(int songId) {
        Uri sArtworkUri = Uri.parse("content://media/external/audio/media");
        return ContentUris.withAppendedId(sArtworkUri, songId);
    }

    public static Bitmap getAlbumArtBitmap(Context context, long albumId) {
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