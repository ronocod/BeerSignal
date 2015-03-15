package com.starstorm.beer.service

import com.parse.FunctionCallback
import com.parse.ParseCloud
import com.starstorm.beer.util.getDefaultParams

/**
 * Created by Conor on 16/10/2014.
 */
public object ParseUserService {

    public fun changeUsername(newUsername: String, callback: FunctionCallback<String>) {
        val params = getDefaultParams()
        params.put("newUsername", newUsername)
        ParseCloud.callFunctionInBackground<String>("changeusername", params, callback)
    }
}
