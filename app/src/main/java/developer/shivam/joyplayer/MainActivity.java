package developer.shivam.joyplayer;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    String[] viewPagerCategory = {"Songs", "Album", "Artist", "Playlist"};
    static AppCompatSeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setLollipopStyle();

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        seekBar = (AppCompatSeekBar) findViewById(R.id.seekBar);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(android.R.color.white));

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(4);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setLollipopStyle() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {

                getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {

                getWindow().getDecorView()
                        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

                getWindow().setStatusBarColor(Color.TRANSPARENT);
            }

            LinearLayout fakeStatusBar = (LinearLayout) findViewById(R.id.fakeStatusBar);
            fakeStatusBar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            fakeStatusBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) getResources().getDimension(R.dimen.statusBarHeight)));
        }
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {

                case 0 : return new SongsListFragment();

                case 1 : return new SongsListFragment();

                case 2 : return new SongsListFragment();

                case 3 : return new SongsListFragment();

                default: return null;
            }
        }

        @Override
        public int getCount() {
            return viewPagerCategory.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return viewPagerCategory[position].toString();
        }
    }
}
