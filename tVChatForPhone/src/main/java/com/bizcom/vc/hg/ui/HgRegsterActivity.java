package com.bizcom.vc.hg.ui;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.V2.jni.ConfigRequest;
import com.bizcom.db.V2TechDBHelper;
import com.bizcom.db.V2techBaseProvider;
import com.bizcom.request.V2ImRequest;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.jni.RequestLogInResponse;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.AlertMsgUtils;
import com.bizcom.util.FileUtils;
import com.bizcom.util.LocalSharedPreferencesStorage;
import com.bizcom.util.V2Log;
import com.bizcom.util.V2Toast;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.activity.BaseActivity;
import com.bizcom.vc.activity.LoginActivity;
import com.bizcom.vc.hg.view.HeadLayoutManagerHG;
import com.bizcom.vc.hg.web.LinkInfo;
import com.bizcom.vc.hg.web.MD5;
import com.bizcom.vc.hg.web.interf.BussinessManger;
import com.bizcom.vc.hg.web.interf.IBussinessManager;
import com.bizcom.vc.hg.web.interf.IBussinessManager.OnResponseListener;
import com.bizcom.vo.User;
import com.cgs.utils.ToastUtil;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.shdx.tvchat.phone.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class HgRegsterActivity extends BaseActivity implements TextWatcher {
    public static final int REGISTEROK = -9767;

    private boolean isChecked1 = false;// 控制密码显示
    private boolean isChecked2;// 控制密码显示

    private CheckBox cb1;
    private String titleText;
    private HgRegsterActivity mContext;
    private HeadLayoutManagerHG mHeadLayoutManager;
    private Button btGetQcode;
    private EditText etPwd1;
    private EditText etUsername;
    private EditText etNickname;
    private EditText etPwd2;
    private EditText etQcode;
    private ImageView showPwd1;
    private ImageView showPwd2;
    private TextView login_button;
    private IBussinessManager service;
    private TimeCount timer;
    private View ll_statement;
    private String qCodeStr = "";// 保存验证码 本地校验
    private long lastGetQcodeTime = 0;// 验证码发送的时间
    public static final String pwdPatten = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$";

    boolean isDebug = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.setNeedHandler(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pwd);
        mContext = this;
        service = BussinessManger.getInstance(mContext);
        initView();
    }

    @Override
    public void addBroadcast(IntentFilter filter) {

    }

    @Override
    public void receiveBroadcast(Intent intent) {

    }

    private void initView() {
        titleText = getIntent().getStringExtra("titleText");
        mHeadLayoutManager = new HeadLayoutManagerHG(mContext, findViewById(R.id.head_layout), false);
        mHeadLayoutManager.updateTitle(titleText);
        etUsername = (EditText) findViewById(R.id.et_username);
        etNickname = (EditText) findViewById(R.id.et_nickname);
        etPwd1 = (EditText) findViewById(R.id.password1);
        etPwd2 = (EditText) findViewById(R.id.password2);
        cb1 = (CheckBox) findViewById(R.id.cb1);
        etPwd1.setHint("请输入密码");
        etPwd2.setHint("请确认密码");
        etQcode = (EditText) findViewById(R.id.et_qCode);
        btGetQcode = (Button) findViewById(R.id.bt_getQcode);
        showPwd1 = (ImageView) findViewById(R.id.im_showpwd1);
        showPwd2 = (ImageView) findViewById(R.id.im_showpwd2);
        login_button = (TextView) findViewById(R.id.login_button);
        login_button.setText("注册");
        login_button.setEnabled(false);
        ll_statement = findViewById(R.id.ll_statement);
        ll_statement.setVisibility(View.VISIBLE);
        setListener();

    }

    private void setListener() {
        findViewById(R.id.img_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        cb1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    String name = String.valueOf(etUsername.getText());
                    String code = String.valueOf(etQcode.getText());
                    String pwd = String.valueOf(etPwd1.getText());
                    if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(code)
                            && !TextUtils.isEmpty(pwd)) {
                        login_button.setEnabled(true);
                    }
                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(code)
                            || TextUtils.isEmpty(pwd)) {
                        login_button.setEnabled(false);
                    }
                } else {
                    login_button.setEnabled(false);
                }
            }
        });
        etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() >= 3) {
                    btGetQcode.setEnabled(true);
                } else {
                    btGetQcode.setEnabled(false);
                }

            }
        });
        btGetQcode.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                final String phone = etUsername.getText() == null ? "" : etUsername.getText().toString();

                if (TextUtils.isEmpty(phone)) {
                    ToastUtil.ShowToast_long(mContext, "手机号不能为空");
                    return;
                }
                if (phone.startsWith("1") && phone.length() == 11) {
                    service.checkPhoneIsRegister(new OnResponseListener() {

                        @Override
                        public void onResponse(boolean isSuccess, int what, Object obj) {
                            if (isSuccess) {
                                // 调用获取验证码接口

                                getQcode(phone);
                            } else {
                                AlertMsgUtils.showConfirm(mContext, "去登录", "取消", "您的手机号码已经注册", new AlertMsgUtils.OnDialogBtnClickListener() {
                                    @Override
                                    public void onConfirm(Dialog dialog) {
                                        Intent intent = new Intent(mContext, LoginActivity.class);
                                        intent.putExtra("fromTel", phone);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }
                        }

                    }, phone, phone);
                } else {
                    AlertMsgUtils.show(mContext, "手机号码格式有误");
                    // ToastUtil.ShowToast_long(mContext, "手机号码格式有误");
                }

            }
        });

        login_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                final String phoneText = etUsername.getText() == null ? "" : etUsername.getText().toString();
                final String qCodeText = etQcode.getText() == null ? "" : etQcode.getText().toString();
                //  final String nickNameText = etNickname.getText() == null ? "" : etNickname.getText().toString();
                final String pwdText1 = etPwd1.getText() == null ? "" : etPwd1.getText().toString();
                //  final String pwdText2 = etPwd2.getText() == null ? "" : etPwd2.getText().toString();
                if (checkText(phoneText, qCodeText, pwdText1)) {

                    WaitDialogBuilder.showNormalWithHintProgress(mContext,
                            getResources().getString(R.string.loding_progress));
                    service.registerUser(new OnResponseListener() {

                        @Override
                        public void onResponse(boolean isSuccess, int what, Object obj) {
                            WaitDialogBuilder.dismissDialog();
                            if (isDebug) {
                                login("17717551669", "f123456789");
                            }
                            if (isSuccess) {
                                login(phoneText, pwdText1);
                            } else {
                                ToastUtil.ShowToast_long(mContext, String.valueOf(obj));
                            }

                        }

                    }, phoneText, phoneText, "tv_" + phoneText, pwdText1);
                }

            }
        });

        ll_statement.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {// 用户协议
                Intent in = new Intent(mContext, AboutUsActivity.class);
                in.putExtra("titleText", "用户使用协议");
                in.putExtra("webUrl", LinkInfo.REGISTER_CODE);
                startActivity(in);
            }
        });

        showPwd1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (etPwd1.getText() == null || TextUtils.equals(etPwd1.getText().toString().trim(), ""))
                    return;
                if (!isChecked1) {
                    /* show the password */
                    // mEtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    etPwd1.setInputType(InputType.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_NORMAL);
                    isChecked1 = true;
                    showPwd1.setImageResource(R.mipmap.open_eye);

                } else {
                    /* hide the password */
                    // mEtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    etPwd1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    isChecked1 = false;
                    showPwd1.setImageResource(R.mipmap.close_eye);
                }

            }
        });
        showPwd2.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (etPwd2.getText() == null || TextUtils.equals(etPwd2.getText().toString().trim(), ""))
                    return;
                if (!isChecked2) {
                    /* show the password */
                    // mEtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    etPwd2.setInputType(InputType.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_NORMAL);
                    isChecked2 = true;

                } else {
                    /* hide the password */
                    // mEtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    etPwd2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    isChecked2 = false;
                }

            }
        });

        etUsername.addTextChangedListener(this);
        etQcode.addTextChangedListener(this);
        etPwd1.addTextChangedListener(this);
    }


    protected boolean checkText(String phoneText, String qCodeText, String pwdText1) {
        if (isDebug) {
            return true;
        }
        long duration = (System.currentTimeMillis() - lastGetQcodeTime) / 1000;

        if (TextUtils.isEmpty(phoneText)) {
            ToastUtil.ShowToast_long(mContext, "手机号不能为空");
            return false;
        }
        if (TextUtils.isEmpty(qCodeText)) {
            ToastUtil.ShowToast_long(mContext, "验证码不能为空");
            return false;
        }
        if (!TextUtils.equals(qCodeText, qCodeStr)) {
            ToastUtil.ShowToast_long(mContext, "验证码错误");
            return false;
        }

        if (duration > (10 * 60)) {
            ToastUtil.ShowToast_long(mContext, "验证码已过期");
            return false;
        }
        if (TextUtils.isEmpty(pwdText1)) {
            ToastUtil.ShowToast_long(mContext, "密码不能为空");
            return false;
        }
        if (!Pattern.matches(pwdPatten, pwdText1)) {
            ToastUtil.ShowToast_long(mContext, "密码由6-20位字母和数字组成");
            return false;
        }
        if (!cb1.isChecked()) {
            ToastUtil.ShowToast_long(mContext, "请先阅读业务使用协议");
            return false;
        }
//        if (TextUtils.isEmpty(pwdText2)) {
//            ToastUtil.ShowToast_long(mContext, "两次密码不一致");
//            return false;
//        }
//        if (!TextUtils.equals(pwdText1, pwdText2)) {
//            ToastUtil.ShowToast_long(mContext, "两次密码不一致");
//            return false;
//        }
        return true;
    }

    /**
     * 获取验证码
     *
     * @param phone
     */
    private void getQcode(String phone) {
        MD5 md5 = new MD5();
        String sign = md5.getMD5ofStr(phone);// MD5
        sign = sign.toUpperCase();

        WaitDialogBuilder.showNormalWithHintProgress(mContext, getResources().getString(R.string.loding_progress));

        timer = new TimeCount(60000, 1000);
        timer.start();
        service.smsVerificationCode(new OnResponseListener() {

            @Override
            public void onResponse(boolean isSuccess, int what, Object obj) {
                WaitDialogBuilder.dismissDialog();
                if (isSuccess) {
                    qCodeStr = String.valueOf(obj);
                    lastGetQcodeTime = System.currentTimeMillis();
                    //ToastUtil.ShowToast_long(mContext, "验证码已发送，请注意查收！");
                    AlertMsgUtils.show(mContext, "验证码已发送，请注意查收！");
                } else {
                    AlertMsgUtils.show(mContext, String.valueOf(obj));
                    // ToastUtil.ShowToast_long(mContext, String.valueOf(obj));
                }
            }

        }, phone, sign);
    }

    protected void resetQcodeButton() {
        btGetQcode.setText("获取验证码");
        btGetQcode.setBackgroundResource(R.drawable.btn_get_code_selector);
        btGetQcode.setClickable(true);
        btGetQcode.setEnabled(true);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String name = String.valueOf(etUsername.getText());
        String code = String.valueOf(etQcode.getText());
        String pwd = String.valueOf(etPwd1.getText());
        if (cb1.isChecked() && (
                !TextUtils.isEmpty(name) && !TextUtils.isEmpty(code)
                        && !TextUtils.isEmpty(pwd))) {
            login_button.setEnabled(true);
        }
        if (!cb1.isChecked() || TextUtils.isEmpty(name) || TextUtils.isEmpty(code)
                || TextUtils.isEmpty(pwd)) {
            login_button.setEnabled(false);
        }
    }

    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {// 计时完毕
            resetQcodeButton();
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程
            btGetQcode.setClickable(false);// 防止重复点击
            btGetQcode.setEnabled(false);
            btGetQcode.setBackgroundResource(R.drawable.btn_get_code_selector);
            btGetQcode.setText(millisUntilFinished / 1000 + "（s）");
        }
    }

    V2ImRequest mUserService = new V2ImRequest();
    ConfigRequest mConfigRequest = new ConfigRequest();

    private void login(String userName, String pwd) {
        String registrationID = "";
        String ip = LinkInfo.VIDEO_CODE;
        String port = LocalSharedPreferencesStorage.getConfigStrValue(this, "port");
        mConfigRequest.setServerAddress(ip, Integer.parseInt(port));
        mUserService.login(userName, pwd, registrationID, new HandlerWrap(mHandler, LoginActivity.LOG_IN_CALL_BACK, null));
    }


    @Override
    public void receiveMessage(Message msg) {
        V2Log.e("what-->" + msg.what);
        switch (msg.what) {
            case LoginActivity.LOG_IN_CALL_BACK:
                JNIResponse rlr = (JNIResponse) msg.obj;
                if (rlr.getResult() == JNIResponse.Result.TIME_OUT) {
                    V2Toast.makeText(mContext, R.string.error_time_out, V2Toast.LENGTH_LONG).show();
                    GlobalConfig.initConfigFile(true);
                } else if (rlr.getResult() == JNIResponse.Result.SUCCESS) {
                    saveUserInfo2File(etUsername.getText().toString(), AUTO_TYPE_ACCOUNT);
                    // 获取到登陆用户对象
                    User user = ((RequestLogInResponse) rlr).getUser();
                    String serverID = ((RequestLogInResponse) rlr).getServerID();
                    if (user == null || serverID == null) {
                        throw new RuntimeException(getResources().getString(R.string.login_error_init_user_id));
                    }
                    GlobalHolder.getInstance().setCurrentUser(user);
                    // 注销所需要初始化的全局变量或值
                    GlobalConfig.isLogined = true;
                    // 构建文件夹路径
                    GlobalConfig.SERVER_DATABASE_ID = serverID;
                    // 为登陆用户创建个人资料文件夹
                    createPersonFolder(user);
                    // 如果是游客登陆则不需要保存账号和密码
                    if (LocalSharedPreferencesStorage.getConfigBooleanValue(mContext, "userPwd", true)) {
                        saveUserNameAndPasswd(etUsername.getText().toString(), etPwd1.getText().toString());
                    } else {
                        saveUserNameAndPasswd(etUsername.getText().toString(), "");
                    }
                    // Save user info
                    initDataBaseTableCacheNames();
                    LocalSharedPreferencesStorage.putIntValue(mContext, GlobalConfig.KEY_LOGGED_IN, 1);
                    LocalSharedPreferencesStorage.putBooleanValue(mContext, "isAutoLogin", true);
                    Intent intent = new Intent(HgRegsterActivity.this, NickNamePhotoSettingActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                    finish();
                }

                if (rlr.getResult() != JNIResponse.Result.SUCCESS) {

                }
                break;
        }
    }

    @Override
    public void initViewAndListener() {

    }

    @Override
    public void receiveNewAvatar(User targetUser, Bitmap bnewAvatarm) {

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

    private boolean saveUserNameAndPasswd(String user, String passwd) {
        return LocalSharedPreferencesStorage.putStrValue(this, new String[]{"user", "passwd"},
                new String[]{user, passwd});
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

    private SparseArray<List<String>> autoHintDatas = new SparseArray<>();
    private static final int AUTO_TYPE_IP = 2;
    private static final int AUTO_TYPE_PORT = 3;
    private static final int AUTO_TYPE_ACCOUNT = 4;

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

}
