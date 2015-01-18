package com.starstorm.beer.service;

import com.parse.FunctionCallback;

import java.util.List;

/**
 * Created by conor on 18/01/15.
 */
public interface SignalService {
    void fireSignal(FunctionCallback<Object> callback);

    void fireSignal(List<String> recipients, FunctionCallback<Object> callback);
}
