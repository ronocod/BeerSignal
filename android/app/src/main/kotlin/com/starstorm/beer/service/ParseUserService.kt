package com.starstorm.beer.service

import com.parse.FunctionCallback
import com.parse.ParseCloud
import com.starstorm.beer.util.ParseHelper

/**
 * Created by Conor on 16/10/2014.
 */
public enum class ParseUserService {
    INSTANCE

    public fun changeUsername(newUsername: String, callback: FunctionCallback<String>) {
        val params = ParseHelper.getDefaultParams()
        params.put("newUsername", newUsername)
        ParseCloud.callFunctionInBackground<String>("changeusername", params, callback)
    }
}
