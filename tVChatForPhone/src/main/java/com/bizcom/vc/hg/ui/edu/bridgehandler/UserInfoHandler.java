package com.bizcom.vc.hg.ui.edu.bridgehandler;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.bizcom.vo.User;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.CallBackFunction;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 2017/1/16.
 */

public class UserInfoHandler implements BridgeHandler {

    String tag = "UserInfoHandler";
    String result = null;
    User user;

    public UserInfoHandler(User user) {
        this.user = user;
        long mUserId = user.getmUserId();
        String nickname= user.getNickName();
        String account =user.getAccount();
        String userName = user.getCommentName();
        Map<String,Object> map = new HashMap<>();
        map.put("mUserId",mUserId);
        map.put("mNickName",nickname);
        map.put("mAccount",account);
        map.put("mCommentName",userName);
        result = JSONObject.otJSONString(map);
    }

    @Override
    public void handler(String data, CallBackFunction function) {
        Log.e(tag,"user map json-->"+result);
        function.onCallBack(result);
    }
}
