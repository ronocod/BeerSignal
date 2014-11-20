package com.starstorm.beer.util;

import com.starstorm.beer.BuildConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Conor on 17/09/2014.
 */
public abstract class ParseHelper {

    public static HashMap<String, Object> getDefaultParams() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("version", BuildConfig.VERSION_CODE);
        params.put("platform", "android");
        return params;
    }

}
