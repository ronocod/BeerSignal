package com.starstorm.beer.activity

import android.app.Fragment
import android.app.FragmentManager
import android.content.Intent
import android.os.Bundle
import android.support.v13.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBarActivity
import android.view.Window

import com.parse.ParseAnalytics
import com.parse.ParseFacebookUtils
import com.parse.ParseUser
import com.starstorm.beer.R
import com.starstorm.beer.fragment.BigRedFragment
import com.starstorm.beer.fragment.FacebookFriendsFragment
import com.starstorm.beer.fragment.FriendsFragment
import com.starstorm.beer.fragment.YouFragment
import com.starstorm.beer.ui.slidingtabs.SlidingTabLayout

import java.util.Locale

public class MainActivity : ActionBarActivity() {

    private var youFragment: YouFragment? = null
    private var bigRedFragment: BigRedFragment? = null
    private var friendListFragment: FriendsFragment? = null
    private var facebookFriendListFragment: FacebookFriendsFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ParseUser.getCurrentUser() == null) {
            // no logged-in user, go to login screen
            val intent = Intent(this, javaClass<LoginActivity>())
            startActivity(intent)
            // empty animation for an instant transition
            overridePendingTransition(0, 0)
            return
        }

        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)

        setContentView(R.layout.activity_main)

        ParseAnalytics.trackAppOpenedInBackground(getIntent())

        val sectionsPagerAdapter = SectionsPagerAdapter(getFragmentManager())
        val viewPager = findViewById(R.id.pager) as ViewPager
        viewPager.setAdapter(sectionsPagerAdapter)

        val slidingTabLayout = findViewById(R.id.sliding_tabs) as SlidingTabLayout
        slidingTabLayout.setViewPager(viewPager)

        viewPager.setCurrentItem(1, false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data)
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            when (position) {
                0 -> {
                    if (youFragment == null) {
                        youFragment = YouFragment.newInstance()
                    }
                    return youFragment!!
                }
                1 -> {
                    if (bigRedFragment == null) {
                        bigRedFragment = BigRedFragment.newInstance()
                    }
                    return bigRedFragment!!
                }
                2 -> {
                    if (friendListFragment == null) {
                        friendListFragment = FriendsFragment.newInstance()
                    }
                    return friendListFragment!!
                }
                3 -> {
                    if (facebookFriendListFragment == null) {
                        facebookFriendListFragment = FacebookFriendsFragment.newInstance()
                    }
                    return facebookFriendListFragment!!
                }
            }
            return Fragment()
        }

        override fun getCount(): Int {
            return 4
        }

        override fun getPageTitle(position: Int): CharSequence? {
            val l = Locale.getDefault()
            val title = when (position) {
                0 -> {
                    val currentUser = ParseUser.getCurrentUser()
                    when {
                        currentUser != null -> currentUser.getUsername()
                        else -> getString(R.string.title_section0)
                    }
                }
                1 -> getString(R.string.title_section1)
                2 -> getString(R.string.title_section2)
                3 -> "Facebook Friends test"
                else -> ""
            }
            return title.toUpperCase(l)
        }
    }

}
