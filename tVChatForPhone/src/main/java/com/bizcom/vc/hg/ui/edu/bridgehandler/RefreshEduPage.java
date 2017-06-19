package com.bizcom.vc.hg.ui.edu.bridgehandler;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bizcom.vc.hg.ui.HTab0;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.CallBackFunction;

/**
 * Created by admin on 2017/2/10.
 */

public class RefreshEduPage implements BridgeHandler {


    String tag = "RefreshEduPage";

    Context context;

    public RefreshEduPage(Context context) {
        this.context = context;
    }

    @Override
    public void handler(String data, CallBackFunction function) {
        Log.e(tag, "do refresh");
        Intent intent = new Intent(HTab0.ON_PAGE_REFRESH_ACTION);
        context.sendBroadcast(intent);
    }
}
