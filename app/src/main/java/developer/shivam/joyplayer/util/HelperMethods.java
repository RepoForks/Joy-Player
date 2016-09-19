package developer.shivam.joyplayer.util;

public class HelperMethods {

    public static String getSongDuration (int duration) {
        int totalSeconds = duration / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return minutes + "m:" + seconds + "s";
    }
}
