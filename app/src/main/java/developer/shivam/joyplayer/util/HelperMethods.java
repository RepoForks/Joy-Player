package developer.shivam.joyplayer.util;

import android.content.Context;
import android.util.TypedValue;

public class HelperMethods {

    public static String getSongDuration (int duration) {
        int totalSeconds = duration / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return minutes + "m:" + seconds + "s";
    }

    public static int getDpFromPixel(Context context, int pixel) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixel, context.getResources().getDisplayMetrics());
    }
}
