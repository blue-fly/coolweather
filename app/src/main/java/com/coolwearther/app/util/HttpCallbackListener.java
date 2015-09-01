package com.coolwearther.app.util;

/**
 * Created by dellpc on 2015-08-30.
 */
public interface HttpCallbackListener {
    void onFinish(String response);

    void onError(Exception e);
}
