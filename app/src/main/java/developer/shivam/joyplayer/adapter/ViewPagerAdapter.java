package developer.shivam.joyplayer.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    Context mContext;
    List<Fragment> fragmentList;

    public ViewPagerAdapter(Context context, FragmentManager fm, List<Fragment> list) {
        super(fm);
        mContext = context;
        fragmentList = list;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "TRACKS";
        } else {
            return "ALBUMS";
        }
    }
}
