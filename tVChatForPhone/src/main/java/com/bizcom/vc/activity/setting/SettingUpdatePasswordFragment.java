package com.bizcom.vc.activity.setting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.bizcom.request.V2ImRequest;
import com.bizcom.request.jni.JNIResponse.Result;
import com.bizcom.service.JNIService;
import com.bizcom.util.V2Toast;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.widget.MProgressDialog;
import com.bizcom.vc.widget.cus.edittext.ClearEditText;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.shdx.tvchat.phone.R;

public class SettingUpdatePasswordFragment extends Fragment {

    private static final int ERR_IM_UPDATEPWD_FAIL = 1007;
    private ViewGroup rootView;
    private Context mContext;

    private ClearEditText mOldPassword;
    private ClearEditText mNewPassword;
    private ClearEditText mConfimNewPassword;
    private TextView mFinishOver;

    private ComETOnTouchListener mComETOnTouchListener = new ComETOnTouchListener();
    private ComETOnFocusChangeListener mComETOnFoucusChangeListener = new ComETOnFocusChangeListener();
    private OnClickListener mFinishOverClickListener = new FinishOverClickListener();
    private ComETOnTextWatcher mComETOnTextWatcher = new ComETOnTextWatcher();

    private V2ImRequest imService = new V2ImRequest();
    private LocalReceiver mLocalBroadcast = new LocalReceiver();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        ((SettingActivity) getActivity())
                .setCurrentVisibleFragment(SettingUpdatePasswordFragment.class.getSimpleName());
        IntentFilter filter = new IntentFilter();
        filter.addAction(JNIService.JNI_BROADCAST_SETTING_UPDATE_PASSWORD);
        filter.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
        mContext.registerReceiver(mLocalBroadcast, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.tab_fragment_setting_password, container, false);
        View functionLy = rootView.findViewById(R.id.ws_activity_main_title_functionLy);
        if (functionLy != null) {
            functionLy.setVisibility(View.INVISIBLE);
        }
        initViewAndListener();
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        imService.clearCalledBack();
        try {
            mContext.unregisterReceiver(mLocalBroadcast);
        } catch (IllegalArgumentException e) {

        }
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((ViewGroup) rootView.getParent()).removeView(rootView);
    }

    private void initViewAndListener() {
        TextView mTitleContent = (TextView) rootView.findViewById(R.id.ws_common_activity_title_content);
        mTitleContent.setText(getText(R.string.setting_update_password));
        TextView mBackIV = (TextView) rootView.findViewById(R.id.ws_common_activity_title_left_button);
        ((SettingActivity) getActivity()).setComBackImageTV(mBackIV);
        mBackIV.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ((SettingActivity) mContext).replaceFragment(SettingTabFragment.class.getSimpleName());
            }
        });
        rootView.findViewById(R.id.ws_common_activity_title_right_button).setVisibility(View.GONE);

        CharSequence mOldPasswordHint = mContext.getText(R.string.setting_updatepassword_old);
        mOldPassword = (ClearEditText) rootView.findViewById(R.id.ws_setting_updatePsssword_old);
        mOldPassword.setTag(mOldPasswordHint);
        mOldPassword.setRegexPattern(GlobalConfig.CHINESE_REGEX);
        mOldPassword.setSpecificChars(mOldPasswordHint.toString());

        CharSequence mNewPasswordHint = mContext.getText(R.string.setting_updatepassword_new);
        mNewPassword = (ClearEditText) rootView.findViewById(R.id.ws_setting_updatePsssword_new);
        mNewPassword.setTag(mNewPasswordHint);
        mNewPassword.setRegexPattern(GlobalConfig.CHINESE_REGEX);
        mNewPassword.setSpecificChars(mNewPasswordHint.toString());

        CharSequence mConfimNewPasswordHint = mContext.getText(R.string.setting_updatepassword_newPassword);
        mConfimNewPassword = (ClearEditText) rootView.findViewById(R.id.ws_setting_updatePsssword_new_confim);
        mConfimNewPassword.setTag(mConfimNewPasswordHint);
        mConfimNewPassword.setRegexPattern(GlobalConfig.CHINESE_REGEX);
        mConfimNewPassword.setSpecificChars(mConfimNewPasswordHint.toString());

        mFinishOver = (TextView) rootView.findViewById(R.id.ws_setting_updatePasswrod_finish);
        mFinishOver.setOnClickListener(mFinishOverClickListener);
        mFinishOver.setEnabled(false);
        mFinishOver.setClickable(false);

        mOldPassword.setOnTouchListener(mComETOnTouchListener);
        mNewPassword.setOnTouchListener(mComETOnTouchListener);
        mConfimNewPassword.setOnTouchListener(mComETOnTouchListener);

        mOldPassword.setOnFocusChangeListener(mComETOnFoucusChangeListener);
        mNewPassword.setOnFocusChangeListener(mComETOnFoucusChangeListener);
        mConfimNewPassword.setOnFocusChangeListener(mComETOnFoucusChangeListener);

        mOldPassword.addTextChangedListener(mComETOnTextWatcher);
        mNewPassword.addTextChangedListener(mComETOnTextWatcher);
        mConfimNewPassword.addTextChangedListener(mComETOnTextWatcher);
    }

    private class FinishOverClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (GlobalHolder.getInstance().checkServerConnected(mContext)) {
                return;
            }

            String oldPassword = mOldPassword.getText().toString().trim();
            String newPassword = mNewPassword.getText().toString().trim();
            String confimNewPassword = mConfimNewPassword.getText().toString().trim();
            if (TextUtils.isEmpty(oldPassword) || oldPassword.equals(getText(R.string.setting_updatepassword_old))) {
                mOldPassword.setText("");
                mOldPassword.setError(mContext.getText(R.string.setting_updatepassword_error_oldPswEmpty));
                mOldPassword.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(newPassword) || newPassword.equals(getText(R.string.setting_updatepassword_new))) {
                mNewPassword.setText("");
                mNewPassword.setError(mContext.getText(R.string.setting_updatepassword_error_newPswEmpty));
                mNewPassword.requestFocus();
                return;
            }

            if (newPassword.length() < 6 || newPassword.length() > 18) {
                mNewPassword.setText("");
                mNewPassword.setError(mContext.getText(R.string.setting_updatepassword_error_newPswLen));
                mNewPassword.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(confimNewPassword)
                    || confimNewPassword.equals(getText(R.string.setting_updatepassword_newPassword))) {
                mConfimNewPassword.setText("");
                mConfimNewPassword.setError(mContext.getText(R.string.setting_updatepassword_error_confimPswEmpty));
                mConfimNewPassword.requestFocus();
                return;
            }

            if (!newPassword.equals(confimNewPassword)) {
                mConfimNewPassword.setError(mContext.getText(R.string.setting_updatepassword_error_PswNotSame));
                mConfimNewPassword.requestFocus();
                return;
            }

            if (oldPassword.equals(newPassword)) {
                mNewPassword.setText("");
                mNewPassword.setError(mContext.getText(R.string.setting_updatepassword_error_same));
                mNewPassword.requestFocus();
                return;
            }

            WaitDialogBuilder.showNormalWithHintProgress(mContext);
            imService.imRequestUpdatePassWord(oldPassword, newPassword, null);
        }
    }

    ;

    private class ComETOnTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                EditText et = ((EditText) v);
                et.requestFocus();
                et.setError(null);
            }
            return true;
        }
    }

    private class ComETOnTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (getText(R.string.setting_updatepassword_old).equals(s.toString())
                    || getText(R.string.setting_updatepassword_new).equals(s.toString())
                    || getText(R.string.setting_updatepassword_newPassword).equals(s.toString())) {
                return;
            }

            if (!TextUtils.isEmpty(s)) {
                mFinishOver.setEnabled(true);
                mFinishOver.setClickable(true);
                mFinishOver.setBackgroundResource(R.drawable.ws_com_btn_selector);
            } else {
                mFinishOver.setEnabled(false);
                mFinishOver.setClickable(false);
                mFinishOver.setBackgroundResource(R.drawable.regist_corner_button_normal);
            }
        }
    }

    private class ComETOnFocusChangeListener implements OnFocusChangeListener {

        @Override
        public void onFocusChange(View arg0, boolean focus) {
            EditText target = (EditText) arg0;
            String tagetText = (String) target.getTag();
            if (focus) {
                if (tagetText.equals(target.getText().toString())) {
                    target.setText("");
                }
                target.setTextColor(Color.BLACK);
                target.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(target, InputMethodManager.SHOW_FORCED);
            } else {
                if (target.getText().toString().trim().isEmpty()) {
                    target.setError(null);
                    target.setText(tagetText);
                    target.setInputType(InputType.TYPE_CLASS_TEXT);
                    target.setTextColor(mContext.getResources().getColor(R.color.regist_edit_hint));
                } else {
                    target.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
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
                    V2Toast.makeText(mContext, R.string.setting_update_password_success, 0).show();
                    if (mProgressDialog == null) {
                        mProgressDialog = new MProgressDialog(getActivity());
                        mProgressDialog.setMessage(getText(R.string.setting_logouting).toString());
                    }
                    mProgressDialog.show();
                    V2ImRequest.invokeNative(V2ImRequest.NATIVE_CANNEL_LOGIN, 0);
                } else {
                    if (ERR_IM_UPDATEPWD_FAIL == result) {
                        V2Toast.makeText(mContext, R.string.setting_updatepassword_error_oldPsw, 0).show();
                    } else {
                        V2Toast.makeText(mContext, R.string.setting_update_password_failed, 0).show();
                    }
                    WaitDialogBuilder.dismissDialog();
                }
            }
        }
    }
}
