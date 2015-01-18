package com.starstorm.beer.service;

import com.parse.FunctionCallback;

/**
 * Created by conor on 18/01/15.
 */
public interface UserService {
    void changeUsername(String newUsername, FunctionCallback<String> callback);
}
