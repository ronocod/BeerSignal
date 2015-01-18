package com.starstorm.beer.service;

import android.app.Activity;

import com.parse.FindCallback;
import com.parse.ParseUser;

/**
 * Created by conor on 18/01/15.
 */
public interface FacebookService {
    void fetchFacebookFriendUsers(Activity activity, FindCallback<ParseUser> callback);
}
