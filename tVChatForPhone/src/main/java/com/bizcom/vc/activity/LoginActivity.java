package com.bizcom.vc.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.MainApplication;
import com.V2.jni.ConfigRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bizcom.db.V2TechDBHelper;
import com.bizcom.db.V2techBaseProvider;
import com.bizcom.request.V2ImRequest;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.jni.RequestLogInResponse;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.AssertUtils;
import com.bizcom.util.DialogManager;
import com.bizcom.util.FileUtils;
import com.bizcom.util.LocalSharedPreferencesStorage;
import com.bizcom.util.MessageUtil;
import com.bizcom.util.V2Log;
import com.bizcom.util.V2Toast;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.hg.ui.GetPwdActivity;
import com.bizcom.vc.hg.ui.HgRegsterActivity;
import com.bizcom.vc.hg.ui.HomeActivity;
import com.bizcom.vc.hg.util.DataUtil;
import com.bizcom.vc.hg.web.LinkInfo;
import com.bizcom.vc.hg.web.interf.BussinessManger;
import com.bizcom.vc.hg.web.interf.IBussinessManager.OnResponseListener;
import com.bizcom.vo.User;
import com.cgs.utils.ToastUtil;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.shdx.tvchat.phone.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;

//import cn.jpush.android.api.JPushInterface;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("NewApi")
public class LoginActivity extends BaseActivity {
    private boolean isChecked = false;// 控制密码显示
    private static final String TAG = LoginActivity.class.getSimpleName();

    private static final String LOGIN_DEFAULT_PORT = "5123";

    public static final int LOG_IN_CALL_BACK = 1;
    private static final int REG_REQUSETCODE = 77;
    private static final int START_LOGIN = 5;

    private static final int AUTO_TYPE_IP = 2;
    private static final int AUTO_TYPE_PORT = 3;
    private static final int AUTO_TYPE_ACCOUNT = 4;

    private LinearLayout mLlLoginFormLayout;
    private EditText mEtUserName;
    private EditText mEtPassword;
    private ImageView mIvSettingButton;
    private Dialog mSettingDialog;
    private Dialog mVersionUpdateDialog;

    private EtUserNameOnEditorActionListener mEtUserNameOnEditorActionListener = new EtUserNameOnEditorActionListener();
    private EtUserNameOnTouchListener mEtUserNameOnTouchListener = new EtUserNameOnTouchListener();
    private EtPasswordOnEditorActionListener mEtPasswordOnEditorActionListener = new EtPasswordOnEditorActionListener();
    private EtPasswordOnTouchListener mEtPasswordOnTouchListener = new EtPasswordOnTouchListener();

    private RegistButtonOnClickListener mRegistButtonOnClickListener = new RegistButtonOnClickListener();
    private RlLoginLayoutOnClickListener mRlLoginLayoutOnClickListener = new RlLoginLayoutOnClickListener();
    private TvLoginButtonOnClickListener mTvLoginButtonOnClickListener = new TvLoginButtonOnClickListener();
    private OnClickListener mIvSettingButtonOnClickListener = new IvSettingButtonOnClickListener();

    private ConfigRequest mConfigRequest = new ConfigRequest();
    private V2ImRequest mUserService = new V2ImRequest();

    private boolean isLoggingIn;
    private boolean isFward;
    private boolean isAutoLogin;
    private boolean registLogin;

    private SparseArray<List<String>> autoHintDatas = new SparseArray<>();
    private EditText mDialogUserNameTV;
    private EditText mDialogUserPsdTV;

    private View re_problem;
    private TextView mRegistTV;
    private TextView tv_forget_pwd;
    private boolean getAdrOk = false;
    private boolean timeOut = false;
    final boolean isLogOut = MainApplication.isLogout;

    private Dialog mGetServerErrorDialog;

    SharedPreferences preferences;
    String fromTel;

    public static final int PERMISSIN_CAMEAR = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.setNeedAvatar(false);
        super.setNeedBroadcast(true);
        super.setNeedHandler(true);
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences("tvl", MODE_PRIVATE);
        setContentView(R.layout.splash_load);
//        if (Build.VERSION.SDK_INT >= 23) {
//            requestCamearPermission();
//        } else {
        getIp();
        fromTel = getIntent().getStringExtra("fromTel");
        // }


    }

    /**
     * 申请文件读写权限
     * <p/>
     * 3
     */
//    void requestCamearPermission() {
//        int code = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
//        if (code == PackageManager.PERMISSION_GRANTED) {
//            Log.d(TAG, "有拍照权限");
//            getIp();
//            fromTel = getIntent().getStringExtra("fromTel");
//        } else {
//            Log.d(TAG, "没有拍照权限");
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIN_CAMEAR);
//        }
//    }

//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case PERMISSIN_CAMEAR:
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Log.d(TAG, "授权成功");
//                    getIp();
//                    fromTel = getIntent().getStringExtra("fromTel");
//                } else {
//                    Log.d(TAG, "授权失败");
//                }
//                break;
//            default:
//                break;
//        }
//    }
    private void getIp() {
        if (!LocalSharedPreferencesStorage.checkCurrentAviNetwork(mContext)) {
            showGetServerIpDialog();
            return;
        }
        if (TextUtils.isEmpty(LinkInfo.WEBURL)) {
            getServerIp();

        } else {
            start(isLogOut);
        }

    }

    public void getServerIp() {
        mHandler.postDelayed(new Runnable() {
            public void run() {
                if (!getAdrOk) {
                    timeOut = true;
                    showGetServerIpDialog();

                }
            }
        }, 8000);
        BussinessManger.getInstance(mContext).serviceAddr(new OnResponseListener() {

            @Override
            public void onResponse(boolean isSuccess, int what, Object obj) {
                if (timeOut)
                    return;
                if (isSuccess) {
                    getAdrOk = true;
                    JSONArray array = JSON.parseArray(String.valueOf(obj));

                    for (Object o : array) {
                        String addr_code = String.valueOf(((JSONObject) o).get("addr_code"));
                        String server_addr = String.valueOf(((JSONObject) o).get("server_addr"));
                        switch (addr_code) {
                            case "INTER_CODE":
                              //  LinkInfo.WEBURL = server_addr;
                                LinkInfo.WEBURL="http://tvl.hongguaninfo.com/iptv_edu/service/";
                                break;
                            case "VIDEO_CODE":
                                LinkInfo.VIDEO_CODE = server_addr;
                                break;
                            case "H5_CODE":
                                LinkInfo.H5_CODE = server_addr;
                                break;
                            case "QUESTION_CODE":
                                LinkInfo.QUESTION_CODE = server_addr;
                                break;
                            case "HELP_CODE":
                                LinkInfo.HELP_CODE = server_addr;
                                break;
                            case "REGISTER_CODE":
                                LinkInfo.REGISTER_CODE = server_addr;
                                break;
                            case "INTER_CODE_NEW":
//							LinkInfo.WEBURL= server_addr;
                                break;
                            case "isTest":

                                break;
                            case "GET_AD_ETVID_URL":
                                break;
                        }
                    }

                    getMediaEncodeType();
                    start(isLogOut);

                } else {
                    ToastUtil.ShowToast_long(mContext, String.valueOf(obj));
                    finish();
                }
            }

        }, BussinessManger.CHANNEL);
    }

    private void showGetServerIpDialog() {
        mGetServerErrorDialog = DialogManager.getInstance()
                .showNoTitleDialog(DialogManager.getInstance().new DialogInterface(mContext, null,
                        mContext.getText(R.string.error_connect_to_server),
                        mContext.getText(R.string.conversation_quit_dialog_confirm_text),
                        mContext.getText(R.string.login_retry)) {

                    @Override
                    public void confirmCallBack() {
                        mGetServerErrorDialog.dismiss();
                        finish();
                    }

                    @Override
                    public void cannelCallBack() {
                        mGetServerErrorDialog.dismiss();
                        timeOut = false;
                        getAdrOk = false;
                        getIp();
                    }
                });

        mGetServerErrorDialog.show();
    }

    protected void start(boolean isLogOut) {
        if (!isLogOut) {
            isAutoLogin = false;
            mHandler.obtainMessage(START_LOGIN).sendToTarget();
        } else {
            MainApplication.isLogout = false;
            mHandler.obtainMessage(START_LOGIN).sendToTarget();
        }

    }

    @Override
    protected void onDestroy() {
        Log.i("tvliao", "loginActivity-onDestroy");
        mUserService.clearCalledBack();
        if (!isFward) {
            ((MainApplication) getApplication()).uninitForExitProcess();
        }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSettingDialog != null && mSettingDialog.isShowing()) {
            mSettingDialog.dismiss();
        }

    }

    @Override
    public void addBroadcast(IntentFilter filter) {
        filter.addAction(PublicIntent.BROADCAST_REGIST_NOTIFY_TO_LOGIN);
    }

    @Override
    public void receiveBroadcast(Intent intent) {
        String action = intent.getAction();
        if (PublicIntent.BROADCAST_REGIST_NOTIFY_TO_LOGIN.equals(action)) {
            String account = intent.getStringExtra("registAccount");
            String password = intent.getStringExtra("registPassword");
            mEtUserName.setText(account);
            mEtPassword.setText(password);
            attemptLogin(false);
        }
    }

    @Override
    public void receiveMessage(Message msg) {
        switch (msg.what) {
            case LOG_IN_CALL_BACK:
                // 关闭等待对话框
                WaitDialogBuilder.dismissDialog();
                isLoggingIn = false;
                JNIResponse rlr = (JNIResponse) msg.obj;
                if (rlr.getResult() == JNIResponse.Result.TIME_OUT) {
                    V2Toast.makeText(mContext, R.string.error_time_out, V2Toast.LENGTH_LONG).show();
                    GlobalConfig.initConfigFile(true);
                } else if (rlr.getResult() == JNIResponse.Result.FAILED) {
//				mEtPassword.setError(getString(R.string.error_incorrect_password));
                    ToastUtil.ShowToast_long(mContext, getString(R.string.error_incorrect_password).toString());
                    if (mEtPassword != null) {
                        mEtPassword.requestFocus();
                    }
                } else if (rlr.getResult() == JNIResponse.Result.CONNECT_ERROR) {
                    V2Toast.makeText(mContext, R.string.error_connect_to_server, V2Toast.LENGTH_LONG).show();
                } else if (rlr.getResult() == JNIResponse.Result.NO_RESOURCE) {
                    V2Toast.makeText(mContext, R.string.error_no_resource, V2Toast.LENGTH_LONG).show();
                } else if (rlr.getResult() == JNIResponse.Result.LOGED_OVER_TIME) {
                    V2Toast.makeText(mContext, R.string.error_resource_over_time, V2Toast.LENGTH_LONG).show();
                } else if (rlr.getResult() == JNIResponse.Result.SERVER_REJECT) {
                    String defIp = LocalSharedPreferencesStorage.getConfigStrValue(mContext, "ip");
                    String defPort = "8090";
                    final String url = defIp + ":" + defPort + File.separator + "update" + File.separator
                            + "51vcloud_Setup.apk";
                    // getResources().getString(R.string.app_name)
                    V2Log.d(TAG, "build update version url : " + url);
                    String mDialogTitle = getResources().getString(R.string.login_version_update_title);
                    String mDialogContent = String.format(getResources().getString(R.string.login_version_update_content),
                            url);
                    mVersionUpdateDialog = DialogManager.getInstance()
                            .showNormalModeDialog(DialogManager.getInstance().new DialogInterface(mContext, mDialogTitle,
                                    mDialogContent, getResources().getString(R.string.common_confirm_name),
                                    getResources().getString(R.string.common_return_name)) {
                                @Override
                                public void confirmCallBack() {
                                    Intent intent = new Intent();
                                    intent.setAction("android.intent.action.VIEW");
                                    Uri content_url = Uri.parse("http://" + url);
                                    intent.setData(content_url);
                                    startActivity(intent);
                                }

                                @Override
                                public void cannelCallBack() {
                                    mVersionUpdateDialog.dismiss();
                                }
                            });
                    mVersionUpdateDialog.show();
                    // V2Toast.makeText(mContext,
                    // R.string.error_version_to_connect_server,
                    // V2Toast.LENGTH_LONG).show();
                } else if (rlr.getResult() == JNIResponse.Result.LOGED_ORG_DISABLE) {
                    V2Toast.makeText(mContext, R.string.error_org_disable, V2Toast.LENGTH_LONG).show();
                } else if (rlr.getResult() == JNIResponse.Result.LOGED_USER_DISABLE) {
                    V2Toast.makeText(mContext, R.string.error_user_disable, V2Toast.LENGTH_LONG).show();
                } else if (rlr.getResult() == JNIResponse.Result.SUCCESS) {
                    if (mEtUserName != null) {
                        saveUserInfo2File(mEtUserName.getText().toString(), AUTO_TYPE_ACCOUNT);
                    }
                    // 获取到登陆用户对象
                    User user = ((RequestLogInResponse) rlr).getUser();
                    Log.e("user", "user---------->" + user.getmUserId() + " " + user.getAccount() + " " + user.getId());
                    String serverID = ((RequestLogInResponse) rlr).getServerID();
                    if (user == null || serverID == null) {
                        throw new RuntimeException(getResources().getString(R.string.login_error_init_user_id));
                    }
                    GlobalHolder.getInstance().setCurrentUser(user);
                    // 注销所需要初始化的全局变量或值
                    GlobalConfig.isLogined = true;
                    // 构建文件夹路径
                    GlobalConfig.SERVER_DATABASE_ID = serverID;
                    V2Log.d(TAG, "Build folder finish! Globle Path is : " + GlobalConfig.getGlobalPath());
                    // 为登陆用户创建个人资料文件夹
                    createPersonFolder(user);
                    // 如果是游客登陆则不需要保存账号和密码
                    if (mEtUserName != null && mEtPassword != null) {
                        if (LocalSharedPreferencesStorage.getConfigBooleanValue(mContext, "userPwd", true)) {
                            saveUserNameAndPasswd(mEtUserName.getText().toString(), mEtPassword.getText().toString());
                        } else {
                            saveUserNameAndPasswd(mEtUserName.getText().toString(), "");
                        }
                    }
                    // Save user info
                    initDataBaseTableCacheNames();
                    LocalSharedPreferencesStorage.putIntValue(mContext, GlobalConfig.KEY_LOGGED_IN, 1);
                    LocalSharedPreferencesStorage.putBooleanValue(mContext, "isAutoLogin", true);
                    preferences.edit().putBoolean("isLogin", true).commit();
                    if (!registLogin) {
                        mContext.startActivity(new Intent(mContext, HomeActivity.class));
                        // mContext.startAsctivity(new Intent(mContext,
                        // Snippet.class));
                    }
                    isFward = true;
                    finish();
                }

                if (mEtPassword == null || mEtUserName == null)
                    return;
                if (rlr.getResult() != JNIResponse.Result.SUCCESS) {
                    if (!TextUtils.isEmpty(mEtPassword.getText().toString()) && !mEtPassword.getText().toString()
                            .equals(mContext.getResources().getText(R.string.prompt_password))) {
                        mEtPassword.requestFocus();
                        // mEtPassword.setSelection(mEtPassword.getText().length());
                    }
                }

                if (registLogin) {
                    Intent i = new Intent();
                    i.setAction(PublicIntent.BROADCAST_LOGIN_NOTIFY_TO_FINISH_REGIST);
                    i.addCategory(PublicIntent.DEFAULT_CATEGORY);
                    if (isFward) {
                        i.putExtra("isLoginSuccess", true);
                    } else {
                        i.putExtra("isLoginSuccess", false);
                    }

                    sendBroadcast(i);
                    registLogin = false;
                }
                break;
            case START_LOGIN:
                // 判断是否是程序第一次加载
                GlobalConfig.isFirstLauncher = LocalSharedPreferencesStorage.getConfigBooleanValue(mContext,
                        "isFirstLaunch", true);
                if (GlobalConfig.isFirstLauncher) {
                    // 第一次启动,停止推送消息
//				JPushInterface.stopPush(mContext);
                    LocalSharedPreferencesStorage.putBooleanValue(mContext, "isFirstLaunch", false);
                    // 判断该版本是否需要显示引导页
                    // String assertValue = AssertUtils.getAssertValue(mContext,
                    // "guide");
                    // if (assertValue != null &&
                    // assertValue.equals(V2GlobalConstants.SETTINGS_VISIBILE)) {
                    startActivityForResult(new Intent(mContext, WelcomeActivity.class), REG_REQUSETCODE);
                    // }

                } else {
                    boolean isLogin = preferences.getBoolean("isLogin", false);
                    if (isLogin) {
                        String user = LocalSharedPreferencesStorage.getConfigStrValue(this, "user");
                        String password = LocalSharedPreferencesStorage.getConfigStrValue(this, "passwd");
                        String registrationID = JPushInterface.getRegistrationID(mContext);

                        Log.d(TAG, user + " " + password + " " + registrationID);
                        attemptLogin2(user, password, registrationID);
                        return;
                    } else {
                        //退出登录
                        MainApplication.IsInitDataLoadingFinish = false;
                    }

                    showLoginLayout();
                }

                break;
        }
    }

    private void showLoginLayout() {
        setContentView(R.layout.activity_login);
        initView();
        initUserNameAndPassword();
        buildSettingDialog();
        //    mLlLoginFormLayout.setVisibility(View.VISIBLE);

        if (isAutoLogin) {
            attemptLogin(true);
        } else {
            if (!TextUtils.isEmpty(mEtPassword.getText().toString()) && !mEtPassword.getText().toString()
                    .equals(mContext.getResources().getText(R.string.prompt_password))) {
                mEtPassword.requestFocus();
                // mEtPassword.setSelection(mEtPassword.getText().length());
            }
        }

    }

    private void initView() {
        isChecked = false;
        mEtUserName = (EditText) findViewById(R.id.email);

        mEtPassword = (EditText) findViewById(R.id.password);

        mRegistTV = (TextView) findViewById(R.id.login_free_regist);
        mRegistTV.setVisibility(View.VISIBLE);
        mRegistTV.setOnClickListener(mRegistButtonOnClickListener);

        final TextView mTvLogin = (TextView) findViewById(R.id.login_button);
        mTvLogin.setOnClickListener(mTvLoginButtonOnClickListener);

        mIvSettingButton = (ImageView) findViewById(R.id.show_setting);
        mIvSettingButton.setOnClickListener(mIvSettingButtonOnClickListener);

        mEtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    mTvLogin.setEnabled(true);
                } else {
                    mTvLogin.setEnabled(false);
                }
            }
        });


        tv_forget_pwd = (TextView) findViewById(R.id.tv_forget_pwd);

        tv_forget_pwd.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent i = new Intent(mContext, GetPwdActivity.class);
                i.putExtra("titleText", "忘记密码");
                LoginActivity.this.startActivityForResult(i, REG_REQUSETCODE);
            }
        });


        findViewById(R.id.im_showpwd).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mEtPassword.getText() == null || TextUtils.equals(mEtPassword.getText().toString().trim(), ""))
                    return;
                if (!isChecked) {
                    /* show the password */
                    // mEtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    mEtPassword.setInputType(InputType.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_NORMAL);
                    isChecked = true;
                    ((ImageView) findViewById(R.id.im_showpwd)).setImageResource(R.mipmap.open_eye);
                } else {
                    /* hide the password */
                    // mEtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    mEtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    isChecked = false;
                    ((ImageView) findViewById(R.id.im_showpwd)).setImageResource(R.mipmap.close_eye);

                }

            }
        });

    }

    @Override
    public void initViewAndListener() {
    }

    @Override
    public void receiveNewAvatar(User targetUser, Bitmap bnewAvatarm) {

    }

    private void initUserNameAndPassword() {
        if (TextUtils.isEmpty(fromTel)) {
            String user = LocalSharedPreferencesStorage.getConfigStrValue(this, "user");
            if (user != null && !user.trim().isEmpty()) {
                mEtUserName.setText(user);
                mEtUserName.setTextColor(Color.BLACK);
            }

        } else {
            mEtUserName.setText(fromTel);
            mEtUserName.setTextColor(Color.BLACK);
        }

        String password = LocalSharedPreferencesStorage.getConfigStrValue(this, "passwd");
        if (password != null && !password.trim().isEmpty()) {
            //  mEtPassword.setText(password);
            mEtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }

    private void startLlLoginFormLayoutAnimIn() {
        if (re_problem.getVisibility() == View.VISIBLE)
            return;
        re_problem.setVisibility(View.VISIBLE);
        final Animation tabBlockHolderAnimation = AnimationUtils.loadAnimation(this, R.anim.login_container_down_in);
        tabBlockHolderAnimation.setFillAfter(true);
        // 设置动画的一些特效
        // tabBlockHolderAnimation.setInterpolator(new BounceInterpolator());
        re_problem.startAnimation(tabBlockHolderAnimation);
    }

    private void startLlLoginFormLayoutAnimOut() {
        re_problem.setVisibility(View.INVISIBLE);
        final Animation tabBlockHolderAnimation = AnimationUtils.loadAnimation(this, R.anim.login_container_down_out);
        tabBlockHolderAnimation.setFillAfter(true);
        // 设置动画的一些特效
        // tabBlockHolderAnimation.setInterpolator(new BounceInterpolator());
        re_problem.startAnimation(tabBlockHolderAnimation);
    }

    private boolean checkIPorDNS(String str) {
        if (str == null) {
            return false;
        }
        String ValidIpAddressRegex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";

        String ValidHostnameRegex = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.){1,}([A-Za-z][A-Za-z][A-Za-z]*)$";

        return str.matches(ValidIpAddressRegex) || str.matches(ValidHostnameRegex);
    }

    private boolean saveServiceSettingInfo(String ip, String port) {
        return LocalSharedPreferencesStorage.putStrValue(this, new String[]{"ip", "port"},
                new String[]{ip, port});
    }

    private boolean saveUserNameAndPasswd(String user, String passwd) {
        return LocalSharedPreferencesStorage.putStrValue(this, new String[]{"user", "passwd"},
                new String[]{user, passwd});
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin(boolean showProgress) {
        // String ip = LocalSharedPreferencesStorage.getConfigStrValue(this,
        // "ip");
        // String port = LocalSharedPreferencesStorage.getConfigStrValue(this,
        // "port");
        // String ip = "101.200.186.105";
        String ip = LinkInfo.VIDEO_CODE;
        String port = LocalSharedPreferencesStorage.getConfigStrValue(this, "port");
        Log.i("tvliao", "ip-" + ip + "-" + port);

        if (ip == null || ip.isEmpty() || port == null || port.isEmpty()) {
            V2Toast.makeText(mContext, R.string.error_no_host_configuration, V2Toast.LENGTH_SHORT).show();
            mIvSettingButton.performClick();
            return;
        }

        // Reset errors.
        mEtUserName.setError(null);
        mEtPassword.setError(null);

        // Store values at the time of the login attempt.
        String mUserName = mEtUserName.getText().toString();
        String mPassword = mEtPassword.getText().toString();

        // Check user name is initial user name or not.
        if (mContext.getResources().getText(R.string.login_user_name).equals(mUserName)) {
//			mEtUserName.setError(getString(R.string.error_field_required));
            ToastUtil.ShowToast_long(mContext, getString(R.string.error_cannot_empty_phone).toString());
            mEtUserName.requestFocus();
            return;
        }

        // Check password is initial password
        if (mContext.getResources().getText(R.string.prompt_password).equals(mPassword)) {
//			mEtPassword.setError(getString(R.string.error_field_required));
            ToastUtil.ShowToast_long(mContext, getString(R.string.error_field_required).toString());
            mEtPassword.requestFocus();
            return;
        }

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(mUserName)) {
//			mEtUserName.setError(getString(R.string.error_field_required));
            ToastUtil.ShowToast_long(mContext, getString(R.string.error_field_required).toString());
            focusView = mEtUserName;
            cancel = true;
        }

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
//			mEtPassword.setError(getString(R.string.error_field_required));
            ToastUtil.ShowToast_long(mContext, getString(R.string.error_field_required).toString());
            focusView = mEtPassword;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            synchronized (LoginActivity.class) {
                if (isLoggingIn) {
                    V2Log.w("Current state is logging in");
                    return;
                }
                mConfigRequest.setServerAddress(ip, Integer.parseInt(port));

                String registrationID = JPushInterface
                        .getRegistrationID(mContext);
                isLoggingIn = true;
                // Show a progress spinner, and kick off a background task to
                // perform the user login attempt.
                if (showProgress) {
                    registLogin = false;
                    WaitDialogBuilder.showNormalWithHintProgress(mContext,
                            getResources().getString(R.string.login_progress_signing_in));
                } else {
                    registLogin = true;
                }
                String phone = mEtUserName.getText().toString();
                String pwd = mEtPassword.getText().toString();
                mUserService.login(phone, pwd, registrationID, new HandlerWrap(mHandler, LOG_IN_CALL_BACK, null));
            }
        }
    }

    private void attemptLogin2(String username, String pwd, String registionId) {
        String ip = LinkInfo.VIDEO_CODE;
        String port = LocalSharedPreferencesStorage.getConfigStrValue(this, "port");
        if (ip == null || ip.isEmpty() || port == null || port.isEmpty()) {
            V2Toast.makeText(mContext, R.string.error_no_host_configuration, V2Toast.LENGTH_SHORT).show();
            mIvSettingButton.performClick();
            return;
        }
        mConfigRequest.setServerAddress(ip, Integer.parseInt(port));
        mUserService.login(username, pwd, registionId, new HandlerWrap(mHandler, LOG_IN_CALL_BACK, null));
    }

    private void showSoftInput(View v) {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(v, 0);
    }

    // 创建登陆用户存储数据的文件夹
    private void createPersonFolder(User user) {
        GlobalConfig.LOGIN_USER_ID = String.valueOf(user.getmUserId());

        File avatarPath = new File(GlobalConfig.getGlobalUserAvatarPath());
        if (!avatarPath.exists()) {
            boolean res = avatarPath.mkdirs();
            V2Log.i(" create avatar dir " + avatarPath.getAbsolutePath() + "  " + res);
        }

        File image = new File(GlobalConfig.getGlobalPicsPath());
        if (!image.exists()) {
            boolean res = image.mkdirs();
            V2Log.i(" create image dir " + image.getAbsolutePath() + "  " + res);
        }

        File audioPath = new File(GlobalConfig.getGlobalAudioPath());
        if (!audioPath.exists()) {
            boolean res = audioPath.mkdirs();
            V2Log.i(" create audio dir " + audioPath.getAbsolutePath() + "  " + res);
        }

        File filePath = new File(GlobalConfig.getGlobalFilePath());
        if (!filePath.exists()) {
            boolean res = filePath.mkdirs();
            V2Log.i(" create file dir " + filePath.getAbsolutePath() + "  " + res);
        }

    }

    private Dialog buildSettingDialog() {
        if (mSettingDialog != null)
            return mSettingDialog;
        final Dialog dialog = new Dialog(mContext, R.style.IpSettingDialog);
        dialog.setContentView(R.layout.ip_setting);

        Button cancelButton = (Button) dialog.findViewById(R.id.ip_setting_cancel);
        Button saveButton = (Button) dialog.findViewById(R.id.ip_setting_save);

        mDialogUserPsdTV = (EditText) dialog.findViewById(R.id.ip);
        mDialogUserNameTV = (EditText) dialog.findViewById(R.id.port);

        String defIp = LocalSharedPreferencesStorage.getConfigStrValue(mContext, "ip");
        if (TextUtils.isEmpty(defIp)) {
            // 获取配置文件中默认ip
            defIp = AssertUtils.getAssertValue(mContext, "ip");
            saveServiceSettingInfo(defIp, LOGIN_DEFAULT_PORT);
        }
        mDialogUserPsdTV.setText(defIp);

        // if (!TextUtils.isEmpty(defIp))
        // mDialogUserPsdTV.setSelection(defIp.length());

        String defPort = LocalSharedPreferencesStorage.getConfigStrValue(mContext, "port");
        if (TextUtils.isEmpty(defPort)) {
            defPort = LOGIN_DEFAULT_PORT;
            saveServiceSettingInfo(defIp, defPort);
        }
        mDialogUserNameTV.setText(defPort);
        //
        // if (!TextUtils.isEmpty(defPort))
        // mDialogUserNameTV.setSelection(defPort.length());
        saveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String ets = mDialogUserPsdTV.getText().toString();
                String portStr5 = mDialogUserNameTV.getText().toString();
                if (!checkIPorDNS(ets)) {
//					mDialogUserPsdTV.setError(mContext.getText(R.string.error_host_invalid));
                    ToastUtil.ShowToast_long(mContext, getString(R.string.error_host_invalid).toString());
                    mDialogUserPsdTV.requestFocus();
                    return;
                }
                if (portStr5.isEmpty()) {
//					mDialogUserNameTV.setError(mContext.getText(R.string.error_field_required));
                    ToastUtil.ShowToast_long(mContext, getString(R.string.error_field_required).toString());
                    mDialogUserNameTV.requestFocus();
                    return;
                }

                saveUserInfo2File(ets, AUTO_TYPE_IP);
                saveUserInfo2File(portStr5, AUTO_TYPE_PORT);
                if (!saveServiceSettingInfo(ets, portStr5)) {
                    V2Toast.makeText(mContext, R.string.error_save_host_config, V2Toast.LENGTH_LONG).show();
                } else {
                    V2Toast.makeText(mContext, R.string.succeed_save_host_config, V2Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogUserPsdTV.setText(LocalSharedPreferencesStorage.getConfigStrValue(mContext, "ip"));
                mDialogUserNameTV.setText(LocalSharedPreferencesStorage.getConfigStrValue(mContext, "port"));
                mDialogUserPsdTV.setError(null);
                mDialogUserNameTV.setError(null);
                MessageUtil.hideKeyBoard(mContext, v.getWindowToken());
                dialog.dismiss();
            }
        });

        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        mSettingDialog = dialog;
        return dialog;
    }

    /**
     * 初始化获取数据库中所有表
     */
    private void initDataBaseTableCacheNames() {
        if (V2techBaseProvider.mSQLitDatabaseHolder != null) {
            V2techBaseProvider.mSQLitDatabaseHolder.close();
        }
        V2TechDBHelper.clearAll();
        V2techBaseProvider.init(getApplicationContext());
        V2techBaseProvider.initDataBaseCache();
    }

    private void saveUserInfo2File(String content, int autoType) {
        List<String> oldContents = autoHintDatas.get(autoType);
        boolean isExist = false;

        if (oldContents != null) {
            if (oldContents.contains(content)) {
                isExist = true;
            } else {
                oldContents.add(content);
            }
        } else {
            oldContents = new ArrayList<>();
            oldContents.add(content);
            autoHintDatas.put(autoType, oldContents);
        }

        if (!isExist) {
            switch (autoType) {
                case AUTO_TYPE_ACCOUNT:
                    FileUtils.save2File(buildFilePath("account"), content + "=");
                    break;
                case AUTO_TYPE_IP:
                    FileUtils.save2File(buildFilePath("ip"), content + "=");
                    break;
                case AUTO_TYPE_PORT:
                    FileUtils.save2File(buildFilePath("port"), content + "=");
                    break;
            }
        }
    }

    private String buildFilePath(String type) {
        String parentPath = mContext.getCacheDir().getAbsolutePath();
        return parentPath + "/" + type + ".txt";
    }

    private void prepareAutoHitDatas(AutoCompleteTextView autoEt, int autoType) {
        String target = null;
        switch (autoType) {
            case AUTO_TYPE_ACCOUNT:
                target = buildFilePath("account");
                break;
            case AUTO_TYPE_IP:
                target = buildFilePath("ip");
                break;
            case AUTO_TYPE_PORT:
                target = buildFilePath("port");
                break;
        }
        String fileConent = FileUtils.getFileConent(target);
        if (!TextUtils.isEmpty(fileConent)) {
            String[] contents = fileConent.split("=");
            List<String> temp = new ArrayList<>();
            for (int i = 0; i < contents.length; i++) {
                temp.add(contents[i]);
            }

            autoHintDatas.put(autoType, temp);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.common_auto_textview_item, contents);// 配置Adaptor
            autoEt.setAdapter(adapter);
        }
    }

    private class EtUserNameOnEditorActionListener implements OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView tv, int actionId, KeyEvent event) {
            mEtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            return false;
        }

    }

    private class EtUserNameOnTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mEtUserName.requestFocus();
                mEtUserName.setError(null);
            }
            return false;
        }
    }

    private class EtPasswordOnTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mEtPassword, InputMethodManager.SHOW_FORCED);
                EditText et = ((EditText) view);

                et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                et.requestFocus();
                et.setError(null);
            }
            return false;

        }
    }

    private class EtPasswordOnEditorActionListener implements OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEtUserName.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(mEtPassword.getWindowToken(), 0);
                attemptLogin(true);
            }
            return false;
        }

    }

    private class RlLoginLayoutOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mEtUserName.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(mEtPassword.getWindowToken(), 0);
            mEtPassword.setError(null);
            mEtUserName.setError(null);
        }
    }

    private class TvLoginButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mEtUserName.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(mEtPassword.getWindowToken(), 0);
            attemptLogin(true);

        }
    }

    private class RegistButtonOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            Intent i = new Intent(mContext, HgRegsterActivity.class);
            i.putExtra("titleText", "注册");
            LoginActivity.this.startActivityForResult(i, REG_REQUSETCODE);
        }
    }

    private class IvSettingButtonOnClickListener implements OnClickListener {

        @Override
        public void onClick(final View vButton) {
            if (mSettingDialog != null) {
                // mDialogUserNameTV.setSelection(mDialogUserNameTV.getText().length());
                // mDialogUserPsdTV.setSelection(mDialogUserPsdTV.getText().length());
                mSettingDialog.show();
                return;
            }

            Dialog dialog = buildSettingDialog();
            dialog.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REG_REQUSETCODE) {
            switch (resultCode) {
                case HgRegsterActivity.REGISTEROK:
                    String phone = data.getStringExtra("phone");
                    String pwd = data.getStringExtra("pwd");
                    mEtUserName.setText(phone);
                    mEtPassword.setText(pwd);
                    ToastUtil.ShowToast_long(mContext, "注册成功，立即体验");
                    // attemptLogin(true);
                    break;
                case GetPwdActivity.GetPwd:

                    String phone2 = data.getStringExtra("phone");
                    String pwd2 = data.getStringExtra("pwd");
                    mEtUserName.setText(phone2);
                    mEtPassword.setText(pwd2);
                    ToastUtil.ShowToast_long(mContext, "密码修改成功");
                    // attemptLogin(true);
                    break;
                case WelcomeActivity.WELCOMEOK:
                    showLoginLayout();
                    // attemptLogin(true);
                    break;

            }
        }
    }

    private void getMediaEncodeType() {
        BussinessManger.getInstance(this).getMediaEncodeType(new OnResponseListener() {

            @Override
            public void onResponse(boolean isSuccess, int what, Object obj) {
                if (isSuccess) {
                    // 记录获取是否失败 获取失败的话 在启动要重新get一次
                    LocalSharedPreferencesStorage.putBooleanValue(mContext, "getMediaEncodeTypeOK", true);

                    DataUtil.saveData(obj, "MediaEncodeType", mContext);
                } else {
                    LocalSharedPreferencesStorage.putBooleanValue(mContext, "getMediaEncodeTypeOK", false);

                    // ToastUtil.ShowToast_long(mContext, String.valueOf(obj));
                }

            }
        }, android.os.Build.MODEL);

    }

}
