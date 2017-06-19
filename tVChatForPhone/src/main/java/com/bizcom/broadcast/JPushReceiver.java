package com.bizcom.broadcast;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.MainApplication;
import com.bizcom.util.Notificator;
import com.bizcom.util.V2Log;
import com.bizcom.vc.activity.LoginActivity;
import com.bizcom.vc.hg.ui.HomeActivity;
import com.bizcom.vo.JpushBean;
import com.config.GlobalConfig;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.google.gson.Gson;
import com.shdx.tvchat.phone.R;

import cn.jpush.android.api.JPushInterface;

public class JPushReceiver extends BroadcastReceiver {
    private static final String TAG = "JPush";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        V2Log.d(TAG, "[JPushReceiver] onReceive - " + intent.getAction() + ", extras: " + printBundle(bundle));
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            V2Log.d(TAG, "[JPushReceiver] 接收Registration Id : " + regId);
            // send the Registration Id to your server...
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            V2Log.d(TAG, "[JPushReceiver] 接收到推送下来的自定义消息");
            //"loc-key":"EPUSHMSGTYPE_CHAT"
            String jsonText = bundle.getString(JPushInterface.EXTRA_MESSAGE);
            if (!TextUtils.isEmpty(jsonText)) {
                jsonText = jsonText.replaceAll("-", "_");
                Gson gson = new Gson();
                try {
                    JpushBean jpushBean = gson.fromJson(jsonText, JpushBean.class);
                    if (jpushBean != null) {
                        Resources res = context.getResources();
                        String mTitleContent = res.getText(R.string.app_name).toString();
                        String mShowContent = "";
                        if (jpushBean.loc_key.contains(V2GlobalConstants.JPUSH_MESSAGE_CHAT)) {
                            mShowContent = res.getText(R.string.jpush_message_content).toString();
                        } else if (jpushBean.loc_key.contains(V2GlobalConstants.JPUSH_AUDIO_CHAT)) {
                            mShowContent = jpushBean.loc_args[0] + res.getText(R.string.jpush_audio_content).toString();
                        } else if (jpushBean.loc_key.contains(V2GlobalConstants.JPUSH_VIDEO_CHAT)) {
                            mShowContent = jpushBean.loc_args[0] + res.getText(R.string.jpush_video_content).toString();
                        } else if (jpushBean.loc_key.contains(V2GlobalConstants.JPUSH_CONF_CHAT)) {
                            mShowContent = jpushBean.loc_args[0] + res.getText(R.string.jpush_conf_content).toString();
                        }

                        Intent resultIntent = new
                                Intent(PublicIntent.START_LOGION_ACTIVITY);
                        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Notificator.updateSystemNotification(context,
                                mTitleContent, mShowContent, true,
                                resultIntent, V2GlobalConstants.MESSAGE_NOTIFICATION_ID, 0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
            V2Log.d(TAG, "[JPushReceiver] 接收到推送下来的通知的ID: " + notifactionId);
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            V2Log.d(TAG, "[JPushReceiver] 用户点击打开了通知");
            if (!MainApplication.isAlreadLogin) {
                NotificationManager mNotificationManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancelAll();
                Intent i = new Intent();
                if (GlobalConfig.isLogined) {
                    i.setClass(context, HomeActivity.class);
                } else {
                    i.setClass(context, LoginActivity.class);
                }

                i.putExtras(bundle);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(i);
            }
        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
            V2Log.d(TAG, "[JPushReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
            // 在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity，
            // 打开一个网页等..
        } else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
            boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
            Log.w(TAG, "[JPushReceiver]" + intent.getAction() + " connected state change to " + connected);
        } else {
            Log.d(TAG, "[JPushReceiver] Unhandled intent - " + intent.getAction());
        }
    }

    // 打印所有的 intent extra 数据
    private static String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
            } else if (key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
            } else {
                sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
            }
        }
        return sb.toString();
    }

    // /**
    // * send msg to MainActivity
    // *
    // * @param context
    // * @param bundle
    // */
    // private void processCustomMessage(Context context, Bundle bundle) {
    // if (MainActivity.isForeground) {
    // String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
    // String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
    // Intent msgIntent = new Intent(MainActivity.MESSAGE_RECEIVED_ACTION);
    // msgIntent.putExtra(MainActivity.KEY_MESSAGE, message);
    // if (!ExampleUtil.isEmpty(extras)) {
    // try {
    // JSONObject extraJson = new JSONObject(extras);
    // if (null != extraJson && extraJson.length() > 0) {
    // msgIntent.putExtra(MainActivity.KEY_EXTRAS, extras);
    // }
    // } catch (JSONException e) {
    //
    // }
    //
    // }
    // context.sendBroadcast(msgIntent);
    // }
    // }
}
