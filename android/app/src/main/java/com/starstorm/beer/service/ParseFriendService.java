package com.starstorm.beer.service;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseObject;
import com.starstorm.beer.util.ParseHelper;

import java.util.HashMap;

/**
 * Created by Conor on 16/10/2014.
 */
public enum ParseFriendService implements FriendService {
    INSTANCE;

    @Override
    public void sendUnfriendRequest(ParseObject friendship, FunctionCallback<Object> callback) {
        HashMap<String, Object> params = ParseHelper.getDefaultParams();
        params.put("friendshipId", friendship.getObjectId());
        ParseCloud.callFunctionInBackground("unfriend", params, callback);
    }

    @Override
    public void sendFriendRequest(String username, FunctionCallback<Object> callback) {
        HashMap<String, Object> params = ParseHelper.getDefaultParams();
        params.put("username", username.toLowerCase());
        ParseCloud.callFunctionInBackground("sendfriendrequest", params, callback);
    }
}
