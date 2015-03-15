package com.starstorm.beer.util;

import com.crashlytics.android.Crashlytics;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Created by Conor on 05/11/2014.
 */
public class FacebookHelper {

    public static void getFacebookIdInBackground() {
        Request.newMeRequest(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (user == null) {
                    return;
                }
                ParseUser.getCurrentUser().put("facebookId", Long.valueOf(user.getId()));
                ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Crashlytics.logException(e);
                        }
                    }
                });
            }
        }).executeAsync();
    }
}
