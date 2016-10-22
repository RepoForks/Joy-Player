package developer.shivam.joyplayer.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import developer.shivam.joyplayer.pojo.Songs;

public class Sorter {

    public static List<Songs> sort(List<Songs> list) {
        Collections.sort(list, new Comparator<Songs>() {
            @Override
            public int compare(Songs left, Songs right) {
                return left.getName().compareTo(right.getName());
            }
        });
        return list;
    }

    public static List<Songs> getTopRecentAdded(List<Songs> list) {
        Collections.sort(list, new Comparator<Songs>() {
            @Override
            public int compare(Songs left, Songs right) {
                return left.getDateAdded() - right.getDateAdded();
            }
        });

        Collections.reverse(list);
        return list;
    }
}
