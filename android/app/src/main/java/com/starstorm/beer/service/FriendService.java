package com.starstorm.beer.service;

import com.parse.FunctionCallback;
import com.parse.ParseObject;

/**
 * Created by conor on 18/01/15.
 */
public interface FriendService {
    void sendUnfriendRequest(ParseObject friendship, FunctionCallback<Object> callback);

    void sendFriendRequest(String username, FunctionCallback<Object> callback);
}
