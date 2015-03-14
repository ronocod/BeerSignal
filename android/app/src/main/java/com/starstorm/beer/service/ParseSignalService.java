package com.starstorm.beer.service;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.starstorm.beer.util.ParseHelper;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Conor on 16/10/2014.
 */
public enum ParseSignalService {
    INSTANCE;

    private long lastSignal;

    public void fireSignal(FunctionCallback<Object> callback) {
        fireSignal(null, callback);
    }

    public void fireSignal(List<String> recipients, FunctionCallback<Object> callback) {
        long now = System.currentTimeMillis();
        if (now - lastSignal < 10 * 1000) {
            // no spamming - limit calls to 1 every 10 seconds
            return;
        }
        lastSignal = now;

        HashMap<String, Object> params = ParseHelper.getDefaultParams();
        if (recipients != null && !recipients.isEmpty()) {
            params.put("recipients", recipients);
        }
        ParseCloud.callFunctionInBackground("signal", params, callback);
    }
}
