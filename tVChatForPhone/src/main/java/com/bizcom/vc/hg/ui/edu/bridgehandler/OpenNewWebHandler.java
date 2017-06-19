package com.bizcom.vc.hg.ui.edu.bridgehandler;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.bizcom.vc.hg.ui.edu.WebCourseNoToolbarActivity;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.CallBackFunction;

/**
 * Created by admin on 2017/1/20.
 */

public class OpenNewWebHandler implements BridgeHandler {


    String tag = "OpenNewWebHandler";

    Activity activity;

    public OpenNewWebHandler(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void handler(String data, CallBackFunction function) {
        Log.e(tag, "response data from js-->" + data);
        Intent intent = new Intent(activity, WebCourseNoToolbarActivity.class);
        intent.putExtra("url", data);
        activity.startActivity(intent);
    }
}
