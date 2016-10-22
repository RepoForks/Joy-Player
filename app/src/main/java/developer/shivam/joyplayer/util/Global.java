package developer.shivam.joyplayer.util;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

import developer.shivam.joyplayer.pojo.Songs;

/**
 * This is the global class containing variables
 *  that will be used globally to reduce the amount of
 *  fetching music from storage.
 */

public class Global extends Application {

    /**
     * This list will be used throughout the application
     *  which reduce the amount of songs retrieving.
     */
    List<Songs> songsList = new ArrayList<>();

    public List<Songs> getSongsList() {
        return songsList;
    }

    public void setSongsList(List<Songs> songsList) {
        this.songsList = songsList;
    }
}
