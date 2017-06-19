package com.bizcom.vc.activity.setting;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.bizcom.request.V2ImRequest;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.jni.JNIResponse.Result;
import com.bizcom.request.util.EscapedcharactersProcessing;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.LocalSharedPreferencesStorage;
import com.bizcom.util.MessageUtil;
import com.bizcom.util.V2Log;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.activity.BaseActivity;
import com.bizcom.vc.activity.main.MainActivity;
import com.bizcom.vo.User;
import com.config.PublicIntent;
import com.shdx.tvchat.phone.R;

public class RegistActivity extends BaseActivity implements OnClickListener {

    private static final int SUCCESS_IM_CREATE_VALIDATECODE = 0;

    private EditText mValidateET;
    private TextView mFinishTV;

    private V2ImRequest imService = new V2ImRequest();
    ;

    private boolean isWaitingFinish;

    private ValidateETOnFocusChangeListener mValidateETOnFocusChangeListener = new ValidateETOnFocusChangeListener();
    private ValidateETOnTouchListener mValidateETOnTouchListener = new ValidateETOnTouchListener();
    private ValidateETOnTextWatcher mValidateETOnTextWatcher = new ValidateETOnTextWatcher();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_regist);
        super.setNeedAvatar(false);
        super.setNeedBroadcast(true);
        super.setNeedHandler(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MessageUtil.hideKeyBoard(mContext, mValidateET.getWindowToken());
        imService.clearCalledBack();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void addBroadcast(IntentFilter filter) {
        filter.addAction(PublicIntent.BROADCAST_LOGIN_NOTIFY_TO_FINISH_REGIST);
    }

    @Override
    public void receiveBroadcast(Intent intent) {
        String action = intent.getAction();
        if (PublicIntent.BROADCAST_LOGIN_NOTIFY_TO_FINISH_REGIST.equals(action)) {
            boolean login = intent.getBooleanExtra("isLoginSuccess", false);
            if (login) {
                mContext.startActivity(new Intent(mContext, MainActivity.class));
            }
            WaitDialogBuilder.dismissDialog();
            finish();
        }
    }

    @Override
    public void receiveMessage(Message msg) {
        switch (msg.what) {
            case V2ImRequest.JNI_REQUEST_REGIST_RETURN:
                isWaitingFinish = false;
                JNIResponse registRes = (JNIResponse) msg.obj;
                Result registResult = registRes.getResult();
                if (registResult == Result.SUCCESS) {
                    String[] overResult = (String[]) registRes.resObj;
                    String registAccount = overResult[0];
                    String registPassword = overResult[1];
                    String result = overResult[2];

                    LocalSharedPreferencesStorage.putConfigStrValue(mContext,
                            mValidateET.getText().toString().trim() + "account", registAccount);
                    LocalSharedPreferencesStorage.putConfigStrValue(mContext ,
                            mValidateET.getText().toString().trim() + "password" , registPassword);
                    if (Integer.valueOf(result) == SUCCESS_IM_CREATE_VALIDATECODE) {
                        WaitDialogBuilder.changeHintText(getResources().getText(R.string.regist_connecting_server).toString());
                        Intent i = new Intent();
                        i.setAction(PublicIntent.BROADCAST_REGIST_NOTIFY_TO_LOGIN);
                        i.addCategory(PublicIntent.DEFAULT_CATEGORY);
                        i.putExtra("registAccount", registAccount);
                        i.putExtra("registPassword", registPassword);
                        sendBroadcast(i);
                    } else {
                        WaitDialogBuilder.dismissDialog();
                    }
                } else if (registResult == Result.TIME_OUT) {
                    WaitDialogBuilder.dismissDialog();
                    showLongToast(R.string.regist_errorResponse_connectFailed);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void initViewAndListener() {
        findViewById(R.id.ws_common_activity_title_content).setVisibility(View.GONE);
        findViewById(R.id.ws_common_activity_title_right_button).setVisibility(View.GONE);
        TextView mBackBT = (TextView) findViewById(R.id.ws_common_activity_title_left_button);
        setComBackImageTV(mBackBT);

        mValidateET = (EditText) findViewById(R.id.ws_regist_validate_edit);
        mFinishTV = (TextView) findViewById(R.id.ws_regist_finish);
        mFinishTV.setEnabled(false);

        mFinishTV.setOnClickListener(this);
        mBackBT.setOnClickListener(this);
        mValidateET.setOnFocusChangeListener(mValidateETOnFocusChangeListener);
        mValidateET.setOnTouchListener(mValidateETOnTouchListener);
        mValidateET.addTextChangedListener(mValidateETOnTextWatcher);
    }

    @Override
    public void receiveNewAvatar(User targetUser, Bitmap bnewAvatarm) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ws_regist_finish:
                if (isWaitingFinish) {
                    return;
                }

                isWaitingFinish = true;
                final String validateText = mValidateET.getText().toString().trim();
                if (TextUtils.isEmpty(validateText) || validateText.equals(getText(R.string.regist_validate_text))) {
                    mValidateET.setText("");
                    mValidateET.setError(getText(R.string.regist_error_empty_validate));
                    mValidateET.requestFocus();
                    isWaitingFinish = false;
                    return;
                }

                WaitDialogBuilder.showNormalWithHintProgress(mContext);
                mHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        String localCacheAccount = LocalSharedPreferencesStorage.getConfigStrValue(mContext, validateText + "account");
                        String localCachePassword = LocalSharedPreferencesStorage.getConfigStrValue(mContext, validateText + "password");
                        if (TextUtils.isEmpty(localCacheAccount) || TextUtils.isEmpty(localCachePassword)) {
                            imService.imRequestRegistGuestUser(EscapedcharactersProcessing.convert(validateText),
                                    new HandlerWrap(mHandler, V2ImRequest.JNI_REQUEST_REGIST_RETURN, null));
                        } else {
                            V2Log.d("RegistActivity" , "localcache account : " + localCacheAccount + " | password : " + localCachePassword);
                            WaitDialogBuilder.changeHintText(getResources().getText(R.string.regist_connecting_server).toString());
                            Intent i = new Intent();
                            i.setAction(PublicIntent.BROADCAST_REGIST_NOTIFY_TO_LOGIN);
                            i.addCategory(PublicIntent.DEFAULT_CATEGORY);
                            i.putExtra("registAccount", localCacheAccount);
                            i.putExtra("registPassword", localCachePassword);
                            sendBroadcast(i);
                        }
                    }
                }, 1500);
                break;
            case R.id.ws_common_activity_title_left_button:
                finish();
                break;
            default:
                break;
        }
    }

    private class ValidateETOnFocusChangeListener implements OnFocusChangeListener {

        @Override
        public void onFocusChange(View arg0, boolean focus) {
            if (focus) {
                if (mContext.getText(R.string.regist_validate_text).equals(mValidateET.getText().toString())) {
                    mValidateET.setText("");
                }
                mValidateET.setTextColor(Color.BLACK);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mValidateET, InputMethodManager.SHOW_FORCED);
            } else {
                if (mValidateET.getText().toString().trim().isEmpty()) {
                    mValidateET.setError(null);
                    mValidateET.setText(R.string.regist_validate_text);
                    mValidateET.setTextColor(mContext.getResources().getColor(R.color.regist_edit_hint));
                }
                MessageUtil.hideKeyBoard(mContext, mValidateET.getWindowToken());
            }
        }
    }

    private class ValidateETOnTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                EditText et = ((EditText) v);
                et.requestFocus();
                et.setError(null);
                et.setText("");
            }
            return true;

        }
    }

    private class ValidateETOnTextWatcher implements TextWatcher {


        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (getText(R.string.regist_validate_text).equals(s.toString())) {
                return;
            }

            if (!TextUtils.isEmpty(s)) {
                mFinishTV.setEnabled(true);
                mFinishTV.setBackgroundResource(R.drawable.ws_com_btn_selector);
            } else {
                mFinishTV.setEnabled(false);
                mFinishTV.setBackgroundResource(R.drawable.regist_corner_button_normal);
            }
        }
    }
}
