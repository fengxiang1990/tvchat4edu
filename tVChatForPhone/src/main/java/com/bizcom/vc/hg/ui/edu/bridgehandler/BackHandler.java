package com.bizcom.vc.hg.ui.edu.bridgehandler;

import android.app.Activity;
import android.util.Log;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.CallBackFunction;

/**
 * Created by admin on 2017/1/16.
 */

public class BackHandler implements BridgeHandler{

    String tag  = "BackHandler";
    Activity activity;
    public BackHandler(Activity activity){
        this.activity = activity;
    }
    @Override
    public void handler(String data, CallBackFunction function) {
        Log.e(tag,"BackHandler");
        activity.finish();
    }
}
