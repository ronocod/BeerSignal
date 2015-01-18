package com.starstorm.beer.test;

import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Log;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.starstorm.beer.BeerApplication;
import com.starstorm.beer.service.AuthService;
import com.starstorm.beer.service.ParseAuthService;

import java.util.ArrayList;
import java.util.List;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class FacebookFriendTest extends ApplicationTestCase<BeerApplication> {

    private static final String TAG = FacebookFriendTest.class.getSimpleName();
    private final AuthService authService = ParseAuthService.INSTANCE;

    public FacebookFriendTest() {
        super(BeerApplication.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        createApplication();
        final String username = ""; // Enter your test user's username
        final String password = ""; // Enter your test user's password
        ParseUser.logIn(username, password);
        Log.d(TAG, "setUp complete");
    }

    @MediumTest
    public void testFriendsFetch() throws Exception {
        Request.newMyFriendsRequest(ParseFacebookUtils.getSession(), new Request.GraphUserListCallback() {
            @Override
            public void onCompleted(List<GraphUser> users, Response response) {
                if (users == null) {
                    return;
                }
                List<Long> friendsList = new ArrayList<>();
                for (GraphUser user : users) {
                    friendsList.add(Long.valueOf(user.getId()));
                }

                // Construct a ParseUser query that will find friends whose
                // facebook IDs are contained in the current user's friend list.
                ParseQuery<ParseUser> friendQuery = ParseUser.getQuery();
                friendQuery.whereContainedIn("facebookId", friendsList);

                // findObjects will return a list of ParseUsers that are friends with
                // the current user
                friendQuery.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(final List<ParseUser> parseUsers, ParseException e) {
                        assertNotNull(parseUsers);
                        Log.d(TAG, "users: " + parseUsers.size());
                    }
                });
            }
        }).executeAndWait();
    }

    @Override
    public void tearDown() throws Exception {
        authService.logOut();
        Log.d(TAG, "tearDown complete");
    }
}