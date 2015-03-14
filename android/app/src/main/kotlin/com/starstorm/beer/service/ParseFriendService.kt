package com.starstorm.beer.service

import com.parse.FunctionCallback
import com.parse.ParseCloud
import com.parse.ParseObject
import com.starstorm.beer.util.getDefaultParams

/**
 * Created by Conor on 16/10/2014.
 */
public enum class ParseFriendService {
    INSTANCE

    public fun sendUnfriendRequest(friendship: ParseObject, callback: FunctionCallback<Any>) {
        val params = getDefaultParams()
        params.put("friendshipId", friendship.getObjectId())
        ParseCloud.callFunctionInBackground<Any>("unfriend", params, callback)
    }

    public fun sendFriendRequest(username: String, callback: FunctionCallback<Any>) {
        val params = getDefaultParams()
        params.put("username", username.toLowerCase())
        ParseCloud.callFunctionInBackground<Any>("sendfriendrequest", params, callback)
    }
}
