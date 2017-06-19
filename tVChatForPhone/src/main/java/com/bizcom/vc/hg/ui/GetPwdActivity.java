package com.bizcom.vc.hg.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bizcom.request.V2ImRequest;
import com.bizcom.util.AlertMsgUtils;
import com.bizcom.util.LocalSharedPreferencesStorage;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.hg.view.HeadLayoutManagerHG;
import com.bizcom.vc.hg.web.MD5;
import com.bizcom.vc.hg.web.interf.BussinessManger;
import com.bizcom.vc.hg.web.interf.IBussinessManager;
import com.bizcom.vc.hg.web.interf.IBussinessManager.OnResponseListener;
import com.bizcom.vc.widget.MProgressDialog;
import com.cgs.utils.ToastUtil;
import com.config.GlobalHolder;
import com.shdx.tvchat.phone.R;

import java.util.regex.Pattern;

public class GetPwdActivity extends Activity implements TextWatcher {
    String tag  = "GetPwdActivity";
    public static final int GetPwd = -9768;
    private boolean isChecked1 = false;// 控制密码显示
    private boolean isChecked2;// 控制密码显示

    private String titleText;
    private GetPwdActivity mContext;
    private HeadLayoutManagerHG mHeadLayoutManager;
    private Button btGetQcode;
    private EditText etPwd1;
    private EditText etUsername;
    // private EditText etPwd2;
    private EditText etQcode;
    private ImageView showPwd1;
    private ImageView showPwd2;
    private TextView login_button;
    private IBussinessManager service;
    private String uid;
    private TimeCount timer;
    private String qCodeStr = "";// 保存验证码 本地校验
    private long lastGetQcodeTime = 0;

    boolean isFromLogin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pwd);
        mContext = this;
        service = BussinessManger.getInstance(mContext);
        isFromLogin = getIntent().getBooleanExtra("isFromLogin", true);
        initView();
    }

    private void initView() {
        titleText = getIntent().getStringExtra("titleText");
        mHeadLayoutManager = new HeadLayoutManagerHG(mContext, findViewById(R.id.head_layout), false);
        mHeadLayoutManager.updateTitle(titleText);
        etUsername = (EditText) findViewById(R.id.et_username);
        findViewById(R.id.reName).setVisibility(View.GONE);
        etPwd1 = (EditText) findViewById(R.id.password1);
        //  etPwd2 = (EditText) findViewById(R.id.password2);
        etPwd1.setHint("请输入新密码");
        // etPwd2.setHint("请确认新密码");
        etQcode = (EditText) findViewById(R.id.et_qCode);
        btGetQcode = (Button) findViewById(R.id.bt_getQcode);
        showPwd1 = (ImageView) findViewById(R.id.im_showpwd1);
        showPwd2 = (ImageView) findViewById(R.id.im_showpwd2);
        login_button = (TextView) findViewById(R.id.login_button);
        login_button.setText("保存");
        login_button.setEnabled(false);
        setListener();
        TextView text_title = (TextView) findViewById(R.id.text_title);
        text_title.setVisibility(View.VISIBLE);
        text_title.setText(titleText);
        if (!isFromLogin) {
            etUsername.setText(GlobalHolder.getInstance().getCurrentUser().getAccount());
            etUsername.setEnabled(false);
        }
    }

    MProgressDialog mProgressDialog;
    private void setListener() {
        findViewById(R.id.img_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
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
                                ToastUtil.ShowToast_long(mContext, "该手机号未注册");
                            } else {

                                // 调用获取验证码接口
                                uid = String.valueOf(obj);
                                getQcode(phone);
                            }
                        }

                    }, phone, phone);
                } else {
                    ToastUtil.ShowToast_long(mContext, "手机号码格式有误");
                }

            }
        });

        login_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                final String phoneText = etUsername.getText() == null ? "" : etUsername.getText().toString();
                final String qCodeText = etQcode.getText() == null ? "" : etQcode.getText().toString();
                final String pwdText1 = etPwd1.getText() == null ? "" : etPwd1.getText().toString();
                //  final String pwdText2 = etPwd2.getText() == null ? "" : etPwd2.getText().toString();
                if (checkText(phoneText, qCodeText, pwdText1)) {

                    WaitDialogBuilder.showNormalWithHintProgress(mContext,
                            getResources().getString(R.string.loding_progress));
                    service.updatePwd(new OnResponseListener() {

                        @Override
                        public void onResponse(boolean isSuccess, int what, Object obj) {
                            WaitDialogBuilder.dismissDialog();
                            if (isSuccess) {
                                if (isFromLogin) {
                                    Intent intent = new Intent();
                                    intent.putExtra("phone", phoneText);
                                    intent.putExtra("pwd", pwdText1);
                                    setResult(GetPwd, intent);
                                    finish();
                                } else {
                                    AlertMsgUtils.show(GetPwdActivity.this, getResources().getString(R.string.setting_update_password_success), new AlertMsgUtils.OnDialogBtnClickListener() {
                                        @Override
                                        public void onConfirm(Dialog dialog) {
                                            dialog.dismiss();
                                            if (mProgressDialog == null) {
                                                mProgressDialog = new MProgressDialog(GetPwdActivity.this);
                                                mProgressDialog.setMessage(getText(R.string.setting_logouting).toString());
                                            }
                                            mProgressDialog.show();
                                            V2ImRequest.invokeNative(V2ImRequest.NATIVE_CANNEL_LOGIN, 0);
                                            LocalSharedPreferencesStorage.putConfigStrValue(mContext, "passwd", etPwd1.getText().toString().trim());
                                            getSharedPreferences("tvl", MODE_PRIVATE).edit().putBoolean("isLogin", false).commit();
                                        }
                                    });
                                }
                            } else {
                                ToastUtil.ShowToast_long(mContext, String.valueOf(obj));
                            }

                        }

                    }, uid + "", pwdText1);
                }

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

        etUsername.addTextChangedListener(this);
        etQcode.addTextChangedListener(this);
        etPwd1.addTextChangedListener(this);

//        showPwd2.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                if (etPwd2.getText() == null || TextUtils.equals(etPwd2.getText().toString().trim(), ""))
//                    return;
//                if (!isChecked2) {
//					/* show the password */
//                    // mEtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
//                    etPwd2.setInputType(InputType.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_NORMAL);
//                    isChecked2 = true;
//
//                } else {
//					/* hide the password */
//                    // mEtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
//                    etPwd2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
//                    isChecked2 = false;
//                }
//
//            }
//        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(tag, "onDestroy");
        if(mProgressDialog!=null){
            mProgressDialog.dismiss();
        }
    }

    protected boolean checkText(String phoneText, String qCodeText, String pwdText1) {
        long duration = (System.currentTimeMillis() - lastGetQcodeTime) / 1000;

        if (TextUtils.isEmpty(phoneText)) {
            ToastUtil.ShowToast_long(mContext, "手机号不能为空");
            return false;
        }
        if (TextUtils.isEmpty(qCodeText)) {
            ToastUtil.ShowToast_long(mContext, "验证码不能为空");
            return false;
        } else if (!TextUtils.equals(qCodeText, qCodeStr)) {
            ToastUtil.ShowToast_long(mContext, "验证码不正确");
            return false;
        }

        if (duration > (5 * 60)) {
            ToastUtil.ShowToast_long(mContext, "验证码已过期");
            return false;
        }
        if (TextUtils.isEmpty(pwdText1)) {
            ToastUtil.ShowToast_long(mContext, "密码不能为空");
            return false;
        }
        if (!Pattern.matches(HgRegsterActivity.pwdPatten, pwdText1)) {
            ToastUtil.ShowToast_long(mContext, "密码由6-20位字母和数字组成");
            return false;
        }
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
                    ToastUtil.ShowToast_long(mContext, "验证码已发送，请注意查收！");
                } else {
                    ToastUtil.ShowToast_long(mContext, String.valueOf(obj));
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
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(code)
                && !TextUtils.isEmpty(pwd)) {
            login_button.setEnabled(true);
        }
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(code)
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
}
