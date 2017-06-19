package com.bizcom.vc.hg.ui.edu;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.jni.RequestConfCreateResponse;
import com.bizcom.request.jni.RequestEnterConfResponse;
import com.bizcom.request.jni.RequestExitedConfResponse;
import com.bizcom.util.V2Log;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.hg.ui.edu.bridgehandler.BridgeMethd;
import com.bizcom.vc.hg.ui.edu.bridgehandler.CreateConferenceHandler;
import com.bizcom.vc.hg.ui.edu.bridgehandler.JoinConferenceHandler;
import com.bizcom.vc.hg.ui.edu.bridgehandler.OpenNewWebHandler;
import com.bizcom.vc.hg.ui.edu.bridgehandler.ToastHandler;
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
 * Created by admin on 2017/1/12.
 */

public class CourseFragment extends Fragment implements BridgeMethd {

    String TAG = "CourseFragment";

    FrameLayout screen;
    BridgeWebView mWeb;
    ImageView imageView;
    CreateConferenceHandler createConferenceHandler;
    JoinConferenceHandler joinConferenceHandler;
    public String tag;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    User user;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView");
        //  LinearLayout view = (LinearLayout) inflater.inflate(R.layout.course_fragment, container, false);
        screen = new FrameLayout(getActivity());
        mWeb = new BridgeWebView(getActivity());
        imageView = new ImageView(getActivity());
        imageView.setImageResource(R.mipmap.wifi);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        screen.addView(mWeb);
        screen.addView(imageView, params);
        imageView.setVisibility(View.GONE);

        createConferenceHandler = new CreateConferenceHandler(getActivity(), handler);
        joinConferenceHandler = new JoinConferenceHandler(getActivity(), handler);
        user = GlobalHolder.getInstance().getCurrentUser();
        Log.e(TAG, "user-->" + user.toString());
        // userInfoHandler = new UserInfoHandler(user);
        // mWeb.registerHandler(GET_USER_INFO, userInfoHandler);
        mWeb.registerHandler(SHOW_TOAST, new ToastHandler(getActivity()));
        mWeb.registerHandler(CREATE_CONFERENCE, createConferenceHandler);
        mWeb.registerHandler(JOIN_CONFERENCE, joinConferenceHandler);
        mWeb.registerHandler(OPEN_NEW_WEBVIEW, new OpenNewWebHandler(getActivity()));
        mWeb.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return false;
            }
        });
        mWeb.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mWeb.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWeb.getSettings().setDomStorageEnabled(true);
        if (tag != null && tag.equals("tab1")) {

            loadWeb();

        }

        mWeb.setWebViewClient(new BridgeWebViewClient(mWeb) {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                mWeb.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                isError = true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                isError = false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!isError) {
                    mWeb.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.GONE);
                }
            }
        });
        return screen;
    }

    boolean isError = false;

    @Override
    public void onResume() {
        super.onResume();
        //  mWeb.registerHandler(GET_USER_INFO, userInfoHandler);

    }

    public void reload() {
        try {
            mWeb.reload();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadWeb() {
        Log.e(TAG, "loadWeb -->" + tag);
        try {
            if (tag != null && tag.equals("tab1")) {
                String url1 = CourseConfig.COUSE_TAB1 + "/" + user.getTvlUid();
                Log.e(TAG, "url1 -->" + url1);
                mWeb.loadUrl(url1);
            } else if (tag != null && tag.equals("tab2")) {
                mWeb.loadUrl(CourseConfig.COUSE_TAB2 + "/" + user.getTvlUid());
            } else if (tag != null && tag.equals("tab3")) {
                mWeb.loadUrl(CourseConfig.COUSE_TAB3 + "/" + user.getTvlUid());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                            Log.e(TAG, "send Conference Json to js-->" + cJson);
                            createConferenceHandler.function.onCallBack(cJson);
                        }
                        startConferenceActivity(c);

                    } else {
                        V2Log.e(TAG, "Request enter conf response , code is : " + response.getResult().name());
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
                        Toast.makeText(getActivity(), errResId, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case ConferenceHelper.QUIT_CONFERENC_RESP:
                    Log.e(tag, "quit conference");
                    JNIResponse rccr2 = (JNIResponse) msg.obj;
                    JNIResponse.Result result = rccr2.getResult();
                    Log.e(tag, "quit resp-->" + result.toString());
                    break;
                case ConferenceHelper.REQUEST_EXIT_CONF:
                    Log.e(tag, "exit conference");
                    JNIResponse rccr3 = (JNIResponse) msg.obj;
                    JNIResponse.Result result3 = rccr3.getResult();
                    RequestExitedConfResponse requestExitedConfResponse = (RequestExitedConfResponse) rccr3;
                    Log.e(tag, "exit resp-->" + result3.toString());
                    if (result3 == JNIResponse.Result.SUCCESS) {
                        ConferenceHelper.getConferenceHelper().requestEnterConference(requestExitedConfResponse.nConfID, handler);
                    }
                    break;
                case ConferenceHelper.CREATE_CONFERENC_RESP:
                    JNIResponse rccr = (JNIResponse) msg.obj;
                    RequestConfCreateResponse requestConfCreateResponse = null;
                    if (rccr instanceof RequestConfCreateResponse) {
                        requestConfCreateResponse = (RequestConfCreateResponse) rccr;
                    }
                    if (requestConfCreateResponse == null) {
                        Log.e(TAG, "requestConfCreateResponse  is null");
                        ToastUtil.ShowToast_short(getActivity(), getString(R.string.error_create_conference_failed_from_server_side));
                        return;
                    }
                    if (rccr.getResult() != JNIResponse.Result.SUCCESS) {
                        WaitDialogBuilder.dismissDialog();
                        V2Log.e("ConferenceCreateActivity --> CREATE FAILED ... ERROR CODE IS : "
                                + rccr.getResult().name());
                        if (rccr.getResult() == JNIResponse.Result.ERR_CONF_LOCKDOG_NORESOURCE)
                            ToastUtil.ShowToast_short(getActivity(), getString(R.string.error_no_resource));
                        else if (rccr.getResult() == JNIResponse.Result.TIME_OUT) {
                            Log.e(TAG, getString(R.string.error_time_out_create_conference_failed));
                            ToastUtil.ShowToast_short(getActivity(), getString(R.string.error_time_out_create_conference_failed));
                        } else {
                            ToastUtil.ShowToast_short(getActivity(), getString(R.string.error_create_conference_failed_from_server_side));
                        }
                        ConferenceHelper.getConferenceHelper().requestExitConference(requestConfCreateResponse.getConfId(), handler);
                        break;
                    } else {
                        Log.e(TAG, "ConferenceCreateActivity --> Create New Meeting Successfully!!");
                        ConferenceHelper.getConferenceHelper().requestExitConference(requestConfCreateResponse.getConfId(), handler);
                    }
            }
        }
    };


    List<Conversation> mItemList = new ArrayList<>();

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
                V2Log.e(tag, "Recv bad conference , no name , id : " + g.getGroupID());
                continue;
            } else {
                V2Log.d(tag, "Recv new conference, name: " + g.getName() + " | id : " + g.getGroupID());
            }
            Conversation cov = new ConferenceConversation(g, false);
            mItemList.add(cov);
        }
    }


    private void startConferenceActivity(Conference conf) {
        // Set current state to in meeting state
        GlobalHolder.getInstance().setMeetingState(true, conf.getId());
        Intent enterConference = new Intent(getActivity(), ConferenceActivity2.class);
        enterConference.putExtra("conf", conf);
        //enterConference.addCategory(Intent.CATEGORY_HOME);
        // enterConference.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        enterConference.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivity(enterConference);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        try {
            if (mWeb != null) {
                mWeb.destroy();
                mWeb.clearHistory();
                mWeb.removeAllViews();
                mWeb = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}