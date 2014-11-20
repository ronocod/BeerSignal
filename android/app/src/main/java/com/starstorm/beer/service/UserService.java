package com.starstorm.beer.service;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.starstorm.beer.util.ParseHelper;

import java.util.HashMap;

/**
 * Created by Conor on 16/10/2014.
 */
public class UserService {

    public static void changeUsername(String newUsername, FunctionCallback<String> callback) {
        HashMap<String, Object> params = ParseHelper.getDefaultParams();
        params.put("newUsername", newUsername);
        ParseCloud.callFunctionInBackground("changeusername", params, callback);
    }
}
