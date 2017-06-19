package com.V2.jni.callbacAdapter;

import com.V2.jni.callback.WebManagerRequestCallback;
import com.bizcom.util.V2Log;

/**
 * Created by wangzhiguo on 16/3/1.
 */
public abstract class WebManagerRequestCallbackAdapter implements WebManagerRequestCallback {

    @Override
    public void OnWebManagerDelUser(long userID) {
        V2Log.jniCall("OnWebManagerDelUser", " userID: " + userID);
    }
}
