package com.bizcom.vc.hg.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.bizcom.util.AlertMsgUtils;
import com.bizcom.vc.hg.util.QRUtil;
import com.bizcom.vc.hg.util.UserHeaderImgHelper;
import com.bizcom.vc.hg.web.interf.BussinessManger;
import com.bizcom.vc.hg.web.interf.IBussinessManager;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.shdx.tvchat.phone.R;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 我的二维码
 * Created by admin on 2016/12/9.
 */
public class QRCodeActivity extends Activity {


    String tag = QRCodeActivity.class.getName();

    Unbinder unbinder;

    @BindView(R.id.user_head_icon)
    SimpleDraweeView user_header_icon;

    @BindView(R.id.tv_name)
    TextView tv_name;

    @BindView(R.id.tv_phone)
    TextView tv_phone;

    @BindView(R.id.text_title)
    TextView text_title;


    @BindView(R.id.img_code)
    ImageView img_code;


    User user;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        unbinder = ButterKnife.bind(this);
        text_title.setText("我的二维码");
        user = GlobalHolder.getInstance().getCurrentUser();
        tv_name.setText(user.getDisplayName());
        tv_phone.setText(user.getAccount());
        UserHeaderImgHelper.display(user_header_icon, user);

        int width = getWindowManager().getDefaultDisplay().getWidth();
        Bitmap bitmap = QRUtil.createQRImage("tvl:" + user.getmUserId(), width / 5 * 4, width / 5 * 4);
        img_code.setImageBitmap(bitmap);
        preferences = getSharedPreferences("tvl", MODE_PRIVATE);

    }


    @OnClick(R.id.img_back)
    void back() {
        finish();
    }


    @OnClick(R.id.btn_create)
    void createCode() {
        String uid = String.valueOf(user.getmUserId());
        uid = uid.substring(2, uid.length());
        String chatPwd = preferences.getString("chatPwd", null);
        //本地聊口令为空
        if (TextUtils.isEmpty(chatPwd)) {
            getChatPwd(uid);
        } else {
            JSONObject jsonObject = JSONObject.parseObject(chatPwd);
            if (jsonObject != null) {
                long muid = jsonObject.getLong("uid");
                String pwd = jsonObject.getString("pwd");
                //当前登录的用户与本地存储的聊口令一致
                if (muid == user.getmUserId()) {
                    openChatPwdDialog(pwd);
                } else {
                    //不一致 则重新生成聊口令并存储到本地
                    getChatPwd(uid);
                }
            } else {
                getChatPwd(uid);
            }
        }

    }

    void getChatPwd(String uid) {
        BussinessManger.getInstance(this).generateChatPassword("0", uid, "", new IBussinessManager.OnResponseListener() {
            @Override
            public void onResponse(boolean isSuccess, int what, Object obj) {
                Log.e(tag, "generateChatPassword-->" + String.valueOf(obj));
                if (isSuccess) {
                    openChatPwdDialog(String.valueOf(obj));
                    Map<String, Object> map = new HashMap<>();
                    map.put("uid", user.getmUserId());
                    map.put("pwd", String.valueOf(obj));
                    preferences.edit().putString("chatPwd", new Gson().toJson(map)).commit();
                } else {
                    Toast.makeText(QRCodeActivity.this, String.valueOf(obj), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void openChatPwdDialog(String pwd) {
        ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        String tvl_chat_pwd_hint = getResources().getString(R.string.tvl_chat_pwd_hint);
        Log.e(tag,"pwdstr:"+tvl_chat_pwd_hint+pwd);
        if (cmb != null) {
            if (Build.VERSION.SDK_INT >= 21) {
                ClipData clip = cmb.getPrimaryClip();
                if (clip != null) {
                    cmb.setPrimaryClip(ClipData.newPlainText("tvl_chat_pwd", tvl_chat_pwd_hint + pwd));
                }else{
                    cmb.setText(tvl_chat_pwd_hint + pwd);
                }
            } else {
                cmb.setText(tvl_chat_pwd_hint + pwd);
            }
        }
        Log.e(tag,"ClipboardManager:"+cmb+"");
        AlertMsgUtils.showChatPwdDialog(QRCodeActivity.this, "去QQ粘贴", "去微信粘贴", pwd, new AlertMsgUtils.OnDialogBtnsClickListener() {
            @Override
            public void onBtn1Click(Dialog dialog) {
                openApp("com.tencent.mobileqq", "com.tencent.mobileqq.activity.SplashActivity");
            }

            @Override
            public void onBtn2Click(Dialog dialog) {
                openApp("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
            }
        });
    }

    public void openApp(String str1, String str2) {
        if (!isPkgInstalled(str1)) {
            Toast.makeText(this, "应用还没有安装", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        ComponentName cmp = new ComponentName(str1, str2);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(cmp);
        startActivityForResult(intent, 0);
    }

    public boolean isPkgInstalled(String packageName) {

        if (packageName == null || "".equals(packageName))
            return false;
        android.content.pm.ApplicationInfo info = null;
        try {
            info = getPackageManager().getApplicationInfo(packageName, 0);
            return info != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }


    }


}
