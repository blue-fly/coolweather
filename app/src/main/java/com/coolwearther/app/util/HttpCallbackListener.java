package com.coolwearther.app.util;

/**
 * Created by admin on 2015-08-30.
 */
public interface HttpCallbackListener {
    void onFinish(String response);

    void onError(Exception e);
}
