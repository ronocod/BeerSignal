package com.starstorm.beer.util

import com.starstorm.beer.BuildConfig

import java.util.HashMap

/**
 * Created by Conor on 17/09/2014.
 */
public fun getDefaultParams(): HashMap<String, Any> {
    val params = HashMap<String, Any>()
    params.put("version", BuildConfig.VERSION_CODE)
    params.put("platform", "android")
    return params
}
