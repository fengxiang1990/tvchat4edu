package com.bizcom.vc.hg.ui.edu;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.jni.RequestEnterConfResponse;
import com.bizcom.util.V2Log;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.hg.ui.edu.bridgehandler.BackHandler;
import com.bizcom.vc.hg.ui.edu.bridgehandler.BridgeMethd;
import com.bizcom.vc.hg.ui.edu.bridgehandler.CreateConferenceHandler;
import com.bizcom.vc.hg.ui.edu.bridgehandler.JoinConferenceHandler;
import com.bizcom.vc.hg.ui.edu.bridgehandler.OpenNewWebHandler;
import com.bizcom.vc.hg.ui.edu.bridgehandler.RefreshEduPage;
import com.bizcom.vc.hg.ui.edu.bridgehandler.RemoveStudentHandler;
import com.bizcom.vc.hg.ui.edu.bridgehandler.ShareHandler;
import com.bizcom.vc.hg.ui.edu.bridgehandler.ToastHandler;
import com.bizcom.vc.hg.ui.edu.bridgehandler.UserInfoHandler;
import com.bizcom.vo.Conference;
import com.bizcom.vo.ConferenceConversation;
import com.bizcom.vo.Conversation;
import com.bizcom.vo.Group;
import com.bizcom.vo.User;
import com.cgs.utils.ToastUtil;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.BridgeWebViewClient;
import com.google.gson.Gson;
import com.shdx.tvchat.phone.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017/1/16.
 */

public abstract class WebActivity extends Activity implements BridgeMethd {

    String tag = "WebActivity";
    BridgeWebView webView;
    ImageView imageView;
    List<Conversation> mItemList = new ArrayList<>();

    CreateConferenceHandler createConferenceHandler;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = getWebView();
        imageView = getImageView();
        createConferenceHandler = new CreateConferenceHandler(this, handler);
        if (webView != null) {
            //  webView.setDefaultHandler(new DefaultHandler());
            user = GlobalHolder.getInstance().getCurrentUser();
            Log.e(tag, "user-->" + user.toString());
            webView.registerHandler(GET_USER_INFO, new UserInfoHandler(user));
            webView.registerHandler(REFRESH_PAGE, new RefreshEduPage(this));
            webView.registerHandler(REMOVE_STUDENT, new RemoveStudentHandler(this));
            webView.registerHandler(SHOW_TOAST, new ToastHandler(this));
            webView.registerHandler(BACK, new BackHandler(this));
            webView.registerHandler(SHARE, new ShareHandler(this));
            webView.registerHandler(OPEN_NEW_WEBVIEW, new OpenNewWebHandler(this));
            webView.registerHandler(CREATE_CONFERENCE, createConferenceHandler);
            webView.registerHandler(JOIN_CONFERENCE, new JoinConferenceHandler(this, handler));
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                    //ToastUtil.ShowToast_short(WebActivity.this,message);
                    return false;
                }
            });
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            webView.setWebViewClient(new BridgeWebViewClient(webView) {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    WaitDialogBuilder.dismissDialog();
                    if (!isError) {
                        webView.setVisibility(View.VISIBLE);
                        imageView.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    WaitDialogBuilder.showNormalWithHintProgress(WebActivity.this);
                    isError = false;
                }

                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    super.onReceivedError(view, errorCode, description, failingUrl);
                    webView.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    isError = true;
                }
            });
        }
    }

    boolean isError = false;

    protected abstract BridgeWebView getWebView();

    protected abstract ImageView getImageView();

    private void startConferenceActivity(Conference conf) {
        // Set current state to in meeting state
        GlobalHolder.getInstance().setMeetingState(true, conf.getId());
        Intent enterConference = new Intent(WebActivity.this, ConferenceActivity2.class);
        //Intent enterConference = new Intent(mContext, MyConferenceActivity.class);
        enterConference.putExtra("conf", conf);
        enterConference.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivity(enterConference);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ConferenceHelper.REQUEST_ENTER_CONF_RESPONSE:
                    WaitDialogBuilder.dismissDialog();
                    JNIResponse response = (JNIResponse) msg.obj;
                    if (response.getResult() == JNIResponse.Result.SUCCESS) {
                        RequestEnterConfResponse recr = (RequestEnterConfResponse) msg.obj;
                        Conference c = recr.getConf();
                        if (c != null && createConferenceHandler.function != null) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("carrange_id", CourseInfoSingleton.getCourseInfo().getCarrange_id());
                            map.put("conference", c);
                            String cJson = new Gson().toJson(map);
                            Log.e(tag, "send Conference Json to js-->" + cJson);
                            createConferenceHandler.function.onCallBack(cJson);
                        }
                        startConferenceActivity(c);
                    } else {
                        V2Log.e(tag, "Request enter conf response , code is : " + response.getResult().name());
                        int errResId;
                        if (response.getResult() == RequestEnterConfResponse.Result.ERR_CONF_LOCKDOG_NORESOURCE) {
                            errResId = R.string.error_request_enter_conference_no_resource;
                        } else if (response.getResult() == RequestEnterConfResponse.Result.ERR_CONF_NO_EXIST) {
                            errResId = R.string.error_request_enter_conference_not_exist;
                        } else if (response.getResult() == RequestEnterConfResponse.Result.TIME_OUT) {
                            errResId = R.string.error_request_enter_conference_time_out;
                        } else {
                            errResId = R.string.error_request_enter_conference_time_out;
                        }
                        Toast.makeText(WebActivity.this, errResId, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case ConferenceHelper.CREATE_CONFERENC_RESP:
                    JNIResponse rccr = (JNIResponse) msg.obj;
                    // Conference conf = (Conference) rccr.callerObject;
                    if (rccr.getResult() != JNIResponse.Result.SUCCESS) {
                        WaitDialogBuilder.dismissDialog();
                        V2Log.e("ConferenceCreateActivity --> CREATE FAILED ... ERROR CODE IS : "
                                + rccr.getResult().name());
                        if (rccr.getResult() == JNIResponse.Result.ERR_CONF_LOCKDOG_NORESOURCE)
                            ToastUtil.ShowToast_short(WebActivity.this, getString(R.string.error_no_resource));
                        else if (rccr.getResult() == JNIResponse.Result.TIME_OUT) {
                            Log.e(tag, getString(R.string.error_time_out_create_conference_failed));
                            ToastUtil.ShowToast_short(WebActivity.this, getString(R.string.error_time_out_create_conference_failed));
                        } else {
                            ToastUtil.ShowToast_short(WebActivity.this, getString(R.string.error_create_conference_failed_from_server_side));
                        }
                        break;
                    } else {
                        Log.e(tag, "ConferenceCreateActivity --> Create New Meeting Successfully!!");
                        enter();
                    }
            }
        }
    };


    void enter() {
        initConference();
        if (mItemList.size() > 0) {
            Conversation conversation = mItemList.get(0);
            ConferenceHelper.getConferenceHelper().requestEnterConference(conversation.getExtId(), handler);
            return;
        }
    }

    void initConference() {
        List<Group> gl = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_CONFERENCE);
        if (gl != null && gl.size() > 0) {
            populateConversation(gl);
        }
    }

    /**
     * According populateType to fill the List Data. The data from server!
     *
     * @param list
     */
    private void populateConversation(List<Group> list) {
        mItemList.clear();
        for (int i = list.size() - 1; i >= 0; i--) {
            Group g = list.get(i);
            if (TextUtils.isEmpty(g.getName())) {
                Log.e(tag, "Recv bad conference , no name , id : " + g.getGroupID());
                continue;
            } else {
                Log.e(tag, "Recv new conference, name: " + g.getName() + " | id : " + g.getGroupID());
            }
            Conversation cov = new ConferenceConversation(g, false);
            mItemList.add(cov);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView != null) {
                if (webView.canGoBack()) {
                    webView.goBack();
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
