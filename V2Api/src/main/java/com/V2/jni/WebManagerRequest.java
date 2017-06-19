package com.V2.jni;

import com.V2.jni.callback.WebManagerRequestCallback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangzhiguo on 16/3/1.
 */
public class WebManagerRequest {

    private static WebManagerRequest mWebManagerRequest;
    private List<WeakReference<WebManagerRequestCallback>> mCallBacks;

    private WebManagerRequest() {
        mCallBacks = new ArrayList<>();
    }

    public static synchronized WebManagerRequest getInstance() {
        if (mWebManagerRequest == null) {
            synchronized (WebManagerRequest.class) {
                if (mWebManagerRequest == null) {
                    mWebManagerRequest = new WebManagerRequest();
                    if (!mWebManagerRequest.initialize(mWebManagerRequest)) {
                        throw new RuntimeException("can't initilaize WebManagerRequest");
                    }
                }
            }
        }
        return mWebManagerRequest;
    }

    public void addCallback(WebManagerRequestCallback callback) {
        this.mCallBacks.add(new WeakReference<>(callback));
    }

    public void removeCallback(WebManagerRequestCallback callback) {
        for (int i = 0; i < mCallBacks.size(); i++) {
            WeakReference<WebManagerRequestCallback> wf = mCallBacks.get(i);
            if (wf != null && wf.get() != null) {
                if (wf.get() == callback) {
                    mCallBacks.remove(wf);
                    return;
                }
            }
        }
    }

    public native boolean initialize(WebManagerRequest request);

    public native void unInitialize();

    private void OnWebManagerDelUser(long userID) {
        for (int i = 0; i < mCallBacks.size(); i++) {
            WeakReference<WebManagerRequestCallback> wf = mCallBacks.get(i);
            if (wf != null && wf.get() != null) {
                wf.get().OnWebManagerDelUser(userID);
            }
        }
    }
}
