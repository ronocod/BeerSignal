package com.starstorm.beer.service;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.starstorm.beer.util.ParseHelper;

import java.util.HashMap;

/**
 * Created by Conor on 16/10/2014.
 */
public enum ParseUserService implements UserService {
    INSTANCE;

    @Override
    public void changeUsername(String newUsername, FunctionCallback<String> callback) {
        HashMap<String, Object> params = ParseHelper.getDefaultParams();
        params.put("newUsername", newUsername);
        ParseCloud.callFunctionInBackground("changeusername", params, callback);
    }
}
