package com.starstorm.beer.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Window;

import com.parse.ParseAnalytics;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.starstorm.beer.R;
import com.starstorm.beer.fragment.BigRedFragment;
import com.starstorm.beer.fragment.FacebookFriendsFragment;
import com.starstorm.beer.fragment.FriendsFragment;
import com.starstorm.beer.fragment.YouFragment;
import com.starstorm.beer.ui.slidingtabs.SlidingTabLayout;

import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends BaseActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * A custom {@link ViewPager} title strip which looks much like Tabs present in Android v4.0 and
     * above, but is designed to give continuous feedback to the user when scrolling.
     */
    @InjectView(R.id.sliding_tabs)
    SlidingTabLayout mSlidingTabLayout;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    YouFragment youFragment;
    BigRedFragment bigRedFragment;
    FriendsFragment friendListFragment;
    FacebookFriendsFragment facebookFriendListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ParseUser.getCurrentUser() == null) {
            // no logged-in user, go to login screen
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            // empty animation for an instant transition
            overridePendingTransition(0, 0);
            return;
        }

        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);
        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mSlidingTabLayout.setViewPager(mViewPager);

        mViewPager.setCurrentItem(1, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case 0:
                    if (youFragment == null) {
                        youFragment = YouFragment.newInstance();
                    }
                    return youFragment;
                case 1:
                    if (bigRedFragment == null) {
                        bigRedFragment = BigRedFragment.newInstance();
                    }
                    return bigRedFragment;
                case 2:
                    if (friendListFragment == null) {
                        friendListFragment = FriendsFragment.newInstance();
                    }
                    return friendListFragment;
                case 3:
                    if (facebookFriendListFragment == null) {
                        facebookFriendListFragment = FacebookFriendsFragment.newInstance();
                    }
                    return facebookFriendListFragment;
            }
            return new Fragment();
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    ParseUser currentUser = ParseUser.getCurrentUser();
                    String title = (currentUser != null ) ? currentUser.getUsername() : getString(R.string.title_section0);
                    return title.toUpperCase(l);
                case 1:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 3:
                    return "Facebook Friends test".toUpperCase(l);
            }
            return null;
        }
    }

}
