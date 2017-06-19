package com.bizcom.vc.hg.ui.edu.bridgehandler;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.bizcom.vc.hg.util.MessageSendUtil;
import com.bizcom.vc.hg.web.ConstantParams;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.CallBackFunction;

/**
 * Created by admin on 2017/2/9.
 */

public class RemoveStudentHandler implements BridgeHandler {

    String tag = "RemoveStudentHandler";
    Context context;

    public RemoveStudentHandler(Context context) {
        this.context = context;
    }


    @Override
    public void handler(String data, CallBackFunction function) {
        Log.e(tag,"RemoveStudentHandler handler");
        String userId = null;
        if (!TextUtils.isEmpty(data)) {
            userId = data;
        }
        long uid = Long.parseLong("11" + userId);
        JSONObject json = new JSONObject();
        try {
            json.put("type", ConstantParams.MESSAGE_TYPE_REMOVE_BY_TEACHER);
            json.put("fromID", GlobalHolder.getInstance().getCurrentUserId());//自己的userId
            json.put("timeStamp", System.currentTimeMillis() + "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("message", json.toString());
        User user = GlobalHolder.getInstance().getUser(uid);
        Log.e("remote tv", user.getmUserId() + " " + user.getAccount() + " " + user.getNickName());
        new MessageSendUtil(context).sendMessageToRemote(json.toString(),
                GlobalHolder.getInstance().getUser(uid));//tv的UserId
        function.onCallBack("true");
    }
}
