package com.bizcom.vc.hg.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bizcom.request.V2ImRequest;
import com.bizcom.request.jni.JNIResponse.Result;
import com.bizcom.service.JNIService;
import com.bizcom.util.AlertMsgUtils;
import com.bizcom.util.LocalSharedPreferencesStorage;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.hg.view.HeadLayoutManagerHG;
import com.bizcom.vc.widget.MProgressDialog;
import com.cgs.utils.ToastUtil;
import com.config.GlobalHolder;
import com.shdx.tvchat.phone.R;

import java.util.regex.Pattern;

import static com.bizcom.vc.hg.ui.HgRegsterActivity.pwdPatten;

public class ChangePwdActivity extends Activity {

    private static final int ERR_IM_UPDATEPWD_FAIL = 1007;
    private Context mContext;

    private boolean[] isCheckedArr = new boolean[]{false, false, false};

    private EditText mOldPassword;
    private EditText mNewPassword;
    //private EditText mConfimNewPassword;
    private TextView mFinishOver;

    private OnClickListener mFinishOverClickListener = new FinishOverClickListener();

    private V2ImRequest imService = new V2ImRequest();
    private LocalReceiver mLocalBroadcast = new LocalReceiver();
    private HeadLayoutManagerHG mHeadLayoutManager;

    private TextView text_title;
    private ImageView img_back;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hg_activity_change_pwd);
        text_title = (TextView) findViewById(R.id.text_title);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mContext = this;
        IntentFilter filter = new IntentFilter();
        filter.addAction(JNIService.JNI_BROADCAST_SETTING_UPDATE_PASSWORD);
        filter.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
        registerReceiver(mLocalBroadcast, filter);

        initViewAndListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        imService.clearCalledBack();
        try {
            unregisterReceiver(mLocalBroadcast);
        } catch (IllegalArgumentException e) {

        }
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    private void initViewAndListener() {
        String titleText = getIntent().getStringExtra("titleText");
        text_title.setText(titleText);
        mOldPassword = (EditText) findViewById(R.id.password1);

        mNewPassword = (EditText) findViewById(R.id.password2);

        // mConfimNewPassword = (EditText) findViewById(R.id.password3);

        CharSequence mOldPasswordHint = mContext.getText(R.string.setting_updatepassword_old);
        mOldPassword.setTag(mOldPasswordHint);

        CharSequence mNewPasswordHint = mContext.getText(R.string.setting_updatepassword_new);
        mNewPassword.setTag(mNewPasswordHint);

        CharSequence mConfimNewPasswordHint = mContext.getText(R.string.setting_updatepassword_newPassword);
        //mConfimNewPassword.setTag(mConfimNewPasswordHint);

        mFinishOver = (TextView) findViewById(R.id.comfirm_button);
        mFinishOver.setOnClickListener(mFinishOverClickListener);

        setShowPwdClickListener(R.id.im_showpwd1, mOldPassword, 0);
        setShowPwdClickListener(R.id.im_showpwd2, mNewPassword, 1);
        // setShowPwdClickListener(R.id.im_showpwd3, mConfimNewPassword, 2);

        findViewById(R.id.tv_forget_pwd).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChangePwdActivity.this, GetPwdActivity.class);
                intent.putExtra("isFromLogin", false);
                startActivity(intent);
            }
        });
    }

    public void setShowPwdClickListener(int id, final EditText inputPwd, final int isCheckIndex) {
        findViewById(id).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (inputPwd.getText() == null || TextUtils.equals(inputPwd.getText().toString().trim(), ""))
                    return;
                if (!isCheckedArr[isCheckIndex]) {
                    inputPwd.setInputType(InputType.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_NORMAL);
                    isCheckedArr[isCheckIndex] = true;
                    ((ImageView) findViewById(R.id.im_showpwd2)).setImageResource(R.mipmap.open_eye);

                } else {
                    inputPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    isCheckedArr[isCheckIndex] = false;
                    ((ImageView) findViewById(R.id.im_showpwd2)).setImageResource(R.mipmap.close_eye);
                }

            }
        });
    }

    private class FinishOverClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (GlobalHolder.getInstance().checkServerConnected(mContext)) {
                return;
            }

            String oldPassword = mOldPassword.getText().toString().trim();
            String newPassword = mNewPassword.getText().toString().trim();
            //   String confimNewPassword = mConfimNewPassword.getText().toString().trim();
            if (TextUtils.isEmpty(oldPassword) || oldPassword.equals(getText(R.string.setting_updatepassword_old))) {
                mOldPassword.setText("");
                ToastUtil.ShowToast_long(mContext, mContext.getText(R.string.setting_updatepassword_error_oldPswEmpty).toString());
                mOldPassword.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(newPassword) || newPassword.equals(getText(R.string.setting_updatepassword_new))) {
                mNewPassword.setText("");
                ToastUtil.ShowToast_long(mContext, mContext.getText(R.string.setting_updatepassword_error_newPswEmpty).toString());
                mNewPassword.requestFocus();
                return;
            }


            if (!Pattern.matches(pwdPatten, newPassword)) {
                ToastUtil.ShowToast_long(mContext, "密码由6-20位字母和数字组成");
                mNewPassword.requestFocus();
                return;
            }

//            if (!newPassword.equals(confimNewPassword)) {
//                ToastUtil.ShowToast_long(mContext, mContext.getText(R.string.setting_updatepassword_error_PswNotSame).toString());
//                mConfimNewPassword.requestFocus();
//                return;
//            }

            if (oldPassword.equals(newPassword)) {
                mNewPassword.setText("");
                ToastUtil.ShowToast_long(mContext, mContext.getText(R.string.setting_updatepassword_error_same).toString());
                mNewPassword.requestFocus();
                return;
            }

            WaitDialogBuilder.showNormalWithHintProgress(mContext);
            imService.imRequestUpdatePassWord(oldPassword, newPassword, null);
        }
    }

    MProgressDialog mProgressDialog;

    class LocalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (JNIService.JNI_BROADCAST_SETTING_UPDATE_PASSWORD.equals(action)) {
                int result = intent.getIntExtra("result", -1);
                if (result == Result.SUCCESS.ordinal()) {
                    WaitDialogBuilder.dismissDialog();
                    // V2Toast.makeText(mContext, R.string.setting_update_password_success, 0).show();
                    // WaitDialogBuilder.changeHintText(mContext.getText(R.string.setting_logouting).toString());
                    // V2ImRequest.invokeNative(V2ImRequest.NATIVE_CANNEL_LOGIN, 0);
                    // LocalSharedPreferencesStorage.putConfigStrValue(mContext, "passwd", mNewPassword.getText().toString().trim());
                    AlertMsgUtils.show(ChangePwdActivity.this, getResources().getString(R.string.setting_update_password_success), new AlertMsgUtils.OnDialogBtnClickListener() {
                        @Override
                        public void onConfirm(Dialog dialog) {
                            dialog.dismiss();
                            if (mProgressDialog == null) {
                                mProgressDialog = new MProgressDialog(ChangePwdActivity.this);
                                mProgressDialog.setMessage(getText(R.string.setting_logouting).toString());
                            }
                            mProgressDialog.show();

                            V2ImRequest.invokeNative(V2ImRequest.NATIVE_CANNEL_LOGIN, 0);
                            LocalSharedPreferencesStorage.putConfigStrValue(mContext, "passwd", mNewPassword.getText().toString().trim());
                            getSharedPreferences("tvl", MODE_PRIVATE).edit().putBoolean("isLogin", false).commit();
                        }
                    });
                } else {
                    if (ERR_IM_UPDATEPWD_FAIL == result) {
                        AlertMsgUtils.show(mContext, getResources().getString(R.string.setting_updatepassword_error_oldPsw));
                        //V2Toast.makeText(mContext, R.string.setting_updatepassword_error_oldPsw, 0).show();
                    } else {
                        //V2Toast.makeText(mContext, R.string.setting_update_password_failed, 0).show();
                        AlertMsgUtils.show(mContext, getResources().getString(R.string.setting_update_password_failed));

                    }
                    WaitDialogBuilder.dismissDialog();
                }
            }
        }
    }
}
