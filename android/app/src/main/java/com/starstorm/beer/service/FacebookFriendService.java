package com.starstorm.beer.service;

import android.app.Activity;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.FindCallback;
import com.parse.ParseFacebookUtils;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Conor on 16/10/2014.
 */
public class FacebookFriendService {

    public static void fetchFacebookFriendUsers(final Activity activity, final FindCallback<ParseUser> callback) {

        if (ParseFacebookUtils.getSession().getPermissions().contains("user_friends")) {
            // we already have the permission
            performRequest(callback);
        } else {
            // get the permission and then perform the request
            ParseFacebookUtils.linkInBackground(ParseUser.getCurrentUser(), Arrays.asList("user_friends"), activity)
                .onSuccess(new Continuation<Void, Void>() {
                    @Override
                    public Void then(Task<Void> voidTask) throws Exception {
                        performRequest(callback);
                        return null;
                    }
                });
        }
    }

    private static void performRequest(final FindCallback<ParseUser> callback) {
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
                friendQuery.findInBackground(callback);
            }
        }).executeAsync();
    }
}
