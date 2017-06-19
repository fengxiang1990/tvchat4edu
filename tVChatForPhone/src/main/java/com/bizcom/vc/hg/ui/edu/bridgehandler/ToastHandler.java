package com.bizcom.vc.hg.ui.edu.bridgehandler;

import android.content.Context;
import android.widget.Toast;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.CallBackFunction;

/**
 * Created by admin on 2017/2/7.
 */

public class ToastHandler implements BridgeHandler {

    String tag = "ToastHandler";

    Context context;

    public ToastHandler(Context context) {
        this.context = context;
    }

    @Override
    public void handler(String data, CallBackFunction function) {
        Toast.makeText(context, data, Toast.LENGTH_SHORT).show();
    }
}
