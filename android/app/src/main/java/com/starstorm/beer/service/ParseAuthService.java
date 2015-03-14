package com.starstorm.beer.service;

import com.parse.ParseInstallation;
import com.parse.ParseUser;

/**
 * Created by Conor on 18/10/2014.
 */
public enum ParseAuthService {
    INSTANCE;

    public void logOut() {
        ParseUser.logOut();
        ParseInstallation.getCurrentInstallation().remove("currentUser");
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
