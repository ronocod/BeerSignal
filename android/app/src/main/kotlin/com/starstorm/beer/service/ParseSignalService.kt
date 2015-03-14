package com.starstorm.beer.service

import com.parse.FunctionCallback
import com.parse.ParseCloud
import com.starstorm.beer.util.ParseHelper

/**
 * Created by Conor on 16/10/2014.
 */
public enum class ParseSignalService {
    INSTANCE

    private var lastSignal: Long = 0

    public fun fireSignal(callback: FunctionCallback<Any>) {
        fireSignal(null, callback)
    }

    public fun fireSignal(recipients: List<String>?, callback: FunctionCallback<Any>) {
        val now = System.currentTimeMillis()
        if (now - lastSignal < 10 * 1000) {
            // no spamming - limit calls to 1 every 10 seconds
            return
        }
        lastSignal = now

        val params = ParseHelper.getDefaultParams()
        if (recipients != null && !recipients.isEmpty()) {
            params.put("recipients", recipients)
        }
        ParseCloud.callFunctionInBackground<Any>("signal", params, callback)
    }
}
