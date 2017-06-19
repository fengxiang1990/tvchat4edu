package com.bizcom.vc.hg.web.interf;


import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.MainApplication;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Request;
import com.bizcom.vc.hg.beans.queryPhoneInfoBean;
import com.bizcom.vc.hg.ui.edu.CourseConfig;
import com.bizcom.vc.hg.ui.edu.bridgehandler.model.CheckUserRoleResponse;
import com.bizcom.vc.hg.ui.edu.bridgehandler.model.StudentInfo;
import com.bizcom.vc.hg.ui.edu.bridgehandler.model.StudentInfo2;
import com.bizcom.vc.hg.util.MessageSendUtil;
import com.bizcom.vc.hg.web.ConstantParams;
import com.bizcom.vc.hg.web.IQuery;
import com.bizcom.vc.hg.web.LinkInfo;
import com.bizcom.vc.hg.web.MD5;
import com.bizcom.vc.hg.web.OnRespListener;
import com.bizcom.vc.hg.web.Web;
import com.bizcom.vc.hg.web.models.NickName;
import com.bizcom.vc.hg.web.models.UserCenterCallLong;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.google.gson.reflect.TypeToken;
import com.shdx.tvchat.phone.BuildConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Decoder.BASE64Encoder;

public class BussinessManger implements IBussinessManager {
    public final static String ErrorText = "出现未知错误";
    public final static String CHANNEL = "Android";
    private Context mcon;

    public static final String REQUEST_PARAM_LEFT = "&" + "requestMessage=";


    private BussinessManger(Context mcon) {
        this.mcon = mcon;
    }

    public static synchronized IBussinessManager getInstance(Context mCon) {

        return new BussinessManger(mCon);

    }

    @Override
    public void tvAdd(OnResponseListener lis, String sn, String channel) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();


            map.put("sn", sn);
            map.put("channel", channel);
            map.put("methodName", "tvAdd");
            IQuery query = Web.getQuery(LinkInfo.WEBURL + "tvAdd.html");

            query.queryPost(map, new OnRespListener() {

                @Override
                public void onResp(int status, Map<String, Object> result, String msg) {

                }

                @Override
                public void onResp(int status, String result, String msg) {
                    //					返回报文：
                    //					{"data":{"uid":6,"pwd":"531070","nickName":"6762304","userName":"6762304"},"code":"0000","msg":"新增成功"}

                }
            });
        } catch (Exception e) {
            lis.onResponse(false, -1, ErrorText);
        }

    }

    @Override
    public void credentials(final OnResponseListener lis, String uid, String tvId, String channel) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();

            map.put("methodName", "credentials");
            map.put("uid", uid);
            map.put("tvId", tvId);
            map.put("channel", channel);

            IQuery query = Web.getQuery(LinkInfo.WEBURL + "credentials.html");

            query.queryPost(map, new OnRespListener() {

                @Override
                public void onResp(int status, Map<String, Object> result, String msg) {

                }

                @Override
                public void onResp(int status, String result, String msg) {
                    System.out.println("");
                    lis.onResponse(true, 0, result);

                }
            });
        } catch (Exception e) {
            lis.onResponse(false, -1, ErrorText);
        }

    }

    @Override
    public void binding(final OnResponseListener lis, String uid, String tvId, String channel) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("methodName", "binding");
            map.put("uid", uid);
            map.put("tvId", tvId);
            map.put("channel", channel);

            IQuery query = Web.getQuery(LinkInfo.WEBURL + "binding.html");

            query.queryPost(map, new OnRespListener() {

                @Override
                public void onResp(int status, Map<String, Object> result, String msg) {

                }

                @Override
                public void onResp(int status, String result, String msg) {

                    if (TextUtils.isEmpty(result)) {
                        return;
                    }
                    JSONObject mJson = JSON.parseObject(result);

                    if (checkResponse(mJson, lis)) {
                        lis.onResponse(true, 0, mJson.get("msg"));
                    }

                }
            });
        } catch (Exception e) {
            lis.onResponse(false, -1, ErrorText);
        }

    }

    @Override
    public void removeBingding(final OnResponseListener lis, String uid, String tvId, String channel) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("methodName", "removeBingding");
            map.put("uid", uid);
            map.put("tvId", tvId);
            map.put("channel", channel);

            IQuery query = Web.getQuery(LinkInfo.WEBURL + "removeBingding.html");

            query.queryPost(map, new OnRespListener() {

                @Override
                public void onResp(int status, Map<String, Object> result, String msg) {

                }

                @Override
                public void onResp(int status, String result, String msg) {


                    if (TextUtils.isEmpty(result)) {
                        return;
                    }
                    JSONObject mJson = JSON.parseObject(result);

                    if (checkResponse(mJson, lis)) {
                        lis.onResponse(true, 0, mJson.get("msg"));
                    }


                }
            });
        } catch (Exception e) {
            lis.onResponse(false, -1, ErrorText);
        }

    }

    @Override
    public void queryTvAndUserRel(OnResponseListener lis, String friendId, String tvId, String channel) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("uid", friendId);
            map.put("tvId", tvId);
            map.put("channel", channel);
            map.put("friendId", friendId);
            map.put("methodName", "queryTvAndUserRel");


            IQuery query = Web.getQuery(LinkInfo.WEBURL + "queryTvAndUserRel.html");

            query.queryPost(map, new OnRespListener() {

                @Override
                public void onResp(int status, Map<String, Object> result, String msg) {

                }

                @Override
                public void onResp(int status, String result, String msg) {

                }
            });
        } catch (Exception e) {
            lis.onResponse(false, -1, ErrorText);
        }

    }

    @Override
    public void addTvFriend(final OnResponseListener lis, String friendId, String tvId, String uid, String channel) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("uid", uid);
            map.put("tvId", tvId);
            map.put("friendId", friendId);
            map.put("channel", channel);
            map.put("groupId", "0");
            map.put("methodName", "addTvFriend");

            IQuery query = Web.getQuery(LinkInfo.WEBURL + "addTvFriend.html");

            query.queryPost(map, new OnRespListener() {

                @Override
                public void onResp(int status, Map<String, Object> result, String msg) {

                }

                @Override
                public void onResp(int status, String result, String msg) {


                    if (TextUtils.isEmpty(result)) {
                        return;
                    }
                    JSONObject mJson = JSON.parseObject(result);

                    if (checkResponse(mJson, lis)) {
                        Object data = mJson.get("msg");
                        lis.onResponse(true, 0, data);
                    }


                }
            });
        } catch (Exception e) {
            lis.onResponse(false, -1, ErrorText);
        }

    }

    @Override
    public void delTvFriend(final OnResponseListener lis, String friendId, String tvId, String uid, String channel) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("uid", uid);
            map.put("tvId", tvId);
            map.put("channel", channel);
            map.put("friendId", friendId);
            map.put("groupId", "0");
            map.put("methodName", "delTvFriend");

            IQuery query = Web.getQuery(LinkInfo.WEBURL + "delTvFriend.html");

            query.queryPost(map, new OnRespListener() {

                @Override
                public void onResp(int status, Map<String, Object> result, String msg) {

                }

                @Override
                public void onResp(int status, String result, String msg) {


                    if (TextUtils.isEmpty(result)) {
                        return;
                    }
                    JSONObject mJson = JSON.parseObject(result);

                    if (checkResponse(mJson, lis)) {
                        Object data = mJson.get("msg");
                        lis.onResponse(true, 0, data);
                    }


                }
            });
        } catch (Exception e) {
            lis.onResponse(false, -1, ErrorText);
        }
    }

    @Override
    public void queryTvUserFriend(final OnResponseListener lis, String tvId, String firendId, String uid, String channel) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("tvId", tvId);
            map.put("channel", channel);
            map.put("firendId", firendId);
            map.put("userId", uid);
            IQuery query = Web.getQuery(LinkInfo.WEBURL + "queryByFriend.shtml");

            query.queryPost(map, new OnRespListener() {

                @Override
                public void onResp(int status, Map<String, Object> result, String msg) {

                }

                @Override
                public void onResp(int status, String result, String msg) {
                    if (TextUtils.isEmpty(result)) {
                        return;
                    }
                    Log.i("tvliao", result);
                    JSONObject mJson = JSON.parseObject(result);

//                    if (checkResponse(mJson, lis)) {
                    lis.onResponse(true, 0, mJson);
//                    }
                }
            });
        } catch (Exception e) {
            lis.onResponse(false, -1, ErrorText);
        }
    }

    @Override
    public void queryTvUserFriend(OnResponseListener lis, String tvId, String uid, String channel) {
        // TODO Auto-generated method stub

    }

    @Override
    public void queryTvUserFriend(final OnResponseListener lis, String tvId, String channel) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("tvId", tvId);
            map.put("channel", channel);
            map.put("methodName", "queryTvUserFriend");
            IQuery query = Web.getQuery(LinkInfo.WEBURL + "queryTvUserFriend.html");

            query.queryPost(map, new OnRespListener() {

                @Override
                public void onResp(int status, Map<String, Object> result, String msg) {

                }

                @Override
                public void onResp(int status, String result, String msg) {
                    if (TextUtils.isEmpty(result)) {
                        return;
                    }
                    JSONObject mJson = JSON.parseObject(result);

                    if (checkResponse(mJson, lis)) {
                        Object data = mJson.get("data");
                        lis.onResponse(true, 0, data);
                    }
                }
            });
        } catch (Exception e) {
            lis.onResponse(false, -1, ErrorText);
        }

    }

    @Override
    public void queryTvByUid(final OnResponseListener lis, String uid, String channel) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("uid", uid);
            map.put("channel", channel);
            map.put("methodName", "queryTvByUid");

            IQuery query = Web.getQuery(LinkInfo.WEBURL + "queryTvByUid.html");

            query.queryPost(map, new OnRespListener() {

                @Override
                public void onResp(int status, Map<String, Object> result, String msg) {

                }

                @Override
                public void onResp(int status, String result, String msg) {
                    if (TextUtils.isEmpty(result)) {
                        return;
                    }
                    JSONObject mJson = JSON.parseObject(result);

                    if (checkResponse(mJson, lis)) {
                        Object data = mJson.get("data");
                        lis.onResponse(true, 0, data);
                    }
                }
            });
        } catch (Exception e) {
            lis.onResponse(false, -1, ErrorText);
        }

    }

    protected boolean checkResponse(JSONObject mJson, OnResponseListener lis) {

        String returnCode = mJson.get("code") + "";
        String returnMsg = mJson.get("msg") + "";
        if (TextUtils.equals(returnCode, "0000")) {
            return true;
        } else {
            lis.onResponse(false, -1, returnMsg);
        }
        return false;
    }

    @Override
    public void queryTvByTvId(final OnResponseListener lis, String tvId, String channel) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("tvId", tvId);
            map.put("channel", channel);
            map.put("methodName", "queryTvByTvId");


            IQuery query = Web.getQuery(LinkInfo.WEBURL + "queryTvByTvId.html");

            query.queryPost(map, new OnRespListener() {

                @Override
                public void onResp(int status, Map<String, Object> result, String msg) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onResp(int status, String result, String msg) {
                    if (TextUtils.isEmpty(result)) {
                        return;
                    }
                    JSONObject mJson = JSON.parseObject(result);

                    if (checkResponse(mJson, lis)) {
                        Object data = mJson.get("data");
                        lis.onResponse(true, 0, data);
                    }
                }
            });
        } catch (Exception e) {
            lis.onResponse(false, -1, ErrorText);
        }

    }

    @Override
    public void registerUser(final OnResponseListener lis, String phone, String accountNo, String userName, String passWord) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("phone", phone);
            map.put("accountNo", accountNo);
            map.put("userName", userName);
            map.put("methodName", "registerUser");

            MD5 md5 = new MD5();
            passWord = md5.getMD5ofStr(passWord);//MD5
            passWord = passWord.toUpperCase();
            map.put("passWord", passWord);

            IQuery query = Web.getQuery(LinkInfo.WEBURL + "registerUser.html");

            query.queryPost(map, new OnRespListener() {

                @Override
                public void onResp(int status, Map<String, Object> result, String msg) {

                }

                @Override
                public void onResp(int status, String result, String msg) {
                    if (TextUtils.isEmpty(result)) return;

                    JSONObject mJson = JSON.parseObject(result);

                    if (mJson == null) return;
                    String code = mJson.getString("result") == null ? "" : mJson.getString("result");
                    String info = mJson.getString("info") == null ? ErrorText : mJson.getString("info");

                    if (TextUtils.equals(code, "0")) {
                        lis.onResponse(true, 0, info);
                    } else {
                        lis.onResponse(false, 0, info);
                    }
                }
            });
        } catch (Exception e) {
            lis.onResponse(false, -1, ErrorText);
        }

    }

    @Override
    public void checkPhoneIsRegister(final OnResponseListener lis, String phone, String accountNo) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("phone", phone);
            map.put("accountNo", accountNo);
            map.put("methodName", "checkPhoneIsRegister");

            IQuery query = Web.getQuery(LinkInfo.WEBURL + "checkPhoneIsRegister.html");

            query.queryPost(map, new OnRespListener() {

                @Override
                public void onResp(int status, Map<String, Object> result, String msg) {

                }

                @Override
                public void onResp(int status, String result, String msg) {
                    if (TextUtils.isEmpty(result)) return;

                    JSONObject mJson = JSON.parseObject(result);

                    if (mJson == null) return;
                    String code = mJson.getString("result") == null ? "" : mJson.getString("result");
                    String info = mJson.getString("info") == null ? ErrorText : mJson.getString("info");

                    if (TextUtils.equals(code, "0")) {
                        lis.onResponse(true, 0, info);
                    } else {
                        lis.onResponse(false, 0, code);
                    }
                }
            });
        } catch (Exception e) {
            lis.onResponse(false, -1, ErrorText);
        }


    }

    @Override
    public void serviceAddr(final OnResponseListener lis, String channel) {


        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("channel", channel);
            map.put("methodName", "serviceAddr");
            IQuery query = Web.getQuery(BuildConfig.HOST + "serviceAddr.html");
            query.queryPost(map, new OnRespListener() {

                @Override
                public void onResp(int status, Map<String, Object> result, String msg) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onResp(int status, String result, String msg) {
                    if (TextUtils.isEmpty(result)) {
                        return;
                    }
                    JSONObject mJson = JSON.parseObject(result);

                    if (checkResponse(mJson, lis)) {
                        Object data = mJson.get("data");
                        lis.onResponse(true, 0, data);
                    }
                }
            });
        } catch (Exception e) {
            lis.onResponse(false, -1, ErrorText);
        }


    }

    @Override
    public void smsVerificationCode(final OnResponseListener lis, String phone, String sign) {

        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("phone", phone);
            map.put("sign", sign);
            map.put("methodName", "smsVerificationCode");
            IQuery query = Web.getQuery(LinkInfo.WEBURL + "smsVerificationCode.html");

            query.queryPost(map, new OnRespListener() {

                @Override
                public void onResp(int status, Map<String, Object> result, String msg) {

                }

                @Override
                public void onResp(int status, String result, String msg) {

                    if (TextUtils.isEmpty(result)) return;

                    JSONObject mJson = JSON.parseObject(result);

                    if (mJson == null) return;
                    String code = mJson.getString("result") == null ? "" : mJson.getString("result");
                    String info = mJson.getString("info") == null ? ErrorText : mJson.getString("info");
                    String qCode = mJson.getString("code") == null ? "" : mJson.getString("code");

                    if (TextUtils.equals(code, "0")) {
                        lis.onResponse(true, 0, qCode);
                    } else {
                        lis.onResponse(false, 0, info);
                    }

                }
            });
        } catch (Exception e) {
            lis.onResponse(false, -1, ErrorText);
        }

    }

    @Override
    public void updatePwd(final OnResponseListener lis, String uid, String passWord) {

        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("uid", uid);
            map.put("methodName", "updatePwd");

            MD5 md5 = new MD5();
            passWord = md5.getMD5ofStr(passWord);//MD5
            passWord = passWord.toUpperCase();
            map.put("passWord", passWord);

            IQuery query = Web.getQuery(LinkInfo.WEBURL + "updatePwd.html");

            query.queryPost(map, new OnRespListener() {

                @Override
                public void onResp(int status, Map<String, Object> result, String msg) {

                }

                @Override
                public void onResp(int status, String result, String msg) {
                    //					{"result":"0000","info":"密码修改成功~"}
                    if (TextUtils.isEmpty(result)) return;

                    JSONObject mJson = JSON.parseObject(result);

                    if (mJson == null) return;
                    String code = mJson.getString("result") == null ? "" : mJson.getString("result");
                    String info = mJson.getString("info") == null ? ErrorText : mJson.getString("info");
                    String qCode = mJson.getString("code") == null ? "" : mJson.getString("code");

                    if (TextUtils.equals(code, "0000")) {
                        lis.onResponse(true, 0, qCode);
                    } else {
                        lis.onResponse(false, 0, info);
                    }

                }
            });
        } catch (Exception e) {
            lis.onResponse(false, -1, ErrorText);
        }


    }

    @Override
    public void queryByTvSn(final OnResponseListener lis, String userName) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("userName", userName);
            map.put("channel", BussinessManger.CHANNEL);
            map.put("methodName", "queryByTvSn");

            IQuery query = Web.getQuery(LinkInfo.WEBURL + "queryByTvSn.html");

            query.queryPost(map, new OnRespListener() {

                @Override
                public void onResp(int status, Map<String, Object> result, String msg) {

                }

                @Override
                public void onResp(int status, String result, String msg) {
                    if (TextUtils.isEmpty(result)) {
                        return;
                    }
                    JSONObject mJson = JSON.parseObject(result);

                    if (checkResponse(mJson, lis)) {
                        Map<String, Object> data = (Map<String, Object>) mJson.get("data");
                        lis.onResponse(true, 0, data.get("tvId"));
                    }
                }
            });
        } catch (Exception e) {
            lis.onResponse(false, -1, ErrorText);
        }

    }

    @Override
    public void versionUpInfo(final OnResponseListener lis) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("clientType", "2");
            map.put("methodName", "versionUpInfo");
            IQuery query = Web.getQuery(LinkInfo.WEBURL + "versionUpInfo.do");

            query.queryPost(map, new OnRespListener() {

                @Override
                public void onResp(int status, Map<String, Object> result, String msg) {

                }

                @Override
                public void onResp(int status, String result, String msg) {
                    if (TextUtils.isEmpty(result)) {
                        return;
                    }
                    JSONObject mJson = JSON.parseObject(result);
                    String code = mJson.getString("code");
                    String desc = mJson.getString("desc");
                    if (TextUtils.equals(code, "0")) {
                        lis.onResponse(true, 0, mJson.get("result"));
                    } else {
                        lis.onResponse(false, -1, mJson.get(desc));
                    }
                }
            });
        } catch (Exception e) {
            lis.onResponse(false, -1, ErrorText);
        }
    }

    @Override
    public void notifyTvBatchAddFrends(long fromUserId, String frends, int tvId) {
        JSONObject json = new JSONObject();
        try {
            json.put("type", ConstantParams.MESSAGE_TYPE_FRIEND_BATCH_ADD);
            json.put("fromID", fromUserId);//自己的userId
            json.put("friendlist", frends);
            json.put("timeStamp", System.currentTimeMillis() + "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("message", json.toString());
        User user = GlobalHolder.getInstance().getUser(tvId);
        Log.e("remote tv", user.getmUserId() + " " + user.getAccount() + " " + user.getNickName());
        new MessageSendUtil(mcon).sendMessageToRemote(json.toString(),
                GlobalHolder.getInstance().getUser(tvId));//tv的UserId
    }

    @Override
    public void notifyTvBatchDelFrends(long fromUserId, String frends, int tvId) {
        JSONObject json = new JSONObject();
        try {
            json.put("type", ConstantParams.MESSAGE_TYPE_FRIEND_BATCH_DEL);
            json.put("fromID", fromUserId);//自己的userId
            json.put("friendlist", frends);
            json.put("timeStamp", System.currentTimeMillis() + "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("message", json.toString());
        User user = GlobalHolder.getInstance().getUser(tvId);
        Log.e("remote tv", user.getmUserId() + " " + user.getAccount() + " " + user.getNickName());
        new MessageSendUtil(mcon).sendMessageToRemote(json.toString(),
                GlobalHolder.getInstance().getUser(tvId));//tv的UserId
    }

    @Override
    public void getMediaEncodeType(final OnResponseListener lis, String modle) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("phone_model", modle);
            map.put("channel", "1");
            map.put("methodName", "queryPhoneInfo");

            IQuery query = Web.getQuery(LinkInfo.WEBURL + "queryPhoneInfo.html");

            query.queryPost(map, new OnRespListener() {

                @Override
                public void onResp(int status, Map<String, Object> result, String msg) {

                }

                @Override
                public void onResp(int status, String result, String msg) {
                    if (TextUtils.isEmpty(result)) {
                        return;
                    }
                    JSONObject mJson = JSON.parseObject(result);

                    if (checkResponse(mJson, lis)) {
                        Object data = mJson.get("data");
                        if (data != null) {
                            Map<String, Object> m = (Map<String, Object>) data;
                            queryPhoneInfoBean mBean = new queryPhoneInfoBean();

                            mBean.setPhone_model(String.valueOf(m.get("phone_model")));
                            mBean.setP_p_frame(String.valueOf(m.get("p_p_frame")));
                            mBean.setP_p_rate(String.valueOf(m.get("p_p_rate")));
                            mBean.setP_p_resolving(String.valueOf(m.get("p_p_resolving")));
                            mBean.setP_t_rate(String.valueOf(m.get("p_t_rate")));
                            mBean.setP_t_resolving(String.valueOf(m.get("p_t_resolving")));
                            mBean.setP_t_frame(String.valueOf(m.get("p_t_frame")));
                            mBean.setIsYB(String.valueOf(m.get("isYB")));
                            mBean.setIsYJ(String.valueOf(m.get("isYJ")));

                            lis.onResponse(true, 0, mBean);
                        } else {
                            lis.onResponse(false, -1, "服务端返回数据不正确");
                        }

                    }
                }
            });
        } catch (Exception e) {
            lis.onResponse(false, -1, ErrorText);
        }

    }

    @Override
    public void clientErrorLog(String account, String error_message, String capture_time) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("phone_model", account);
        map.put("error_message", error_message);
        map.put("capture_time", capture_time);
        map.put("phone_model", account);
        map.put("channel", "1");
        map.put("methodName", "clientErrorLog");
        IQuery query = Web.getQuery(LinkInfo.WEBURL + "clientErrorLog.html");

        query.queryPost(map, new OnRespListener() {

            @Override
            public void onResp(int status, Map<String, Object> result, String msg) {

            }

            @Override
            public void onResp(int status, String result, String msg) {


            }
        });
    }

    @Override
    public void updateNickNameByUid(final OnResponseListener lis, String nickName, String userId) {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("nickName", nickName);
        map.put("uid", userId);
        map.put("channel", CHANNEL);
        IQuery query = Web.getQuery(LinkInfo.WEBURL + "updateNickNameByUid.html");

        query.queryPost(map, new OnRespListener() {

            @Override
            public void onResp(int status, Map<String, Object> result, String msg) {

            }

            @Override
            public void onResp(int status, String result, String msg) {


                if (TextUtils.isEmpty(result)) {
                    return;
                }
                JSONObject mJson = JSON.parseObject(result);
                String code = mJson.getString("code");
                String desc = mJson.getString("msg");
                if (TextUtils.equals(code, "0000")) {
                    lis.onResponse(true, 0, desc);
                } else {
                    lis.onResponse(false, -1, desc);
                }

            }
        });

    }


    @Override
    public void notifyTv(int type, int imgeNum, int themeNum, long toID, long tvId) {
        JSONObject json = new JSONObject();
        String fromId = String.valueOf(GlobalHolder.getInstance().mCurrentUserId);
        fromId = fromId.substring(2, fromId.length());
        try {
            json.put("type", type);
            json.put("fromID", fromId);//自己的userId
            json.put("imgeNum", imgeNum);
            json.put("themeNum", themeNum);
            json.put("toID", toID);//第三方的UserId
            json.put("timeStamp", System.currentTimeMillis() + "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        new MessageSendUtil(mcon).sendMessageToRemote(json.toString(),
                GlobalHolder.getInstance().getUser(tvId));//tv的UserId

    }


    @Override
    public void notifyTv(int type, int imgeNum, int themeNum, long toID, long tvId, String url) {
        JSONObject json = new JSONObject();

        String fromId = String.valueOf(GlobalHolder.getInstance().mCurrentUserId);
        fromId = fromId.substring(2, fromId.length());
        try {
            json.put("type", type);
            json.put("fromID", fromId);//自己的userId
            json.put("imgeNum", imgeNum);
            json.put("themeNum", themeNum);
            json.put("pictureUrl", url);
            json.put("toID", toID);//第三方的UserId
            json.put("timeStamp", System.currentTimeMillis() + "");

        } catch (Exception e) {
            e.printStackTrace();
        }

        new MessageSendUtil(mcon).sendMessageToRemote(json.toString(),
                GlobalHolder.getInstance().getUser(tvId));//tv的UserId

    }

    public void mFileUpload(final String filePath, final String fileName, final FileUploadCallBack callBack) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                final String newLine = "\r\n";
//                final String boundaryPrefix = "--";
//                // 定义数据分隔线
//                String BOUNDARY = "========7d4a6d158c9";
//                // 服务器的域名
//                URL url = null;
//                try {
//                    url = new URL("http://tvl.hongguaninfo.com/iptv/service/uploadPic.shtml");
//                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                    // 设置为POST情
//                    conn.setRequestMethod("POST");
//                    // 发送POST请求必须设置如下两行
//                    conn.setDoOutput(true);
//                    conn.setDoInput(true);
//                    conn.setUseCaches(false);
//                    // 设置请求头参数
//                    conn.setRequestProperty("connection", "Keep-Alive");
//                    conn.setRequestProperty("Charsert", "UTF-8");
//                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
        //该头部信息中为请求参数
//                    conn.setRequestProperty("Content-Disposition", "filename=" +
//                            ""+ SystemClock.currentThreadTimeMillis() +
//                            ";channel=" +
//                            "\"android\"" +
//                            ";");
//                    conn.setRequestProperty("Content-Disposition", "filename=\"temp.png\";channel=\"android\";");
//                    OutputStream out = new DataOutputStream(conn.getOutputStream());
//                    // 上传文件
//                    File file = new File(filePath);
//                    if(!file.exists()){
//                        callBack.onFailed("文件不存在");
//                        return;
//                    }else{
//                        String lenth=file.length()+"";
//                        callBack.onStart(Integer.parseInt(lenth));
//                    }
//                    StringBuilder sb = new StringBuilder();
//                    sb.append(boundaryPrefix);
//                    sb.append(BOUNDARY);
//                    sb.append(newLine);
//                    // 文件参数,不可更改
//                    sb.append("Content-Disposition: form-data;name=\"uploadPic\";filename=\"\"" + newLine);
//                    sb.append("Content-Type:application/octet-stream");
//                    // 参数头设置完以后需要两个换行，然后才是参数内容
//                    sb.append(newLine);
//                    sb.append(newLine);
//                    // 将参数头的数据写入到输出流中
//                    out.write(sb.toString().getBytes());
//                    // 数据输入流,用于读取文件数据
//                    DataInputStream in = new DataInputStream(new FileInputStream(
//                            file));
//                    byte[] bufferOut = new byte[1024];
//                    int bytes = 0;
//                    // 每次读1KB数据,并且将文件数据写入到输出流中
//                    while ((bytes = in.read(bufferOut)) != -1) {
//                        out.write(bufferOut, 0, bytes);
//                        callBack.onUpload(bytes);
//                    }
//                    // 最后添加换行
//                    out.write(newLine.getBytes());
//                    in.close();
//                    // 定义最后数据分隔线，即--加上BOUNDARY再加上--。
//                    byte[] end_data = (newLine + boundaryPrefix + BOUNDARY + boundaryPrefix + newLine)
//                            .getBytes();
//                    // 写上结尾标识
//                    out.write(end_data);
//                    out.flush();
//                    out.close();
//
//                    // 定义BufferedReader输入流来读取URL的响应
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(
//                            conn.getInputStream()));
//                    String result = reader.readLine();
////                    if ((result = reader.readLine()) != null) {
////                        result+=result;
////                    }
////                    {"data":"http://tvl.hongguaninfo.com/sendPic/20161115/20161115100348672414.png","code":"0000","success":false,"msg":"上传成功!"}
//                    if (TextUtils.isEmpty(result)) {
//                        callBack.onFailed(String.valueOf("发送图片失败"));
//                        return;
//                    }
//                    JSONObject mJson = JSON.parseObject(result);
//                    String code = mJson.getString("code");
//                    String desc = mJson.getString("msg");
//                    if (TextUtils.equals(code, "0000")) {
//                        callBack.onSuccess(String.valueOf(mJson.get("data")));
//                    } else {
//                        callBack.onFailed(String.valueOf(mJson.get("msg")));
//                    }

//                } catch (Exception e) {
//                    callBack.onFailed(String.valueOf(e.toString()));
//                    e.printStackTrace();
//                }
//            }
//        }).start();

        Map<String, Object> map = new HashMap<String, Object>();
        File file = new File(filePath);
        if (!file.exists()) {
            callBack.onFailed("文件不存在");
            return;
        } else {
            String lenth = file.length() + "";
            callBack.onStart(Integer.parseInt(lenth));
        }
        DataInputStream in = null;
        try {
            in = new DataInputStream(new FileInputStream(file));

            byte[] bytes = new byte[in.available()];
            in.read(bytes);
            String strBase64 = new BASE64Encoder().encode(bytes);      //将字节流数组转换为字符串
            map.put("data", strBase64);
            map.put("fileName", "test.jpg");
            map.put("channel", "android");

        } catch (Exception e) {
            e.printStackTrace();
        }
        IQuery query = Web.getQuery(LinkInfo.WEBURL + "uploadPic.shtml");

        callBack.onStart(100);
        callBack.onUpload(10);
        query.queryPost(map, new OnRespListener() {

            @Override
            public void onResp(int status, Map<String, Object> result, String msg) {

            }

            @Override
            public void onResp(int status, String result, String msg) {


                if (TextUtils.isEmpty(result)) {
                    return;
                }
                JSONObject mJson = JSON.parseObject(result);
                String code = mJson.getString("code");
                String desc = mJson.getString("msg");
                if (TextUtils.equals(code, "0000")) {
                    callBack.onUpload(100);
                    callBack.onSuccess(String.valueOf(mJson.get("data")));
                } else {
                    callBack.onFailed(String.valueOf(mJson.get("msg")));
                }

            }
        });

    }

    @Override
    public void mFileUpload(Bitmap bm, String fileName, final FileUploadCallBack callBack) {
        Map<String, Object> map = new HashMap<String, Object>();
        callBack.onStart(0);
        DataInputStream in = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            InputStream isBm = new ByteArrayInputStream(baos.toByteArray());
            in = new DataInputStream(isBm);
            byte[] bytes = new byte[in.available()];
            in.read(bytes);
            String strBase64 = new BASE64Encoder().encode(bytes);      //将字节流数组转换为字符串
            map.put("data", strBase64);
            map.put("fileName", "test.jpg");
            map.put("channel", "android");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        IQuery query = Web.getQuery(LinkInfo.WEBURL + "uploadPic.shtml");

        callBack.onStart(100);
        callBack.onUpload(10);
        query.queryPost(30 * 1000, map, new OnRespListener() {

            @Override
            public void onResp(int status, Map<String, Object> result, String msg) {

            }

            @Override
            public void onResp(int status, String result, String msg) {


                if (TextUtils.isEmpty(result)) {
                    return;
                }
                JSONObject mJson = JSON.parseObject(result);
                String code = mJson.getString("code");
                String desc = mJson.getString("msg");
                if (TextUtils.equals(code, "0000")) {
                    callBack.onUpload(100);
                    callBack.onSuccess(String.valueOf(mJson.get("data")));
                } else {
                    callBack.onFailed(String.valueOf(mJson.get("msg")));
                }

            }
        });
    }

    @Override
    public void getNickNameByRandom(String userId, SimpleResponseListener<BaseResponse<NickName>> listener) {
        Map<String, String> map = new HashMap<>();
        map.put("uid", userId);
        map.put("channel", CHANNEL);
        GsonRequest<BaseResponse<NickName>> gsonRequest =
                new GsonRequest<>(
                        Request.Method.POST,
                        BuildConfig.HOST + "getNickName.html",
                        map,
                        null,
                        new TypeToken<BaseResponse<NickName>>() {
                        },
                        listener,
                        listener);
        MainApplication.queue.add(gsonRequest);
    }

    @Override
    public void generateChatPassword(String type, String uid, String pwd, final OnResponseListener listener) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("type", type);
        map.put("uid", uid);
        map.put("pwd", pwd);
        map.put("channel", CHANNEL);
        IQuery query = Web.getQuery(LinkInfo.WEBURL + "tvChatPwd.shtml");

        query.queryPost(map, new OnRespListener() {

            @Override
            public void onResp(int status, Map<String, Object> result, String msg) {

            }

            @Override
            public void onResp(int status, String result, String msg) {


                if (TextUtils.isEmpty(result)) {
                    return;
                }
                JSONObject mJson = JSON.parseObject(result);
                String code = mJson.getString("code");
                String desc = mJson.getString("msg");
                Object data = mJson.get("data");
                if (TextUtils.equals(code, "0000")) {
                    listener.onResponse(true, 0, data);
                } else if (TextUtils.equals(code, "0003")) {
                    listener.onResponse(false, -1, "通过用户id未找到用户");
                } else if (TextUtils.equals(code, "0020")) {
                    listener.onResponse(false, -1, "通过聊口令未找到对应用户信息");
                } else if (TextUtils.equals(code, "9999")) {
                    listener.onResponse(false, -1, "系统异常");
                } else {
                    listener.onResponse(false, -1, desc);
                }

            }
        });
    }

    @Override
    public void syncFrendsBatchToTv(String friends, String tvId, String uid, String channel, String type, final OnResponseListener listener) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("tvId", tvId);
        map.put("uid", uid);
        map.put("friends", friends);
        map.put("groupId", "0");
        map.put("channel", CHANNEL);
        map.put("type", type);  //0 是添加  1是删除
        IQuery query = Web.getQuery(LinkInfo.WEBURL + "addFriendsBatch.shtml");

        query.queryPost(map, new OnRespListener() {

            @Override
            public void onResp(int status, Map<String, Object> result, String msg) {

            }

            @Override
            public void onResp(int status, String result, String msg) {


                if (TextUtils.isEmpty(result)) {
                    return;
                }
                JSONObject mJson = JSON.parseObject(result);
                String code = mJson.getString("code");
                String desc = mJson.getString("msg");
                Object data = mJson.get("data");
                if (TextUtils.equals(code, "0000")) {
                    listener.onResponse(true, 0, data);
                } else {
                    listener.onResponse(false, -1, desc);
                }

            }
        });
    }

    /**
     * @param uid      TV UID
     * @param fileName
     * @param data
     * @param channel
     * @param listener
     */
    @Override
    public void updateTvPhoto(String uid, String fileName, String data, String channel, final OnResponseListener listener) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("uid", uid);
        map.put("fileName", fileName);
        map.put("data", data);
        map.put("channel", CHANNEL);
        IQuery query = Web.getQuery(LinkInfo.WEBURL + "uploadUserHeadImg.shtml");

        query.queryPost(map, new OnRespListener() {

            @Override
            public void onResp(int status, Map<String, Object> result, String msg) {

            }

            @Override
            public void onResp(int status, String result, String msg) {

                if (TextUtils.isEmpty(result)) {
                    listener.onResponse(false, -1, "服务器错误");
                    return;
                }
                JSONObject mJson = JSON.parseObject(result);
                String code = mJson.getString("code");
                String desc = mJson.getString("msg");
                if (TextUtils.equals(code, "0000")) {
                    listener.onResponse(true, 0, desc);
                } else {
                    listener.onResponse(false, -1, desc);
                }

            }
        });
    }

    @Override
    public void getUserCenterCallLong(String uid, String channel, SimpleResponseListener<BaseResponse<UserCenterCallLong>> listener) {
        Map<String, String> map = new HashMap<>();
        map.put("uid", uid);
        map.put("channel", CHANNEL);
        GsonRequest<BaseResponse<UserCenterCallLong>> gsonRequest =
                new GsonRequest<>(
                        Request.Method.POST,
                        BuildConfig.HOST + "getUserTouchData.html",
                        map,
                        null,
                        new TypeToken<BaseResponse<UserCenterCallLong>>() {
                        },
                        listener,
                        listener);
        MainApplication.queue.add(gsonRequest);

    }

    @Override
    public void getContactFriends(String accounts, long uid, final OnResponseListener listener) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("accounts", accounts);
        String userId = String.valueOf(uid);
        map.put("uid", userId.substring(2, userId.length()));
        map.put("channel", CHANNEL);
        IQuery query = Web.getQuery(LinkInfo.WEBURL + "queryIsFriend.shtml");
        query.queryPost(map, new OnRespListener() {

            @Override
            public void onResp(int status, Map<String, Object> result, String msg) {

            }

            @Override
            public void onResp(int status, String result, String msg) {
                if (TextUtils.isEmpty(result)) {
                    listener.onResponse(false, -1, "服务器错误");
                    return;
                }
                JSONObject mJson = JSON.parseObject(result);
                String code = mJson.getString("code");
                String desc = mJson.getString("msg");
                if (TextUtils.equals(code, "0000")) {
                    listener.onResponse(true, 0, mJson);
                } else {
                    listener.onResponse(false, -1, desc);
                }

            }
        });
    }

    @Override
    public void closeCourseTime(String userid, String carrange_id,String courseid, String vedioId, SimpleResponseListener<BaseResponse> listener) {
        Map<String, String> map = new HashMap<>();
        map.put("userid", userid);
        map.put("carrange_id", carrange_id);
        map.put("courseid", courseid);
        map.put("vedioId", vedioId);
        map.put("channel", CHANNEL);
        GsonRequest<BaseResponse> gsonRequest =
                new GsonRequest<>(
                        Request.Method.POST,
                        BuildConfig.EDU_HOST + "/class/updateClassArrangeStatus",
                        map,
                        null,
                        new TypeToken<BaseResponse>() {
                        },
                        listener,
                        listener);
        MainApplication.queue.add(gsonRequest);
    }

    @Override
    public void checkUserRole(String account, SimpleResponseListener<BaseResponse<CheckUserRoleResponse>> listener) {
        Map<String, String> map = new HashMap<>();
        map.put("account", account);
        map.put("channel", CHANNEL);
        GsonRequest<BaseResponse<CheckUserRoleResponse>> gsonRequest =
                new GsonRequest<>(
                        Request.Method.POST,
                        BuildConfig.EDU_HOST + "clientInter/queryUserRole",
                        map,
                        null,
                        new TypeToken<BaseResponse<CheckUserRoleResponse>>() {
                        },
                        listener,
                        listener);
        MainApplication.queue.add(gsonRequest);
    }

    @Override
    public void getStudentInfo(String uid, String courseId, SimpleResponseListener<BaseResponse<StudentInfo>> listener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", uid);
        map.put("courseId", courseId);
        map.put("channel", CHANNEL);
        GsonRequest<BaseResponse<StudentInfo>> gsonRequest =
                new GsonRequest<>(
                        Request.Method.POST,
                        BuildConfig.EDU_HOST + "courceArrange/toLession",
                        map,
                        null,
                        new TypeToken<BaseResponse<StudentInfo>>() {
                        },
                        listener,
                        listener);
        MainApplication.queue.add(gsonRequest);
    }


    @Override
    public void getStudentList(String courseId, SimpleResponseListener<BaseResponse<List<StudentInfo2>>> listener) {
        Map<String, String> map = new HashMap<>();
        map.put("courseid", courseId);
        map.put("channel", CHANNEL);
        GsonRequest<BaseResponse<List<StudentInfo2>>> gsonRequest =
                new GsonRequest<>(
                        Request.Method.POST,
                        CourseConfig.GET_STUDENT_LIST
                        ,
                        map,
                        null,
                        new TypeToken<BaseResponse<List<StudentInfo2>>>() {
                        },
                        listener,
                        listener);
        MainApplication.queue.add(gsonRequest);
    }


}




