package com.bizcom.vc.hg.ui.edu.bridgehandler;

import android.app.Activity;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.CallBackFunction;

/**
 * Created by admin on 2017/1/16.
 */

public class AttendClassHandler implements BridgeHandler{

    String tag = "AttendClassHandler";

    Activity activity;

    public AttendClassHandler(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void handler(String data, CallBackFunction function) {

    }
}
